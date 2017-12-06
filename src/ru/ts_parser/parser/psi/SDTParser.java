package ru.ts_parser.parser.psi;

import static ru.ts_parser.Constant.*;
import static ru.ts_parser.tools.Tools.binToInt;
import ru.ts_parser.TSTableData;
import ru.ts_parser.entity.Packet;
import ru.ts_parser.parser.DescriptorParser;

/**
 *
 * @author dmgouriev
 */
public class SDTParser extends PSIParserAbstract {

    private final DescriptorParser descriptor;

    public SDTParser() {
        descriptor = new DescriptorParser();
    }

    @Override
    protected void parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, short tableID, TSTableData tables) {
        if (tableID == SDT_CURRENT_STREAM_TABLE_ID || tableID == SDT_OTHER_STREAM_TABLE_ID) {
            int transportStreamID = (int) binToInt(sectionBinary, position = 0, position += BITS_LEN_16);
//        byte versionNum = (byte) binToInt(sectionBinary, position += 2, position += versionNumLength);
            position += 7;

            byte currentNextIndicator = sectionBinary[position++];
            if (currentNextIndicator != 1) {
                clearPacketBuffer();
                return;
            }

//        short sectionNum = (short) binToInt(sectionBinary, position, position += sectionNumLength);
//        short lastSectionNum = (short) binToInt(sectionBinary, position, position += sectionNumLength);
            position += 16;

            //int originalNetworkID = (int) binToInt(sectionBinary, position, position += networkIDlength);
            position += 16;

            position += 8;

            int N = sectionLength * BYTE_BITS_LEN - BITS_LEN_32;

            while (position < N) {
                int serviceID = (int) binToInt(sectionBinary, position, position += BITS_LEN_16);

                /*
            byte EITscheduleFlag = sectionBinary[position += 6];
            byte EITpresentFollowingFlag = sectionBinary[position += 1];
            byte runningStatus = (byte) binToInt(sectionBinary, position += 1, position += runningStatusLength);
                 */
                position += 11;

                byte freeCAmode = sectionBinary[position++];
                tables.updateProgramFreeCAModeMap(serviceID, freeCAmode);
                tables.updateProgramTransportStreamMap(serviceID, transportStreamID);
                short descriptorsLoopLength = (short) binToInt(sectionBinary, position, position += BITS_LEN_12);

                if ((descriptorsLoopLength *= BYTE_BITS_LEN) + position > (sectionLength * BYTE_BITS_LEN)) {
                    break;
                }

                descriptor.parse(serviceID, sectionBinary, descriptorsLoopLength, position, tables);
                position += descriptorsLoopLength;
            }
            tables.decrTransportStreamSet(transportStreamID);
            setParsedFlag();
        }
        clearPacketBuffer();
    }

}
