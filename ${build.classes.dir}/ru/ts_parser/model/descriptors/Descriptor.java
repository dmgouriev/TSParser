package ru.ts_parser.model.descriptors;


public abstract class Descriptor{

    private short descriptorTag;
    private short descriptorTagLength;

    public Descriptor(short descriptorTag, short descriptorTagLength) {
        this.descriptorTag = descriptorTag;
        this.descriptorTagLength = descriptorTagLength;
    }

    public short getDescriptorTag() {
        return descriptorTag;
    }

    public short getDescriptorTagLength() {
        return descriptorTagLength;
    }
}
