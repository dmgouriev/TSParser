package ru.ts_parser.parser.header;

import static ru.ts_parser.tools.Tools.binToInt;
import ru.ts_parser.entity.packet.AdaptationFieldHeader;
import ru.ts_parser.parser.Parser;

/**
 *
 * @author dmgouriev
 */
public class AdaptationFieldParser extends Parser {


    public AdaptationFieldHeader analyzeAdaptationFieldHeader(byte[] adaptationFieldHeader) {

        int i = 8;
        short adaptationFieldLength = (short) binToInt(adaptationFieldHeader, 0, 8);

        byte DI = adaptationFieldHeader[i++];
        byte RAI = adaptationFieldHeader[i++];
        byte ESPI = adaptationFieldHeader[i++];
        byte PF = adaptationFieldHeader[i++];
        byte OF = adaptationFieldHeader[i++];
        byte SPF = adaptationFieldHeader[i++];
        byte TPDF = adaptationFieldHeader[i++];
        byte AFEF = adaptationFieldHeader[i++];

        return new AdaptationFieldHeader(adaptationFieldLength, DI, RAI, ESPI, OF, PF, SPF, TPDF, AFEF);
    }

    public short parseAdaptationFieldHeader(byte[] packet) {
        return (short) ((packet[4] << 8) & 0x0000ff00
                | (packet[5]) & 0x000000ff);
    }
}
