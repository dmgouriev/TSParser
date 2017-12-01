package ru.ts_parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static ru.ts_parser.MpegCommonData.*;
import ru.ts_parser.model.Tables;
import ru.ts_parser.model.packet.Packet;
import ru.ts_parser.parser.header.AdaptationFieldParser;
import ru.ts_parser.parser.header.HeaderParser;
import ru.ts_parser.parser.Parser;
import ru.ts_parser.parser.ParserState;
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

    private final AdaptationFieldParser adaptationFieldParser;
    private final ExecutorService parseExecutorService;

    private final Set<Integer> PAT_PMT_PIDS = new HashSet<>();

    private long packetIndex = 0;
    boolean isPATreceived = false;
    private State state;

    private ParserState parserState;
    private Tables parserTables;

    public StreamParser() {
        headerParser = new HeaderParser();
        adaptationFieldParser = new AdaptationFieldParser();
//        psiParser = new PSIparser(tables);

        patParser = new PATParser();
        pmtParser = new PMTParser();
        sdtParser = new SDTParser();

        packetIndex = 0;
        parserState = null;
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

            if (packet.getPID() <= tsMaxPidValue) {
                // анализ пакета
                if (!isPATreceived) {
                    parserState = parsePAT(packet);
                } else {
                    if (hasAdaptationField(packet.getAdaptationFieldControl())) {
                        short adaptationFieldHeader = adaptationFieldParser.parseAdaptationFieldHeader(packet.getData());
                        byte[] binaryAdaptationFieldHeader = Tools.toBinary(adaptationFieldHeader, tsAdaptationFieldHeaderBinaryLength); //prevod na binárne pole
                        packet.setAdaptationFieldHeader(adaptationFieldParser.analyzeAdaptationFieldHeader(binaryAdaptationFieldHeader)); //analýza adaptačného poľa
                    }
                    if (hasPayload(packet.getAdaptationFieldControl())) {
                        if (parserTables.getPMTSet().contains(packet.getPID())) {

                            parserState = pmtParser.parse(packet, parserTables, parserState);

                        } else if (isPSI(packet.getPID()) || (parserState!= null && isPSI(parserState.getLastPID()) && parserState.needContinue())) {

                            int pid = packet.getPID();
                            if (parserState != null && parserState.needContinue()) {
                                pid = parserState.getType().PID;
                            }
                            switch (pid) {
                                case NITpid:
                                    parserState = null;
                                    break;
                                case SDTpid:
                                    parserState = sdtParser.parse(packet, parserTables, parserState);
                                    break;
                                default:
                                    parserState = null;
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

    public ParserState parsePAT(Packet packet) {
        if (isContinuePacket(PSI_TABLE_TYPE.PAT) || (hasPayload(packet.getAdaptationFieldControl()) && packet.getPID() == PSI_TABLE_TYPE.PAT.PID)) {
            ParserState result = patParser.parse(packet, parserTables, parserState);
            if (!result.needContinue()) {
                isPATreceived = true;
            }
            return result;
//            parserState = psiParser.analyzePAT(packet)
//            if (psiParser.analyzePAT(packet) != null) {
//                updateTables(psiParser);
//                isPATreceived = true;
//                Map<Integer, Integer> PATmap = tables.getPATmap();
//                for (int progId : PATmap.keySet()) {
//                    PAT_PMT_PIDS.add(PATmap.get(progId));
//                }
//                PAT_PMT_PIDS.remove((Integer) 0);
//                System.out.println(tables.getPATmap());
//            }
        }
        return null;
    }

    public boolean isContinuePacket(PSI_TABLE_TYPE type) {
        return (parserState == null ? false : ((parserState.needContinue() && parserState.getType() == type)));
    }

    public boolean isFinished() {
        return PAT_PMT_PIDS.isEmpty();
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
