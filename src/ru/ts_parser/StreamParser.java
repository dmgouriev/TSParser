package ru.ts_parser;

import ru.ts_parser.tools.Tools;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static ru.ts_parser.MPEGConstant.*;
import ru.ts_parser.entity.Packet;
import ru.ts_parser.parser.header.AdaptationFieldParser;
import ru.ts_parser.parser.header.HeaderParser;
import ru.ts_parser.parser.Parser;
import ru.ts_parser.parser.psi.NITParser;
import ru.ts_parser.parser.psi.PATParser;
import ru.ts_parser.parser.psi.PMTParser;
import ru.ts_parser.parser.psi.SDTParser;

/**
 *
 * @author dmgouriev
 */
public class StreamParser extends Parser {
    
    public static StreamParser getInstance() {
        return StreamParserHolder.INSTANCE;
    }
    
    private static class StreamParserHolder {

        private static final StreamParser INSTANCE = new StreamParser();
    }
    
    public static enum State {
        SUSPENDED,
        RUNNING,
        STOPPED
    }

    private HeaderParser headerParser;
    private AdaptationFieldParser adaptationFieldParser;

    private PATParser patParser;
    private PMTParser pmtParser;
    private SDTParser sdtParser;
    private NITParser nitParser;

    private boolean hasPAT = false;
    private boolean hasPMT = false;
    private boolean hasSDT = false;
    private boolean hasNIT = false;

    private ExecutorService parseExecutorService;
    private State state;

    private long packetIndex = 0;
    private TSTableData parserTables;

    private StreamParser() {
        reInitThis();
    }
    
    private void reInitThis() {
        headerParser = new HeaderParser();
        adaptationFieldParser = new AdaptationFieldParser();

        patParser = new PATParser();
        pmtParser = new PMTParser();
        sdtParser = new SDTParser();
        nitParser = new NITParser();

        hasPAT = false;
        hasPMT = false;
        hasSDT = false;
        hasNIT = false;

        packetIndex = 0;
        parserTables = new TSTableData();

        this.parseExecutorService = Executors.newFixedThreadPool(1);
        this.state = State.SUSPENDED;
    }
    

    public void parse(final byte[] buffer) {
        if (state == State.STOPPED) {
            return;
        }
        if (state == State.SUSPENDED) {
            state = State.RUNNING;
        }
        parseExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (buffer.length >= tsPacketSize) {
                    for (int i = 0; i < buffer.length; i += tsPacketSize) {
                        i = seek(buffer, i);
                        if ((i == nil) || ((i + tsPacketSize) > buffer.length)) {
                            stop();
                            break;
                        }
                        parsePacket(Arrays.copyOfRange(buffer, i, i + tsPacketSize));
                        if (isParsed()) {
                            System.out.println("Analized packets for full parse: " + packetIndex);
                            System.out.println(parserTables.toString());
                            stop();
                            break;
                        }
                    }
                } else {
                    parsePacket(buffer);
                    if (isParsed()) {
                            System.out.println("Analized packets for full parse: " + packetIndex);
                            System.out.println(parserTables.toString());
                            stop();
                        }
                }
            }
        });
    }

    public boolean isParsed() {
        return (hasPAT && hasPMT && hasSDT && hasNIT
                && patParser.hasParsedFlag() && pmtParser.hasParsedFlag() && sdtParser.hasParsedFlag() && nitParser.hasParsedFlag());
    }

    public State getState() {
        return state;
    }

    public void start() {
        stop();
        reInitThis();
    }

    public void stop() {
        if (parseExecutorService != null) {
            parseExecutorService.shutdownNow();
        }
        state = State.STOPPED;
    }

    private int seek(byte[] buffer, int i) {
        for (; i < buffer.length - tsPacketSize; i++) {
            if (buffer[i] == syncByte && buffer[i + tsPacketSize] == syncByte) {
                return i;
            }
        }
        return nil;
    }

    private boolean parsePacket(byte[] packetBufferMain) {
        // пакет должен начинаться с байта синхронизации
        if (packetBufferMain[0] == syncByte) {
            //получение 4 байтов заголовка
            int header = headerParser.parseHeader(packetBufferMain);
            Packet packet = headerParser.analyzeHeader(Tools.toBinary(header, tsHeaderBinaryLength), packetBufferMain, packetIndex++);
            if (packet.getTransportErrorIndicator() == 1) {
                return true;
            }
            if (packet.getPID() <= tsMaxPidValue) {
                // анализ пакета
                if (!hasPAT) {
                    if (packet.getPID() == PATpid) {
                        patParser.parse(packet, parserTables);
                        hasPAT = patParser.getParserResult();
                    }
                } else {
                    if (hasAdaptationField(packet.getAdaptationFieldControl())) {
                        short adaptationFieldHeader = adaptationFieldParser.parseAdaptationFieldHeader(packet.getData());
                        byte[] binaryAdaptationFieldHeader = Tools.toBinary(adaptationFieldHeader, tsAdaptationFieldHeaderBinaryLength);
                        packet.setAdaptationFieldHeader(adaptationFieldParser.analyzeAdaptationFieldHeader(binaryAdaptationFieldHeader));
                    }
                    if (hasPayload(packet.getAdaptationFieldControl())) {
                        if (!hasPMT && parserTables.getPMTSet().contains(packet.getPID())) {
                            pmtParser.parse(packet, parserTables);
                            hasPMT = parserTables.getPMTSet().isEmpty();
                        } else if (isPSI(packet.getPID())) {
                            switch (packet.getPID()) {
                                case NITpid:
                                    if (!hasNIT) {
                                        nitParser.parse(packet, parserTables);
                                        hasNIT = nitParser.getParserResult();
                                    }
                                    break;
                                case SDTpid:
                                    if (!hasSDT && nitParser.hasParsedFlag()) {
                                        sdtParser.parse(packet, parserTables);
                                        hasSDT = parserTables.getTransportStreamSet().isEmpty();
                                    }
                                    break;
                                default:
                            }
                        }
                    }
                }

            }
        }

        return true;
    }

    public TSTableData getTables() {
        return this.parserTables;
    }

    public long getPacketIndex() {
        return packetIndex;
    }

}
