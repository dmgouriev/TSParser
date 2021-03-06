package ru.ts_parser.parser.header;

import static ru.ts_parser.tools.Tools.binToInt;
import static ru.ts_parser.Constant.*;
import ru.ts_parser.entity.Packet;
import ru.ts_parser.parser.Parser;


/**
 * Класс включает методы для разбора отдельных битов заголовка транспортного пакета
 */
public class HeaderParser extends Parser {

    public Packet analyzeHeader(byte[] header, byte[] packet, long index) {
        int position = SYNC_BYTE_BITS_LEN; 
        byte transportErrorIndicator = header[position++]; 
        byte payloadStartIndicator = header[position++];
        byte transportPriority = header[position++];
        short PID = (short) binToInt(header, position, position += BITS_LEN_13); 
        byte tranportScramblingControl = (byte) binToInt(header, position, position += BITS_LEN_2); //2
        byte adaptationFieldControl = (byte) binToInt(header, position, position += BITS_LEN_2); //4
        byte continuityCounter = (byte) binToInt(header, position, BITS_LEN_4); //4

        return new Packet(
                index,
                transportErrorIndicator,
                payloadStartIndicator,
                transportPriority,
                PID,
                tranportScramblingControl,
                adaptationFieldControl,
                continuityCounter,
                packet
        );
    }

    public int parseHeader(byte[] packet) {
        //получает первые 4 байта с входным массивом байтов, применяя бит-маски
        return ((packet[0] << 24) & 0xff000000 |
                (packet[1] << 16) & 0x00ff0000 |
                (packet[2] << 8)  & 0x0000ff00 |
                (packet[3])       & 0x000000ff
        );
    }
    
}
