package ru.ts_parser.parser.psi;

import java.util.Arrays;
import static ru.ts_parser.MpegCommonData.*;
import ru.ts_parser.Tools;
import static ru.ts_parser.Tools.binToInt;
import static ru.ts_parser.Tools.intToBinary;
import ru.ts_parser.model.Tables;
import ru.ts_parser.model.packet.Packet;
import ru.ts_parser.model.psi.PSI;
import ru.ts_parser.parser.DescriptorParser;
import ru.ts_parser.parser.Parser;
import ru.ts_parser.parser.ParserState;

/**
 *
 * @author dmgouriev
 */
public class SDTParser extends Parser {

    public byte[] fullPackageBuffer = null;
    
    private final DescriptorParser descriptor;
    
    public SDTParser() {
        descriptor = new DescriptorParser();
    }
    
    
    public ParserState parse(Packet packet, Tables tables, ParserState currentState) {
        
        Tools.byteArrayToHex(packet.getData());
        
        ParserState newState = new ParserState(PSI_TABLE_TYPE.SDT, packet.getPID());

        if (fullPackageBuffer != null) {
            fullPackageBuffer = merge(fullPackageBuffer, Arrays.copyOfRange(packet.getData(), tsHeaderSize, tsPacketSize));
            packet.updateData(fullPackageBuffer);
        }

        int position = calculatePosition(packet) + 1; //определение начальной позиции на основе длины предыдущих заголовков
        PSI psiCommonFields = analyzePSICommonFields(packet.getData(), position); //анализ общих полей таблиц PSI, т. е. tableID, SSI и sectionLength
        position += PSIcommonFieldsLength; //обновление позиции после получения общих полей

        int sectionLength = psiCommonFields.getSectionLength(); 

        if ((sectionLength + position + CRClengthByte) > packet.getData().length) {
            fullPackageBuffer = packet.getData();
            newState.setNeedContinue(true);
            return newState;
        }

        int[] SDTfields = parseNfields(packet.getData(), position, sectionLength);
        byte[] binarySDTfields = intToBinary(SDTfields, sectionLength);

        int transportStreamID = (int) binToInt(binarySDTfields, position = 0, position += serviceIDlength);
        tables.updateTransportStreamIdSet(transportStreamID);
        /*
        byte versionNum = (byte) binToInt(binarySDTfields, position += 2, position += versionNumLength);
        byte currentNextIndicator = binarySDTfields[position++];
        short sectionNum = (short) binToInt(binarySDTfields, position, position += sectionNumLength);
        short lastSectionNum = (short) binToInt(binarySDTfields, position, position += sectionNumLength);
        */
        
        position += 24;
        
        int originalNetworkID = (int) binToInt(binarySDTfields, position, position += networkIDlength);

        position += 8;
        
        int N = sectionLength * byteBinaryLength - CRClength;

        while (position < N) {
            int serviceID = (int) binToInt(binarySDTfields, position, position += networkIDlength);
            /*
            byte EITscheduleFlag = binarySDTfields[position += 6];
            byte EITpresentFollowingFlag = binarySDTfields[position += 1];
            byte runningStatus = (byte) binToInt(binarySDTfields, position += 1, position += runningStatusLength);
            */
            
            position += 11;
            
            byte freeCAmode = binarySDTfields[position++];

            short descriptorsLoopLength = (short) binToInt(binarySDTfields, position, position += descriptorsLengthLength);

            if ((descriptorsLoopLength *= byteBinaryLength) + position > (sectionLength * byteBinaryLength)) {
                break;
            }

            descriptor.loadDescriptors(serviceID, binarySDTfields, descriptorsLoopLength, position, tables);
            position += descriptorsLoopLength;
        }

        newState.setNeedContinue(false);
        fullPackageBuffer = null;
        return newState;
    }

}
