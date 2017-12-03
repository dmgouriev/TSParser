/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ts_parser;

import java.util.Arrays;
import static ru.ts_parser.MpegCommonData.*;
import java.util.Map;

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
        int size = length * byteBinaryLength;
        byte[] binaryFields = new byte[size];
        int offset = 0;

        for (int index = length - 1; index >= 0; index--) {
            for (int i = 0; i < byteBinaryLength; i++, offset++) {
                binaryFields[size - offset - 1] = getBit(intFields[index], i);
            }
        }
        return binaryFields;
    }

    public static <K, V> void ifAbsentPut(Map map, K key, V value) {
        V v = (V) map.get(key);
        if (v == null) {
            map.put(key, value);
        }
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) {
            sb.append(" " + String.format("%02x", b));
        }
        return sb.toString();
    }

    public static <T> T[] append(T[] arr, T element) {
        final int N = arr.length;
        arr = Arrays.copyOf(arr, N + 1);
        arr[N] = element;
        return arr;
    }

}
