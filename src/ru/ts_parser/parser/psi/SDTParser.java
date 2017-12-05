package ru.ts_parser.parser.psi;

import static ru.ts_parser.MPEGConstant.*;
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
        int transportStreamID = (int) binToInt(sectionBinary, position = 0, position += serviceIDlength);
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

        int N = sectionLength * byteBinaryLength - CRClength;

        while (position < N) {
            int serviceID = (int) binToInt(sectionBinary, position, position += networkIDlength);

            /*
            byte EITscheduleFlag = sectionBinary[position += 6];
            byte EITpresentFollowingFlag = sectionBinary[position += 1];
            byte runningStatus = (byte) binToInt(sectionBinary, position += 1, position += runningStatusLength);
             */
            position += 11;

            byte freeCAmode = sectionBinary[position++];
            tables.updateProgramFreeCAModeMap(serviceID, freeCAmode);
            tables.updateProgramTransportStreamMap(serviceID, transportStreamID);
            short descriptorsLoopLength = (short) binToInt(sectionBinary, position, position += descriptorsLengthLength);

            if ((descriptorsLoopLength *= byteBinaryLength) + position > (sectionLength * byteBinaryLength)) {
                break;
            }

            descriptor.parse(serviceID, sectionBinary, descriptorsLoopLength, position, tables);
            position += descriptorsLoopLength;
        }

        tables.decrTransportStreamSet(transportStreamID);
        setParsedFlag();

        clearPacketBuffer();
    }

}
