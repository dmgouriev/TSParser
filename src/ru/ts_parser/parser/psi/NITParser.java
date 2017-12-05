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
public class NITParser extends PSIParserAbstract {

    private final DescriptorParser descriptor;

    final int reserved = 2; //MPEG константа

    public NITParser() {
        descriptor = new DescriptorParser();
    }

    @Override
    protected void parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, short tableID, TSTableData tables) {
        if (tableID == 0x40) { // actual network table_id = 0x40, other networks table_id = 0x41
            int networkID = (int) binToInt(sectionBinary, position = 0, position += networkIDlength);
//            byte versionNum = (byte) binToInt(sectionBinary, position += 2, position += versionNumLength);
            position += 7;

            byte currentNextIndicator = sectionBinary[position++];
            if (currentNextIndicator != 1) {
                clearPacketBuffer();
                return;
            }

//            short sectionNum = (short) binToInt(sectionBinary, position, position += sectionNumLength);
//            short lastSectionNum = (short) binToInt(sectionBinary, position, position += sectionNumLength);
            position += 16;

            short networkDescriptorsLength = (short) binToInt(sectionBinary, position += (reserved * 2), position += twelveLengthLength);

            descriptor.parse(networkID, sectionBinary, networkDescriptorsLength, position, tables);

            position += networkDescriptorsLength * byteBinaryLength;

            //short transportStreamLoopLength = (short) binToInt(sectionBinary, position += (reserved*2), position += twelveLengthLength);
            position += 16;

            int N = sectionLength * byteBinaryLength - CRClength;

            while (position < N) {
                int transportStreamID = (int) binToInt(sectionBinary, position, position += sixteenIDlength);
                tables.incrTransportStreamSet(transportStreamID);
//                int originalNetworkID = (int) binToInt(sectionBinary, position, position += sixteenIDlength);
                position += sixteenIDlength;
                short transportDescriptiorsLoopLength = (short) binToInt(sectionBinary, position += (reserved * 2), position += descriptorsLengthLength);
                if ((transportDescriptiorsLoopLength *= byteBinaryLength) + position > (sectionLength * byteBinaryLength)) {
                    break;
                }
                descriptor.parse(transportStreamID, sectionBinary, transportDescriptiorsLoopLength, position, tables);
                position += transportDescriptiorsLoopLength;
            }
            setParserResult(true);
            setParsedFlag();
        }
        clearPacketBuffer();
    }

    @Override
    protected PSI_TABLE_TYPE getParserPSITableType() {
        return PSI_TABLE_TYPE.NIT;
    }

}
