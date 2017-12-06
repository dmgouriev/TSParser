package ru.ts_parser.parser;

import ru.ts_parser.entity.Packet;
import static ru.ts_parser.MPEGConstant.*;
import static ru.ts_parser.tools.Tools.binToInt;
import static ru.ts_parser.tools.Tools.getBit;
import ru.ts_parser.entity.PSI;

public abstract class Parser {

    public PSI analyzePSICommonFields(byte[] packet, int startPosition) {

        int commonFields = parseCommonFields(packet, startPosition);

        byte[] binaryPacket = new byte[PSI_COMMON_FIELDS_BITS_LEN];
        for (int index = 0; index < PSI_COMMON_FIELDS_BITS_LEN; index++) {
            binaryPacket[PSI_COMMON_FIELDS_BITS_LEN - index - 1] = getBit(commonFields, index);
        }
        int position = 0;
        short tableID = (short) binToInt(binaryPacket, position, position += BITS_LEN_8);
        byte sectionSyntaxIndicator = (byte) binToInt(binaryPacket, position, position += 1);
        int sectionLength = (int) binToInt(binaryPacket, position += 1 + BITS_LEN_2, position += BITS_LEN_12);

        return new PSI(tableID, sectionSyntaxIndicator, sectionLength, null);
    }

    public int parseCommonFields(byte[] packet, int position) {
        return ((packet[position] << 16) & 0x00ff0000
                | (packet[++position] << 8) & 0x0000ff00
                | (packet[++position]) & 0x000000ff);
    }

    public int calculatePosition(Packet packet) {
        int position = HEADER_SIZE;
        if (hasAdaptationField(packet.getAdaptationFieldControl())) {
            position += packet.getAdaptationFieldHeader().getAdaptationFieldLength();
        }
        return position;
    }

    public boolean hasAdaptationField(int adaptationFieldControl) {
        return (adaptationFieldControl == ADAPT_FIELD_ONLY || adaptationFieldControl == ADAPT_AND_PAYLOAD);
    }

    public boolean hasPayload(int adaptationFieldControl) {
        return (adaptationFieldControl == PAYLOAD_ONLY || adaptationFieldControl == ADAPT_AND_PAYLOAD);
    }

    public int[] parseNfields(byte[] packet, int pos, int length) {
        int position = pos, index = 0;
        int[] byteFields = new int[length];
        for (index = 0; index < length; index++) {
            try {
                byte packetByte = packet[position++];
                byteFields[index] = (packetByte & 0x000000ff);
            } catch (Exception e) {
                break;
            }
        }
        return byteFields;
    }

    public byte[] parseNchars(byte[] binaryHeader, int position, int length) {
        int end = position + length;
        byte[] byteStringArray = new byte[length / CHAR_SIZE];
        for (int i = 0; position < end; position += CHAR_SIZE, i++) {
            byteStringArray[i] = binToByte(binaryHeader, position);
        }
        return byteStringArray;
    }

    private char binToChar(byte[] binaryHeader, int start) {
        char result = 0;
        for (int i = start; i < start + CHAR_SIZE; i++) {
            result = (char) ((result << 1) | (binaryHeader[i] == 1 ? 1 : 0));
        }
        return result;
    }

    private byte binToByte(byte[] binaryHeader, int start) {
        byte result = (byte) 0;
        for (int i = start; i < start + CHAR_SIZE; i++) {
            result = (byte) ((result << 1) | (binaryHeader[i] == 1 ? 1 : 0));
        }
        return result;
    }

    public byte[] merge(byte[] array1, byte[] array2) {
        if (array1 == null) {
            array1 = new byte[0];
        }
        byte[] array = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, array, 0, array1.length);
        System.arraycopy(array2, 0, array, array1.length, array2.length);
        return array;
    }

}
