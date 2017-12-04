/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ts_parser.parser.psi.base;

import java.util.Arrays;
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

    protected byte[] fullPackageBuffer = null;

    protected ParserState currentState = null;
    
    protected boolean parserResult = false;
    
    public void parse(Packet packet, Tables tables) {
        if (getParserPSITableType() == PSI_TABLE_TYPE.SDT) {
                System.out.println(packet.getContinuityCounter() + "\n");
        }
        if (fullPackageBuffer != null && packet.getPayloadStartIndicator() == 0) {
            fullPackageBuffer = merge(fullPackageBuffer, Arrays.copyOfRange(packet.getData(), tsHeaderSize, tsPacketSize));
            packet.updateData(fullPackageBuffer);
        } else {
            fullPackageBuffer = null;
            currentState = null;
        }
        int position = calculatePosition(packet) + 1; //определение начальной позиции на основе длины предыдущих заголовков
        PSI psiCommonFields = analyzePSICommonFields(packet.getData(), position); //анализ общих полей таблиц PSI, т. е. tableID, SSI и sectionLength
        position += PSIcommonFieldsLength; //обновление позиции после получения общих полей
        int sectionLength = psiCommonFields.getSectionLength();
        if (sectionLength > 1021) {
            currentState = null;
            return;
        }
        if ((sectionLength + position + CRClengthByte) > packet.getData().length) {
            fullPackageBuffer = packet.getData();
            currentState = new ParserState(getParserPSITableType(), packet.getPID());
            currentState.setNeedContinue(true);
            return;
        }
        int[] intSectionsFields = parseNfields(packet.getData(), position, sectionLength);
        byte[] sectionBinary = Tools.intToBinary(intSectionsFields, sectionLength);
        if ((int) Tools.binToInt(sectionBinary, sectionLength * byteBinaryLength - CRClength, CRClength) != 0) {
            currentState = null;
            return;
        };

        parseSection(packet, sectionBinary, sectionLength, position, tables);
    }

    public void setParserState(ParserState newState) {
        this.currentState = newState;
    }
    
    public void setParserResult(boolean result) {
        this.parserResult = result;
    }
    
    public boolean getParserResult() {
        return parserResult;
    }
    
    protected abstract void parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, Tables tables);

    protected abstract PSI_TABLE_TYPE getParserPSITableType();
    
}
