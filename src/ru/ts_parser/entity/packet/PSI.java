package ru.ts_parser.entity.packet;

public class PSI {

    private short tableID;
    private byte sectionSyntaxIndicator;
    private int sectionLength;
    protected byte[] data;
    private final boolean isPSI;
    private final boolean hasPESheader;

    public PSI(short tableID, byte SSI, int sectionLength, byte[] data) {
        this.hasPESheader = false;
        this.isPSI = true;
        this.tableID = tableID;
        this.sectionSyntaxIndicator = SSI;
        this.sectionLength = sectionLength;
    }

    public PSI() {
        this.hasPESheader = false;
        this.isPSI = true;
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

    public short getTableID() {
        return tableID;
    }

    public byte getSSI() {
        return sectionSyntaxIndicator;
    }

    public int getSectionLength() {
        return sectionLength;
    }

}
