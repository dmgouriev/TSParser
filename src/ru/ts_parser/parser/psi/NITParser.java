package ru.ts_parser.parser.psi;

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
public class NITParser extends PSIParserAbstract { 
    
//    network_information_section(){
//        table_id    8
//        section_syntax_indicator    1
//        reserved_future_use 1
//        reserved    2
//        section_length  12
//        network_id  16
//        reserved    2
//        version_number  5
//        current_next_indicator  1
//        section_number  8
//        last_sectionnumber  8
//        reserved_future_use 4
//        network_descriptors_length  12
//        for(i=0;i<N;i++){
//            descriptor()
//        }
//        reserved_future_use     4
//        transport_stream_loop_length    12
//        for(i=0;i<N;i++){
//            transport_stream_id     16
//            original_network_id     16
//            reserved_future_use     4
//            transport_descriptors_length    12
//            for(j=0;j<N;j++){
//                descriptor()
//            }
//        }
//        CRC_32  32
//    }

    final int reserved = 2; //MPEG константа
    
    @Override
    protected ParserState parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, ParserState newState, Tables tables) {
        int networkID = (int) binToInt(sectionBinary, position = 0, position += networkIDlength);
        
        /*
        byte versionNum = (byte) binToInt(sectionBinary, position += 2, position += versionNumLength);
        byte currentNextIndicator = sectionBinary[position++];
        short sectionNum = (short) binToInt(sectionBinary, position, position += sectionNumLength);
        short lastSectionNum = (short) binToInt(sectionBinary, position, position += sectionNumLength);
        */
        position += 24;

        short networkDescriptorsLength = (short) binToInt(sectionBinary, position += (reserved*2), position += twelveLengthLength);
        
        int N = sectionLength * byteBinaryLength - CRClength;

        while (position < N) {
            short transportStreamLoopLength = (short) binToInt(sectionBinary, position += (reserved*2), position += twelveLengthLength);
            
            int transportStreamID = (int) binToInt(sectionBinary, position, position += sixteenIDlength);
            int originalNetworkID = (int) binToInt(sectionBinary, position, position += sixteenIDlength);
            short descriptorsLoopLength = (short) binToInt(sectionBinary, position += (reserved*2), position += descriptorsLengthLength);

            System.out.println("transportStreamID = " + transportStreamID + ", originalNetworkID = " + originalNetworkID);
            
//            for(j=0;j<N;j++){
//                descriptor()
//            }
//        }
            
//            int serviceID = (int) binToInt(sectionBinary, position, position += networkIDlength);
//            /*
//            byte EITscheduleFlag = sectionBinary[position += 6];
//            byte EITpresentFollowingFlag = sectionBinary[position += 1];
//            byte runningStatus = (byte) binToInt(sectionBinary, position += 1, position += runningStatusLength);
//            */
//            
//            position += 11;
//            
//            byte freeCAmode = sectionBinary[position++];
//
//            short descriptorsLoopLength = (short) binToInt(sectionBinary, position, position += descriptorsLengthLength);
//
//            if ((descriptorsLoopLength *= byteBinaryLength) + position > (sectionLength * byteBinaryLength)) {
//                break;
//            }
//
////            descriptor.loadDescriptors(serviceID, sectionBinary, descriptorsLoopLength, position, tables);
////            position += descriptorsLoopLength;
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
