package ru.ts_parser.parser.psi;

import static ru.ts_parser.base.MpegCommonData.*;
import static ru.ts_parser.tools.Tools.binToInt;
import ru.ts_parser.entity.Tables;
import ru.ts_parser.entity.packet.Packet;
import ru.ts_parser.parser.DescriptorParser;
import ru.ts_parser.parser.ParserState;
import ru.ts_parser.base.PSIParserAbstract;

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
    protected void parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, Tables tables) {
        int networkID = (int) binToInt(sectionBinary, position = 0, position += networkIDlength);
  
//        byte versionNum = (byte) binToInt(sectionBinary, position += 2, position += versionNumLength);
        position+= 7;
        
        byte currentNextIndicator = sectionBinary[position++];
        if (currentNextIndicator != 1) {
            setParserState(null);
            return;
        }
        
//        short sectionNum = (short) binToInt(sectionBinary, position, position += sectionNumLength);
//        short lastSectionNum = (short) binToInt(sectionBinary, position, position += sectionNumLength);
        position += 16;

        short networkDescriptorsLength = (short) binToInt(sectionBinary, position += (reserved*2), position += twelveLengthLength);
        
        descriptor.loadDescriptors(networkID, sectionBinary, networkDescriptorsLength, position, tables);
        
        position += networkDescriptorsLength * byteBinaryLength;
        
        //short transportStreamLoopLength = (short) binToInt(sectionBinary, position += (reserved*2), position += twelveLengthLength);
        position += 16;

        int N = sectionLength * byteBinaryLength - CRClength;
        
        while (position < N) {
            int transportStreamID = (int) binToInt(sectionBinary, position, position += sixteenIDlength);
            int originalNetworkID = (int) binToInt(sectionBinary, position, position += sixteenIDlength);
            short transportDescriptiorsLoopLength = (short) binToInt(sectionBinary, position += (reserved*2), position += descriptorsLengthLength);
//            System.out.println("transportStreamID = " + transportStreamID + ", originalNetworkID = " + originalNetworkID);
            
            if ((transportDescriptiorsLoopLength *= byteBinaryLength) + position > (sectionLength * byteBinaryLength)) {
                break;
            }
            
            descriptor.loadDescriptors(transportStreamID, sectionBinary, transportDescriptiorsLoopLength, position, tables);
            position += transportDescriptiorsLoopLength;
        }

        fullPackageBuffer = null;
        setParserState(null);
    }

    @Override
    protected PSI_TABLE_TYPE getParserPSITableType() {
        return PSI_TABLE_TYPE.NIT;
    }

}
