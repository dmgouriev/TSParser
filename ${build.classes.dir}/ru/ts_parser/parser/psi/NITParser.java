package ru.ts_parser.parser.psi;

import java.util.Arrays;
import static ru.ts_parser.MpegCommonData.*;
import ru.ts_parser.Tools;
import static ru.ts_parser.Tools.binToInt;
import static ru.ts_parser.Tools.intToBinary;
import ru.ts_parser.model.Tables;
import ru.ts_parser.model.packet.Packet;
import ru.ts_parser.model.packet.psi.PSI;
import ru.ts_parser.parser.DescriptorParser;
import ru.ts_parser.parser.Parser;
import ru.ts_parser.parser.ParserState;
import ru.ts_parser.parser.psi.base.PSIParserAbstract;

/**
 *
 * @author dmgouriev
 */
public class NITParser extends PSIParserAbstract { 
    
    @Override
    protected ParserState parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, ParserState newState, Tables tables) {
        int transportStreamID = (int) binToInt(sectionBinary, position = 0, position += serviceIDlength);
        tables.updateTransportStreamIdSet(transportStreamID);
        /*
        byte versionNum = (byte) binToInt(sectionBinary, position += 2, position += versionNumLength);
        byte currentNextIndicator = sectionBinary[position++];
        short sectionNum = (short) binToInt(sectionBinary, position, position += sectionNumLength);
        short lastSectionNum = (short) binToInt(sectionBinary, position, position += sectionNumLength);
        */
        position += 24;
        
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

            short descriptorsLoopLength = (short) binToInt(sectionBinary, position, position += descriptorsLengthLength);

            if ((descriptorsLoopLength *= byteBinaryLength) + position > (sectionLength * byteBinaryLength)) {
                break;
            }

//            descriptor.loadDescriptors(serviceID, sectionBinary, descriptorsLoopLength, position, tables);
//            position += descriptorsLoopLength;
        }

        newState.setNeedContinue(false);
        fullPackageBuffer = null;
        return newState;
    }

    @Override
    protected PSI_TABLE_TYPE getParserPSITableType() {
        return PSI_TABLE_TYPE.NIT;
    }

}
