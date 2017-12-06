package ru.ts_parser.tools;

import static ru.ts_parser.Constant.*;

/**
 *
 * @author dmgouriev
 */
public class Tools {

    public static byte[] toBinary(int source, int length) {
        byte[] binaryField = new byte[length];
        for (int index = 0; index < length; index++) {
            binaryField[length - index - 1] = getBit(source, index);
        }
        return binaryField;
    }

    public static byte getBit(int source, int position) {
        return (byte) ((source >> position) & 1);
    }

    public static long binToInt(byte[] binaryHeader, int start, int end) {
        long result = 0;
        try {
            for (int i = start; i < end; i++) {
                result = (result << 1) | (binaryHeader[i] == 1 ? 1 : 0);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static byte[] intToBinary(int[] intFields, int length) {
        if (length < 0) {
            return new byte[0];
        }
        int size = length * BYTE_BITS_LEN;
        byte[] binaryFields = new byte[size];
        int offset = 0;

        for (int index = length - 1; index >= 0; index--) {
            for (int i = 0; i < BYTE_BITS_LEN; i++, offset++) {
                binaryFields[size - offset - 1] = getBit(intFields[index], i);
            }
        }
        return binaryFields;
    }

}
