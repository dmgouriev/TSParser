package ru.ts_parser;

public class Constant {

    public final static int NIL = -1;

    public static final int PID_PAT_VALUE = 0x00; // PAT (program association table)
    public static final int PID_NIT_VALUE = 0x10; // NIT (Network Information Table)
    public static final int PID_SDT_VALUE = 0x11; // SDT (Service Description Table) (Bouquet Association Table)
    public static final int PID_PMT_VALUE = NIL;

    public final static int BITS_LEN_2 = 2;
    public final static int BITS_LEN_4 = 4;
    public final static int BITS_LEN_8 = 8;
    public final static int BITS_LEN_12 = 12;
    public final static int BITS_LEN_13 = 13;
    public final static int BITS_LEN_16 = 16;
    public final static int BITS_LEN_24 = 24;
    public final static int BITS_LEN_32 = 32;
    
    public static final int BYTE_BITS_LEN = BITS_LEN_8;
    public static final int CHAR_SIZE = BITS_LEN_8;
    
    public final static int PACKET_SIZE = 188;
    public final static int HEADER_SIZE = 4;
    public final static int HEADER_BITS_LEN = HEADER_SIZE * BYTE_BITS_LEN;
    public final static int ADAPT_FIELD_HEADER_SIZE = 2;
    public final static int ADAPT_FIELD_HEADER_BITS_LEN = ADAPT_FIELD_HEADER_SIZE * BYTE_BITS_LEN;
    public final static int MAX_PID_VALUE = 8192;
    public final static int MAX_SECTION_LENGTH = 1021;
    
    public final static int PAYLOAD_ONLY = 1;
    public final static int ADAPT_FIELD_ONLY = 2;
    public final static int ADAPT_AND_PAYLOAD = 3;
  
    public final static int SYNC_BYTE = 0x47;
    public final static int SYNC_BYTE_SIZE = 1;
    public final static int SYNC_BYTE_BITS_LEN = SYNC_BYTE_SIZE * BYTE_BITS_LEN;

    public final static int PSI_MAX_PID_VALUE = 0x001F;    
    public final static int PSI_COMMON_FIELDS_BITS_LEN = BITS_LEN_24;
    public final static int PSI_COMMON_FIELDS_SIZE = PSI_COMMON_FIELDS_BITS_LEN / BYTE_BITS_LEN;
    
    public static final int NIT_CURRENT_NETWORK_TABLE_ID = 0x40;
    public static final int SDT_CURRENT_STREAM_TABLE_ID = 0x42;
    public static final int SDT_OTHER_STREAM_TABLE_ID = 0x46;

    public static final int NETWORK_NAME_DESCRIPTOR = 0x40;	
    public static final int EXTENSION_DESCRIPTOR = 0x7F;	
    public static final int T2_DELIVERY_SYSTEM_DESCRIPTOR = 0x4;
    public static final int SERVICE_DESCRIPTOR = 0x48;	
    
    public static final int MAX_PARSE_TIME_SECS = 20;

}
