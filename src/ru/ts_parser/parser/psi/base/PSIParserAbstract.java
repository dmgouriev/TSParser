/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ts_parser.parser.psi.base;

import java.util.Arrays;
import ru.ts_parser.MpegCommonData;
import static ru.ts_parser.MpegCommonData.*;
import ru.ts_parser.Tools;
import ru.ts_parser.model.Tables;
import ru.ts_parser.model.packet.Packet;
import ru.ts_parser.model.packet.psi.PSI;
import ru.ts_parser.parser.Parser;
import ru.ts_parser.parser.ParserState;

/**
 *
 * @author dmgouriev
 */
public abstract class PSIParserAbstract extends Parser {
    
    public byte[] fullPackageBuffer = null;
    
    public ParserState parse(Packet packet, Tables tables, ParserState currentState) {
        ParserState newState = new ParserState(getParserPSITableType(), packet.getPID());
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
        int[] intSectionsFields = parseNfields(packet.getData(), position, sectionLength);
        return parseSection(packet, Tools.intToBinary(intSectionsFields, sectionLength), sectionLength, position, newState, tables);
    }

    protected abstract ParserState parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, ParserState newState, Tables tables);

    protected abstract PSI_TABLE_TYPE getParserPSITableType();
}
