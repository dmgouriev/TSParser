package ru.ts_parser.model.packet.psi;

public class PSI extends Payload {

    private short tableID;
    private byte sectionSyntaxIndicator;
    private int sectionLength;

    public PSI(short tableID, byte SSI, int sectionLength, byte[] data) {
        super(true, false);
        this.tableID = tableID;
        this.sectionSyntaxIndicator = SSI;
        this.sectionLength = sectionLength;
    }

    public PSI() {
        super(true,false);
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


