package ru.ts_parser.model.packet.psi;

import ru.ts_parser.parser.TimestampParser;


public abstract class Payload extends TimestampParser {
    protected byte[] data;
    private final boolean isPSI;
    private final  boolean hasPESheader;

    protected Payload(boolean isPSI,  boolean hasPESheader){
        this.hasPESheader = hasPESheader;
        this.isPSI = isPSI;
    }

    public byte[] getData() {
        return data;
    }

    public boolean hasPESheader() {
        return hasPESheader;
    }

    public boolean isPSI() {
        return isPSI;
    }
}
