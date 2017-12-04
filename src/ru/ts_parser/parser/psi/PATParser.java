package ru.ts_parser.parser.psi;

import static ru.ts_parser.MpegCommonData.*;
import static ru.ts_parser.Tools.binToInt;
import ru.ts_parser.model.Tables;
import ru.ts_parser.model.packet.Packet;
import ru.ts_parser.parser.psi.base.PSIParserAbstract;

/**
 *
 * @author dmgouriev
 */
public class PATParser extends PSIParserAbstract {

    final int reserved = 2; //MPEG константа

    @Override
    protected void parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, Tables tables) {
        int transportStreamID = (int) binToInt(sectionBinary, position = 0, position += transportStreamIDlength);
        tables.updateCurrentTransportStreamID(transportStreamID);

//        short versionNum = (short) binToInt(sectionBinary, position += reserved, position += versionNumLength);
        position += 7;

        byte currentNextIndicator = sectionBinary[position++];
        if (currentNextIndicator != 1) {
            setParserState(null);
            return;
        }

//        int sectionNum = (int) binToInt(sectionBinary, position, position += sectionNumLength);
//        int lastSectionNum = (int) binToInt(sectionBinary, position, position += sectionNumLength);
        position += 16;

        int N = (sectionLength * byteBinaryLength) - CRClength; //mandatoryPATfields; //длина следующего поля с циклом программных ассоциаций

        for (int i = 0; i < N; i += PATloopLength) { //цикл приобретения программных ассоциаций таблицы PAT
            if (N < position + PATloopLength) {
                break;
            }
            int programNum = (int) binToInt(sectionBinary, position, position += programNumberLength); //получение номера программы
            if (programNum == 0) {
                // It also gives the location of the Network Information Table (NIT) on program num = 0
                position += 16;
                continue;
            }
            //получение PID пакета, содержащего таблицу PMT программы
            int programMapPID = (int) binToInt(sectionBinary, position += 3, position += PCR_PIDlength);
            tables.getPATmap().put(programNum, programMapPID);

            tables.incrPMTSet(programMapPID);
            tables.incrSDTSet(programNum);

        }
        fullPackageBuffer = null;
        setParserState(null);
        setParserResult(true);
    }

    @Override
    protected PSI_TABLE_TYPE getParserPSITableType() {
        return PSI_TABLE_TYPE.PAT;
    }

}
