package ru.ts_parser.parser.psi;

import static ru.ts_parser.MPEGConstant.*;
import static ru.ts_parser.tools.Tools.binToInt;
import ru.ts_parser.TSTableData;
import ru.ts_parser.entity.Packet;

/**
 *
 * @author dmgouriev
 */
public class PATParser extends PSIParserAbstract {

    @Override
    protected void parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, short tableID, TSTableData tables) {
        int transportStreamID = (int) binToInt(sectionBinary, position = 0, position += BITS_LEN_16);
        tables.setCurrentTransportStreamID(transportStreamID);

//        short versionNum = (short) binToInt(sectionBinary, position += BITS_LEN_2, position += versionNumLength);
        position += 7;

        byte currentNextIndicator = sectionBinary[position++];
        if (currentNextIndicator != 1) {
            clearPacketBuffer();
            return;
        }

//        int sectionNum = (int) binToInt(sectionBinary, position, position += sectionNumLength);
//        int lastSectionNum = (int) binToInt(sectionBinary, position, position += sectionNumLength);
        position += 16;

        int N = (sectionLength * BYTE_BITS_LEN) - BITS_LEN_32; //длина следующего поля с циклом программных ассоциаций

        for (int i = 0; i < N; i += BITS_LEN_32) { //цикл приобретения программных ассоциаций таблицы PAT
            if (N < position + BITS_LEN_32) {
                break;
            }
            int programNum = (int) binToInt(sectionBinary, position, position += BITS_LEN_16); //получение номера программы
            if (programNum == 0) {
                // It also gives the location of the Network Information Table (NIT) on program num = 0
                position += 16;
                continue;
            }
            //получение PID пакета, содержащего таблицу PMT программы
            int programMapPID = (int) binToInt(sectionBinary, position += (1 + BITS_LEN_2), position += BITS_LEN_13);
            tables.updatePATMap(programNum, programMapPID);
            tables.incrPMTSet(programMapPID);
        }
        clearPacketBuffer();
        setParserResult(true);
        setParsedFlag();
    }

}
