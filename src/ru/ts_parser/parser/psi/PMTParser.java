package ru.ts_parser.parser.psi;

import java.util.HashMap;
import java.util.Map;
import static ru.ts_parser.MPEGConstant.*;
import static ru.ts_parser.tools.Tools.binToInt;
import ru.ts_parser.TSTableData;
import ru.ts_parser.entity.Packet;

/**
 *
 * @author dmgouriev
 */
public class PMTParser extends PSIParserAbstract {
    
    final int reserved = 2; //MPEG константа

    @Override
    protected void parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, short tableID, TSTableData tables) {
        int programNum = (int) binToInt(sectionBinary, position = 0, position += programNumberLength);

//        byte versionNum = (byte) binToInt(sectionBinary, position += reserved, position += versionNumLength);        
        position += 7;

        byte currentNextIndicator = sectionBinary[position++];
        if (currentNextIndicator != 1) {
clearPacketBuffer();
            return;
        }
        
//        byte sectionNum = (byte) binToInt(sectionBinary, position, position += sectionNumLength);
//        byte lastSectionNum = (byte) binToInt(sectionBinary, position, position += sectionNumLength);
//        short PCR_PID = (short) binToInt(sectionBinary, position += reserved + 1, position += PCR_PIDlength);
        position += 32;
        
        short programInfoLength = (short) binToInt(sectionBinary, position += (reserved*2), position += twelveLengthLength);

        int nLoopDescriptorsLength = programInfoLength * byteBinaryLength;
        position += nLoopDescriptorsLength;

        int N = (sectionLength * byteBinaryLength) - CRClength;
        for (; position < N;) {
            int streamType = (int) binToInt(sectionBinary, position, position += streamTypeLength);
            int elementaryPID = (int) binToInt(sectionBinary, position += 3, position += elementaryPIDlength);
            int ESinfoLength = (int) binToInt(sectionBinary, position += 4, position += twelveLengthLength);
            position += ESinfoLength * byteBinaryLength;
            tables.updateESMap(elementaryPID, streamType);
            tables.updatePMTMap(elementaryPID, programNum);
        }        
        tables.decrPMTSet(packet.getPID());

        clearPacketBuffer();
        setParsedFlag();
    }

    @Override
    protected PSI_TABLE_TYPE getParserPSITableType() {
        return PSI_TABLE_TYPE.PMT;
    }
    
}
