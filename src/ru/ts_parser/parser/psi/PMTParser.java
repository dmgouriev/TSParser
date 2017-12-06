package ru.ts_parser.parser.psi;

import static ru.ts_parser.MPEGConstant.*;
import static ru.ts_parser.tools.Tools.binToInt;
import ru.ts_parser.TSTableData;
import ru.ts_parser.entity.Packet;

/**
 *
 * @author dmgouriev
 */
public class PMTParser extends PSIParserAbstract {

    @Override
    protected void parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, short tableID, TSTableData tables) {
        int programNum = (int) binToInt(sectionBinary, position = 0, position += BITS_LEN_16);

//        byte versionNum = (byte) binToInt(sectionBinary, position += BITS_LEN_2, position += versionNumLength);        
        position += 7;

        byte currentNextIndicator = sectionBinary[position++];
        if (currentNextIndicator != 1) {
            clearPacketBuffer();
            return;
        }

//        byte sectionNum = (byte) binToInt(sectionBinary, position, position += sectionNumLength);
//        byte lastSectionNum = (byte) binToInt(sectionBinary, position, position += sectionNumLength);
//        short PCR_PID = (short) binToInt(sectionBinary, position += BITS_LEN_2 + 1, position += PCR_PIDlength);
        position += 32;

        short programInfoLength = (short) binToInt(sectionBinary, position += BITS_LEN_4, position += BITS_LEN_12);

        int nLoopDescriptorsLength = programInfoLength * BYTE_BITS_LEN;
        position += nLoopDescriptorsLength;

        int N = (sectionLength * BYTE_BITS_LEN) - BITS_LEN_32;
        for (; position < N;) {
            int streamType = (int) binToInt(sectionBinary, position, position += BITS_LEN_8);
            int elementaryPID = (int) binToInt(sectionBinary, position += 3, position += BITS_LEN_13);
            int ESinfoLength = (int) binToInt(sectionBinary, position += 4, position += BITS_LEN_12);
            position += ESinfoLength * BYTE_BITS_LEN;
            tables.updateESMap(elementaryPID, streamType);
            tables.updatePMTMap(elementaryPID, programNum);
        }
        tables.decrPMTSet(packet.getPID());

        clearPacketBuffer();
        setParsedFlag();
    }

}
