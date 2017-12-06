package ru.ts_parser;

import ru.ts_parser.tools.Tools;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static ru.ts_parser.Constant.*;
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
    private TSTableData tsTableData;

    private Timer parserTimer;

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
        tsTableData = new TSTableData();

        this.parseExecutorService = Executors.newFixedThreadPool(1);
        this.state = State.SUSPENDED;
    }

    public void parse(final byte[] buffer) {
        if (state == State.STOPPED) {
            return;
        }
        if (state == State.SUSPENDED) {
            state = State.RUNNING;
            parserTimer = new Timer();
            parserTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                   stop();
                }
            }, Constant.MAX_PARSE_TIME_SECS * 1000);
        }
        parseExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (buffer.length >= PACKET_SIZE) {
                    for (int i = 0; i < buffer.length; i += PACKET_SIZE) {
                        i = seek(buffer, i);
                        if ((i == NIL) || ((i + PACKET_SIZE) > buffer.length)) {
                            stop();
                            break;
                        }
                        parsePacket(Arrays.copyOfRange(buffer, i, i + PACKET_SIZE));
                        if (isParsed()) {
                            stop();
                            break;
                        }
                    }
                } else {
                    parsePacket(buffer);
                    if (isParsed()) {
                        stop();
                    }
                }
            }
        });
    }

    public TSTableData getResult() {
        return tsTableData;
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
        for (; i < buffer.length - PACKET_SIZE; i++) {
            if (buffer[i] == SYNC_BYTE && buffer[i + PACKET_SIZE] == SYNC_BYTE) {
                return i;
            }
        }
        return NIL;
    }

    private boolean parsePacket(byte[] packetBufferMain) {
        // пакет должен начинаться с байта синхронизации
        if (packetBufferMain[0] == SYNC_BYTE) {
            //получение 4 байтов заголовка
            int header = headerParser.parseHeader(packetBufferMain);
            Packet packet = headerParser.analyzeHeader(Tools.toBinary(header, HEADER_BITS_LEN), packetBufferMain, packetIndex++);
            if (packet.getTransportErrorIndicator() == 1) {
                return true;
            }
            if (packet.getPID() <= MAX_PID_VALUE) {
                // анализ пакета
                if (!hasPAT) {
                    if (packet.getPID() == PID_PAT_VALUE) {
                        patParser.parse(packet, tsTableData);
                        hasPAT = patParser.getParserResult();
                    }
                } else {
                    if (hasAdaptationField(packet.getAdaptationFieldControl())) {
                        short adaptationFieldHeader = adaptationFieldParser.parseAdaptationFieldHeader(packet.getData());
                        byte[] binaryAdaptationFieldHeader = Tools.toBinary(adaptationFieldHeader, ADAPT_FIELD_HEADER_BITS_LEN);
                        packet.setAdaptationFieldHeader(adaptationFieldParser.analyzeAdaptationFieldHeader(binaryAdaptationFieldHeader));
                    }
                    if (hasPayload(packet.getAdaptationFieldControl())) {
                        if (!hasPMT && tsTableData.getPMTSet().contains(packet.getPID())) {
                            pmtParser.parse(packet, tsTableData);
                            hasPMT = tsTableData.getPMTSet().isEmpty();
                        } else if (packet.getPID() <= PSI_MAX_PID_VALUE) {
                            switch (packet.getPID()) {
                                case PID_NIT_VALUE:
                                    if (!hasNIT) {
                                        nitParser.parse(packet, tsTableData);
                                        hasNIT = nitParser.getParserResult();
                                    }
                                    break;
                                case PID_SDT_VALUE:
                                    if (!hasSDT && nitParser.hasParsedFlag()) {
                                        sdtParser.parse(packet, tsTableData);
                                        hasSDT = tsTableData.getTransportStreamSet().isEmpty();
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
        return this.tsTableData;
    }

    public long getPacketIndex() {
        return packetIndex;
    }

}
