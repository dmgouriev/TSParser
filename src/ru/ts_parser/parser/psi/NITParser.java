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
public class NITParser extends PSIParserAbstract {

    private final DescriptorParser descriptor;

    public NITParser() {
        descriptor = new DescriptorParser();
    }

    @Override
    protected void parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, short tableID, TSTableData tables) {
        if (tableID == NIT_CURRENT_NETWORK_TABLE_ID) { // actual network table_id = 0x40, other networks table_id = 0x41
            int networkID = (int) binToInt(sectionBinary, position = 0, position += BITS_LEN_16);
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

            short networkDescriptorsLength = (short) binToInt(sectionBinary, position += BITS_LEN_4, position += BITS_LEN_12);

            descriptor.parse(networkID, sectionBinary, networkDescriptorsLength, position, tables);

            position += networkDescriptorsLength * BYTE_BITS_LEN;

            //short transportStreamLoopLength = (short) binToInt(sectionBinary, position += BITS_LEN_4, position += twelveLengthLength);
            position += 16;

            int N = sectionLength * BYTE_BITS_LEN - BITS_LEN_32;

            while (position < N) {
                int transportStreamID = (int) binToInt(sectionBinary, position, position += BITS_LEN_16);
                tables.incrTransportStreamSet(transportStreamID);
//                int originalNetworkID = (int) binToInt(sectionBinary, position, position += sixteenIDlength);
                position += BITS_LEN_16;
                short transportDescriptiorsLoopLength = (short) binToInt(sectionBinary, position += BITS_LEN_4, position += BITS_LEN_12);
                if ((transportDescriptiorsLoopLength *= BYTE_BITS_LEN) + position > (sectionLength * BYTE_BITS_LEN)) {
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

}
