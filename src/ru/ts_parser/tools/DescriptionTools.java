package ru.ts_parser.tools;

/**
 *
 * @author dmgouriev
 */
public class DescriptionTools {
    
    
    public static String getElementaryStreamDescriptor(Integer descriptor) {
        if (descriptor == null) {
            return "Unidentified ES descriptor";
        } else if (descriptor >= 0x1C && descriptor <= 0x7F) {
            return "ITU-T Rec. H.222 | ISO/IEC 13818-1 Reserved";
        } else if (descriptor >= 0x80 && descriptor <= 0xFF) {
            return "User defined";
        } else {
            switch (descriptor) {
                case 0x00:
                    return "Reserved";
                case 0x01:
                    return "ISO/IEC 11172-2 (MPEG-1 video)";
                case 0x02:
                    return "ITU-T Rec. H.262 and ISO/IEC 13818-2 (MPEG-2 higher rate interlaced video)";
                case 0x03:
                    return "ISO/IEC 11172-3 (MPEG-1 audio)";
                case 0x04:
                    return "ISO/IEC 13818-3 (MPEG-2 halved sample rate audio)";
                case 0x05:
                    return "ITU-T Rec. H.222 and ISO/IEC 13818-1 (MPEG-2 tabled data) privately defined	";
                case 0x06:
                    return "ITU-T Rec. H.222 and ISO/IEC 13818-1 (MPEG-2 packetized data) privately defined (i.e., MPEG subtitles/VBI and AC-3)";
                case 0x07:
                    return "ISO/IEC 13522 (MHEG)";
                case 0x08:
                    return "ITU-T Rec. H.222 and ISO/IEC 13818-1 DSM CC ";
                case 0x09:
                    return "ITU-T Rec. H.222 and ISO/IEC 13818-1/11172-1 auxiliary data ";
                case 0x0A:
                    return "ISO/IEC 13818-6 DSM CC multiprotocol encapsulation";
                case 0x0B:
                    return "ISO/IEC 13818-6 DSM CC U-N messages";
                case 0x0C:
                    return "ISO/IEC 13818-6 DSM CC stream descriptors";
                case 0x0D:
                    return "ISO/IEC 13818-6 DSM CC tabled data";
                case 0x0E:
                    return "ISO/IEC 13818-1 auxiliary data ";
                case 0x0F:
                    return "ISO/IEC 13818-7 ADTS AAC (MPEG-2 lower bit-rate audio)";
                case 0x10:
                    return "ISO/IEC 14496-2 (MPEG-4 H.263 based video)";
                case 0x11:
                    return "ISO/IEC 14496-3 (MPEG-4 LOAS multi-format framed audio) ";
                case 0x12:
                    return "ISO/IEC 14496-1 (MPEG-4 FlexMux) ";
                case 0x13:
                    return "ISO/IEC 14496-1 (MPEG-4 FlexMux) in ISO/IEC 14496 tables";
                case 0x14:
                    return "ISO/IEC 13818-6 DSM CC synchronized download protocol";
                case 0x15:
                    return "Packetized metadata";
                case 0x16:
                    return "Sectioned metadata";
                case 0x17:
                    return "ISO/IEC 13818-6 DSM CC Data Carousel metadata";
                case 0x18:
                    return "ISO/IEC 13818-6 DSM CC Object Carousel metadata";
                case 0x19:
                    return "ISO/IEC 13818-6 Synchronized Download Protocol metadata";
                case 0x1A:
                    return "ISO/IEC 13818-11 IPMP";
                case 0x1B:
                    return "ITU-T Rec. H.264 and ISO/IEC 14496-10 (lower bit-rate video)";
                default:
                    return "Unidentified ES descriptor";
            }
        }
    }

    public static String getServiceTypeString(final int serviceType) {

        switch (serviceType) {
            case 0x00:
                return "reserved";
            case 0x12:
                return "reserved";
            case 0x13:
                return "reserved";
            case 0x14:
                return "reserved";
            case 0x15:
                return "reserved";
            case 0x08:
                return "reserved";
            case 0x09:
                return "reserved";
            case 0x01:
                return "TV (SD)";
            case 0x11:
                return "TV (HD-MPEG2)";
            case 0x16:
                return "TV (SD-MPEG4)";
            case 0x19:
                return "TV (HD-MPEG4)";
            case 0x02:
                return "Radio";
            case 0x0A:
                return "Radio (advanced)";
            case 0x03:
                return "Teletext";
            case 0x04:
                return "NVOD reference";
            case 0x05:
                return "NVOD time-shifted";
            case 0x06:
                return "mosaic";
            case 0x07:
                return "PAL coded signal";
            case 0x0B:
                return "mosaic advanced)";
            case 0x0C:
                return "data broadcast";
            case 0x0D:
                return "Common Interface Usage";
            case 0x0E:
                return "RCS Map";
            case 0x0F:
                return "RCS FLS";
            case 0x10:
                return "DVB MHP service";
            case 0x17:
                return "advanced codec SD NVOD time-shifted ";
            case 0x18:
                return "advanced codec SD NVOD reference";
            case 0x1A:
                return "advanced codec HD NVOD time-shifted";
            case 0x1B:
                return "advanced codec HD NVOD reference";
            default:
                if ((0x1C <= serviceType) && (serviceType <= 0x7F)) {
                    return "reserved";
                }

                if ((0x80 <= serviceType) && (serviceType <= 0xFE)) {
                    return "user defined";
                }
                return "Illegal value";
        }
    }

    public static String getServiceType(final int serviceType) {
        switch (serviceType) {
            case 0x01:
            case 0x11:
            case 0x16:
            case 0x19:
                return "TV";
            case 0x02:
            case 0x0A:
                return "Radio";
            default:
                return "Unknown";
        }

    }


    
}
