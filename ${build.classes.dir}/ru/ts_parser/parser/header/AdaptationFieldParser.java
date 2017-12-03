package ru.ts_parser.parser.header;

import static ru.ts_parser.Tools.binToInt;
import static ru.ts_parser.MpegCommonData.PCRLegth;
import static ru.ts_parser.MpegCommonData.nil;
import ru.ts_parser.model.packet.AdaptationFieldHeader;
import ru.ts_parser.model.packet.AdaptationFieldOptionalFields;
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

        return new AdaptationFieldHeader(adaptationFieldLength, DI, RAI, ESPI, OF, PF, SPF, TPDF, AFEF, null);
    }

    public AdaptationFieldOptionalFields analyzeAdaptationFieldOptionalFields(AdaptationFieldHeader adaptationFieldHeader, byte[] binaryAdaptationFieldOptionalFields, long packetIndex, int PID) {

        int i = 0;
        long PCR = nil;
        long OPCR = nil;
        byte spliceCountdown = nil;
        short TPDlength = 0;
        short AFEFlength = 0;
        byte[] TPD = null;
        byte LTWF = 0;
        byte PRF = 0;
        byte SSF = 0;

        if (adaptationFieldHeader.getPCRF() == 0x1) {
            long rawPCR = binToInt(binaryAdaptationFieldOptionalFields, i, i += PCRLegth);
        }
        if (adaptationFieldHeader.getOPCRF() == 0x1) {
            long rawOPCR = binToInt(binaryAdaptationFieldOptionalFields, i, i += PCRLegth);
        }
        if (adaptationFieldHeader.getSplicingPointFlag() == 0x1) {
            spliceCountdown = (byte) binToInt(binaryAdaptationFieldOptionalFields, i, i += 8);
        }
        int offset = i;
        if (adaptationFieldHeader.getTPDflag() == 0x1) {

            TPDlength = (short) binToInt(binaryAdaptationFieldOptionalFields, i, i += 8);
            offset = i;

            TPD = new byte[TPDlength];
            for (int index = 0; offset < i + TPDlength;) {
                TPD[index++] = binaryAdaptationFieldOptionalFields[offset++];
            }
        }

        if (adaptationFieldHeader.getAFEflag() == 0x1) {

            AFEFlength = (short) binToInt(binaryAdaptationFieldOptionalFields, offset, offset += 8);
            LTWF = binaryAdaptationFieldOptionalFields[offset++];
            PRF = binaryAdaptationFieldOptionalFields[offset++];
            SSF = binaryAdaptationFieldOptionalFields[offset++];
        }

        return new AdaptationFieldOptionalFields(PCR, OPCR, spliceCountdown, TPDlength, TPD, AFEFlength, LTWF, PRF, SSF);
    }

    public short parseAdaptationFieldHeader(byte[] packet) {
        return (short) ((packet[4] << 8) & 0x0000ff00
                | (packet[5]) & 0x000000ff);
    }
}