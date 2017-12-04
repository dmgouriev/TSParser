package ru.ts_parser;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static ru.ts_parser.MpegCommonData.*;
import ru.ts_parser.model.Tables;
import ru.ts_parser.model.packet.Packet;
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

    public static enum State {
        SUSPENDED,
        RUNNING,
        STOPPED
    }

    private final HeaderParser headerParser;
//    private final PSIparser psiParser;

    private final PATParser patParser;
    private final PMTParser pmtParser;
    private final SDTParser sdtParser;
    private final NITParser nitParser;

    private final AdaptationFieldParser adaptationFieldParser;
    private final ExecutorService parseExecutorService;

    private long packetIndex = 0;
    boolean isPATreceived = false;
    private State state;

    private Tables parserTables;

    public StreamParser() {
        headerParser = new HeaderParser();
        adaptationFieldParser = new AdaptationFieldParser();
//        psiParser = new PSIparser(tables);

        patParser = new PATParser();
        pmtParser = new PMTParser();
        sdtParser = new SDTParser();
        nitParser = new NITParser();

        packetIndex = 0;
        parserTables = new Tables();

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
                    }
                } else {
                    parsePacket(buffer);
                }
                System.out.println(new TSData(getTables()).getPMTString());
            }
        });
    }

    public State getState() {
        return state;
    }

    public void restart() {
        state = State.STOPPED;
    }

    public void stop() {
        parseExecutorService.shutdownNow();
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

            if (packet.getIndex() == 44702 || packet.getIndex() == 44703) {
                System.out.println(Tools.byteArrayToHex(packet.getData()) + "\n");
            }

            if (packet.getPID() <= tsMaxPidValue) {
                // анализ пакета
                if (!isPATreceived) {
                    if (packet.getPID() == PSI_TABLE_TYPE.PAT.PID) {
                        patParser.parse(packet, parserTables);
                        isPATreceived = patParser.getParserResult();
                    }
                } else {
                    if (hasAdaptationField(packet.getAdaptationFieldControl())) {
                        short adaptationFieldHeader = adaptationFieldParser.parseAdaptationFieldHeader(packet.getData());
                        byte[] binaryAdaptationFieldHeader = Tools.toBinary(adaptationFieldHeader, tsAdaptationFieldHeaderBinaryLength); //prevod na binárne pole
                        packet.setAdaptationFieldHeader(adaptationFieldParser.analyzeAdaptationFieldHeader(binaryAdaptationFieldHeader)); //analýza adaptačného poľa
                    }
                    if (hasPayload(packet.getAdaptationFieldControl())) {
                        if (parserTables.getPMTSet().contains(packet.getPID())) {
                            pmtParser.parse(packet, parserTables);
                        } else if (isPSI(packet.getPID())) {
                            switch (packet.getPID()) {
                                case NITpid:
                                    nitParser.parse(packet, parserTables);
                                    break;
                                case SDTpid:
                                    sdtParser.parse(packet, parserTables);
                                    break;
                                default:
                            }
                        }

//                        if (psiParser.isPSI(packet.getPID())) {
//                            psiParser.analyzePSI(packet);
//                            updateTables(psiParser);
//                        } else if (psiParser.isPMT(packet.getPID())) {
//                            PAT_PIDS.remove(packet.getPID());
//                            if (psiParser.analyzePMT(packet) != null) {
//                                updateTables(psiParser);
//                            }
//                        }
                    }
                }
            }
        }

        return true;
    }

//    private void updateTables(Parser parser) {
//
//        if (parser instanceof PSIparser) {
//            tables.setPMTnumber(parser.tables.getPMTnumber());
//            tables.setPATmap(parser.tables.getPATmap());
//            tables.setPMTmap(parser.tables.getPMTmap());
//            tables.setESmap(parser.tables.getESmap());
//            tables.setProgramNameMap(parser.tables.getProgramNameMap());
//            tables.setServiceNamesMap(parser.tables.getServiceNamesMap());
//            tables.setServiceNamesMap(parser.tables.getServiceNamesMap());
//            tables.setPCRpmtMap(parser.tables.getPCRpmtMap());
//        }
//    }
    public Tables getTables() {
        return this.parserTables;
    }

    public long getPacketIndex() {
        return packetIndex;
    }

}
