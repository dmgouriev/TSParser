/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ts_parser.parser.psi;

import java.util.Arrays;
import static ru.ts_parser.Constant.*;
import ru.ts_parser.tools.Tools;
import ru.ts_parser.TSTableData;
import ru.ts_parser.entity.Packet;
import ru.ts_parser.entity.PSI;
import ru.ts_parser.tools.CRC32;
import ru.ts_parser.parser.Parser;

/**
 *
 * @author dmgouriev
 */
public abstract class PSIParserAbstract extends Parser {

    private byte[] previousPacketBuffer = null;

    private boolean parserResult = false;
    
    private boolean parsedFlag = false;

    public void parse(Packet packet, TSTableData tables) {
        if (previousPacketBuffer != null && packet.getPayloadStartIndicator() == 0) {
            previousPacketBuffer = merge(previousPacketBuffer, Arrays.copyOfRange(packet.getData(), HEADER_SIZE, PACKET_SIZE));
            packet.updateData(previousPacketBuffer);
        } else {
            previousPacketBuffer = null;
        }
        
        int position = calculatePosition(packet) + 1; //определение начальной позиции на основе длины предыдущих заголовков
        PSI psiCommonFields = analyzePSICommonFields(packet.getData(), position); //анализ общих полей таблиц PSI, т. е. tableID, SSI и sectionLength
        int sectionLength = psiCommonFields.getSectionLength();
        
        int sectionStart = position;
        int sectionEnd = position + PSI_COMMON_FIELDS_SIZE + sectionLength;
         
        if (sectionEnd > packet.getData().length) {
            previousPacketBuffer = packet.getData();
            return;
        }
        
        if (sectionLength > MAX_SECTION_LENGTH || !CRC32.validate(Arrays.copyOfRange(packet.getData(), sectionStart, sectionEnd))) {
            previousPacketBuffer = null;
            return;
        }

        position += PSI_COMMON_FIELDS_SIZE; //обновление позиции после получения общих полей

        int[] intSectionsFields = parseNfields(packet.getData(), position, sectionLength);
        byte[] sectionBinary = Tools.intToBinary(intSectionsFields, sectionLength);
        parseSection(packet, sectionBinary, sectionLength, position, psiCommonFields.getTableID(), tables);
    }

    public void setParserResult(boolean result) {
        this.parserResult = result;
    }
    
    public void setParsedFlag() {
        parsedFlag = true;
    }

    public boolean hasParsedFlag() {
        return parsedFlag;
    }
    
    public boolean getParserResult() {
        return parserResult;
    }
    
    public void clearPacketBuffer() {
        previousPacketBuffer = null;
    }

    protected abstract void parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, short tableID, TSTableData tables);

}
