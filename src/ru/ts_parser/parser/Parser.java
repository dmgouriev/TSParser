package ru.ts_parser.parser;

import ru.ts_parser.model.packet.Packet;
import static ru.ts_parser.MpegCommonData.*;
import static ru.ts_parser.Tools.binToInt;
import static ru.ts_parser.Tools.getBit;
import ru.ts_parser.model.psi.PSI;

public abstract class Parser {

    public PSI analyzePSICommonFields(byte[] packet, int startPosition) {

        int commonFields = parseCommonFields(packet, startPosition);

        byte[] binaryPacket = new byte[PSIcommonFieldsBinaryLength];
        for (int index = 0; index < PSIcommonFieldsBinaryLength; index++) {
            binaryPacket[PSIcommonFieldsBinaryLength - index - 1] = getBit(commonFields, index);
        }
        int position = 0;
        final int reserved = 2;
        short tableID = (short) binToInt(binaryPacket, position, position += tableIDlength);
        byte sectionSyntaxIndicator = (byte) binToInt(binaryPacket, position, position += 1);
        int sectionLength = (int) binToInt(binaryPacket, position += 1 + reserved, position += sectionLengthLength);

        return new PSI(tableID, sectionSyntaxIndicator, sectionLength, null);
    }

    public int parseCommonFields(byte[] packet, int position) {
        return ((packet[position] << 16) & 0x00ff0000
                | (packet[++position] << 8) & 0x0000ff00
                | (packet[++position]) & 0x000000ff);
    }

    public boolean isPSI(Integer PID) {
        return PID <= PSImaxPID;
    }
    
    public int calculatePosition(Packet packet) {
        int position = tsHeaderSize;
//        try {
            if (hasAdaptationField(packet.getAdaptationFieldControl())) {
                position += packet.getAdaptationFieldHeader().getAdaptationFieldLength();
            }
//        } catch (Exception e) {
//            return -1;
//        }
        return position;
    }

    public boolean hasAdaptationField(int adaptationFieldControl) {
        return (adaptationFieldControl == adaptationFieldOnly || adaptationFieldControl == adaptationFieldAndPayload);
    }

    public boolean hasPayload(int adaptationFieldControl) {
        return (adaptationFieldControl == payloadOnly || adaptationFieldControl == adaptationFieldAndPayload);
    }
    
    public int[] parseNfields(byte[] packet, int pos, int length) {
        int position = pos, index = 0;
        if (length < 0) {
            return new int[0];
        }
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
        byte[] byteStringArray = new byte[length / charSize];
        for (int i = 0; position < end; position += charSize, i++) {
            byteStringArray[i] = binToByte(binaryHeader, position);
        }
        return byteStringArray;
    }

    private char binToChar(byte[] binaryHeader, int start) {
        char result = 0;
        for (int i = start; i < start + charSize; i++) {
            result = (char) ((result << 1) | (binaryHeader[i] == 1 ? 1 : 0));
        }
        return result;
    }

    private byte binToByte(byte[] binaryHeader, int start) {
        byte result = (byte) 0;
        for (int i = start; i < start + charSize; i++) {
            result = (byte) ((result << 1) | (binaryHeader[i] == 1 ? 1 : 0));
        }
        return result;
    }
    
    protected boolean isSDT(short tableID) {
        return tableID == SDSactualTableID || tableID == SDSotherTableID;
    }

    protected boolean isEIT(short tableID) {
        if (tableID == EISactualPresentTableID || tableID == EISotherPresentTableID) {
            return true;
        } else if (tableID >= 0x50 || tableID <= 0x5F) { //EISactualPresentTableIDschedule
            return true;
        } else if (tableID >= 0x60 || tableID <= 0x6F) { //EISotherPresentTableIDschedule
            return true;
        }
        return false;
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
