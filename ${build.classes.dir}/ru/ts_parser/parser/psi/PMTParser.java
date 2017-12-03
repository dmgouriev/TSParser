package ru.ts_parser.parser.psi;

import java.util.HashMap;
import java.util.Map;
import static ru.ts_parser.MpegCommonData.*;
import static ru.ts_parser.Tools.binToInt;
import ru.ts_parser.model.Tables;
import ru.ts_parser.model.packet.Packet;
import ru.ts_parser.parser.ParserState;
import ru.ts_parser.parser.psi.base.PSIParserAbstract;

/**
 *
 * @author dmgouriev
 */
public class PMTParser extends PSIParserAbstract {
    
    final int reserved = 2; //MPEG константа
    
//    public ParserState parse(Packet packet, Tables tables, ParserState currentState) {
//        ParserState newState = new ParserState(PSI_TABLE_TYPE.PMT, packet.getPID());
//
//        if (fullPackageBuffer != null) {
//            fullPackageBuffer = merge(fullPackageBuffer, Arrays.copyOfRange(packet.getData(), tsHeaderSize, tsPacketSize));
//            packet.updateData(fullPackageBuffer);
//        }
//
//        int position = calculatePosition(packet) + 1; //определение начальной позиции на основе длины предыдущих заголовков
//        PSI psiCommonFields = analyzePSICommonFields(packet.getData(), position); //анализ общих полей таблиц PSI, т. е. tableID, SSI и sectionLength
//        position += PSIcommonFieldsLength; //обновление позиции после получения общих полей
//
//        int sectionLength = psiCommonFields.getSectionLength(); 
//
//        if ((sectionLength + position + CRClengthByte) > packet.getData().length) {
//            fullPackageBuffer = packet.getData();
//            newState.setNeedContinue(true);
//            return newState;
//        }
//        
//        int[] PMTFields = parseNfields(packet.getData(), position, sectionLength);
//        byte[] sectionBinary = intToBinary(PMTFields, sectionLength);
//        
//        int programNum = (int) binToInt(sectionBinary, position = 0, position += programNumberLength);
//        /*
//        byte versionNum = (byte) binToInt(sectionBinary, position += reserved, position += versionNumLength);
//        byte currentNextIndicator = sectionBinary[position++];
//        byte sectionNum = (byte) binToInt(sectionBinary, position, position += sectionNumLength);
//        byte lastSectionNum = (byte) binToInt(sectionBinary, position, position += sectionNumLength);
//        short PCR_PID = (short) binToInt(sectionBinary, position += reserved + 1, position += PCR_PIDlength);
//        */
//        position += 40;
//        
//        short programInfoLength = (short) binToInt(sectionBinary, position += (reserved*2), position += programInfoLengthLength);
//
//        int nLoopDescriptorsLength = programInfoLength * byteBinaryLength;
//        position += nLoopDescriptorsLength;
//
//        Map<Integer, Integer> ESmap = new HashMap<>();
//        Map<Integer, Integer> PMTmap = new HashMap<>();
//        int N = (sectionLength * byteBinaryLength) - CRClength;
//
//        for (; position < N;) {
//            int streamType = (int) binToInt(sectionBinary, position, position += streamTypeLength);
//            int elementaryPID = (int) binToInt(sectionBinary, position += 3, position += elementaryPIDlength);
//            int ESinfoLength = (int) binToInt(sectionBinary, position += 4, position += ESinfoLengthLength);
//            position += ESinfoLength * byteBinaryLength;
//            ESmap.put(elementaryPID, streamType);
//            tables.getPMTmap().put(elementaryPID, programNum);
//        }        
//        
//        tables.updateES(ESmap);
//        tables.updatePMT(PMTmap);
//        tables.decrPMTSet(packet.getPID());
//        
//        newState.setNeedContinue(false);
//        fullPackageBuffer = null;
//        return newState;
//    }

    @Override
    protected ParserState parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, ParserState newState, Tables tables) {
        int programNum = (int) binToInt(sectionBinary, position = 0, position += programNumberLength);
        /*
        byte versionNum = (byte) binToInt(sectionBinary, position += reserved, position += versionNumLength);
        byte currentNextIndicator = sectionBinary[position++];
        byte sectionNum = (byte) binToInt(sectionBinary, position, position += sectionNumLength);
        byte lastSectionNum = (byte) binToInt(sectionBinary, position, position += sectionNumLength);
        short PCR_PID = (short) binToInt(sectionBinary, position += reserved + 1, position += PCR_PIDlength);
        */
        position += 40;
        
        short programInfoLength = (short) binToInt(sectionBinary, position += (reserved*2), position += programInfoLengthLength);

        int nLoopDescriptorsLength = programInfoLength * byteBinaryLength;
        position += nLoopDescriptorsLength;

        Map<Integer, Integer> ESmap = new HashMap<>();
        Map<Integer, Integer> PMTmap = new HashMap<>();
        int N = (sectionLength * byteBinaryLength) - CRClength;

        for (; position < N;) {
            int streamType = (int) binToInt(sectionBinary, position, position += streamTypeLength);
            int elementaryPID = (int) binToInt(sectionBinary, position += 3, position += elementaryPIDlength);
            int ESinfoLength = (int) binToInt(sectionBinary, position += 4, position += ESinfoLengthLength);
            position += ESinfoLength * byteBinaryLength;
            ESmap.put(elementaryPID, streamType);
            tables.getPMTmap().put(elementaryPID, programNum);
        }        
        
        tables.updateES(ESmap);
        tables.updatePMT(PMTmap);
        tables.decrPMTSet(packet.getPID());
        
        newState.setNeedContinue(false);
        fullPackageBuffer = null;
        return newState;
    }

    @Override
    protected PSI_TABLE_TYPE getParserPSITableType() {
        return PSI_TABLE_TYPE.PMT;
    }
    
}
