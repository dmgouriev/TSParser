package ru.ts_parser.parser.psi;

import java.util.Arrays;
import static ru.ts_parser.MpegCommonData.*;
import static ru.ts_parser.Tools.binToInt;
import static ru.ts_parser.Tools.intToBinary;
import ru.ts_parser.model.Tables;
import ru.ts_parser.model.packet.Packet;
import ru.ts_parser.model.psi.PSI;
import ru.ts_parser.parser.Parser;
import ru.ts_parser.parser.ParserState;

/**
 *
 * @author dmgouriev
 */
public class PATParser extends Parser {
    
    public byte[] fullPackageBuffer = null;
    
    final int reserved = 2; //MPEG константа
    
    public ParserState parse(Packet packet, Tables tables, ParserState currentState) {
        ParserState newState = new ParserState(PSI_TABLE_TYPE.PAT, packet.getPID());
        
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
        
        int[] PATFields = parseNfields(packet.getData(), position, sectionLength);
        byte[] binaryPATFields = intToBinary(PATFields, sectionLength);
        int transportStreamID = (int) binToInt(binaryPATFields, position = 0, position += transportStreamIDlength);
        tables.updateCurrentTransportStreamID(transportStreamID); 
        
//        short versionNum = (short) binToInt(binaryPATFields, position += reserved, position += versionNumLength);
//        byte currentNextIndicator = binaryPATFields[position++];
//        int sectionNum = (int) binToInt(binaryPATFields, position, position += sectionNumLength);
//        int lastSectionNum = (int) binToInt(binaryPATFields, position, position += sectionNumLength);
        position += 24;
        
        int N = (sectionLength * byteBinaryLength) - CRClength ; //mandatoryPATfields; //длина следующего поля с циклом программных ассоциаций

        for (int i = 0; i < N; i += PATloopLength) { //цикл приобретения программных ассоциаций таблицы PAT
            if (N < position + PATloopLength) {
                break;
            }
            int programNum = (int) binToInt(binaryPATFields, position, position += programNumberLength); //получение номера программы
            if (programNum == 0) {
                // It also gives the location of the Network Information Table (NIT) on program num = 0
                position += 16;
                continue;
            }
            //получение PID пакета, содержащего таблицу PMT программы
            int programMapPID = (int) binToInt(binaryPATFields, position += 3, position += PCR_PIDlength);
            tables.getPATmap().put(programNum, programMapPID);
            
            tables.incrPMTSet(programMapPID);
            tables.incrSDTSet(programNum);
            
        }
        newState.setNeedContinue(false);
        fullPackageBuffer = null;
        return newState;
    }
    
}
