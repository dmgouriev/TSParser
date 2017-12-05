package ru.ts_parser.parser;

import java.io.UnsupportedEncodingException;
import static ru.ts_parser.tools.Tools.binToInt;
import static ru.ts_parser.MPEGConstant.*;
import ru.ts_parser.TSTableData;

public class DescriptorParser extends Parser {

    public void parse(int ID, byte[] binaryFields, int size, int position, TSTableData tables) {
        for (size += position; position < size;) {
            short descriptorTag = (short) binToInt(binaryFields, position, position += descriptorTagLength);
            short descriptorLength = (short) binToInt(binaryFields, position, position += descriptorLengthLength);

            switch (descriptorTag) {
                case service_descriptor:
                    parseServiceDescriptor(ID, binaryFields, position, tables);
                    break;
                case network_name_descriptor:
                    parseNetworkDescriptor(descriptorLength, ID, binaryFields, position, tables);
                    break;
                case extension_descriptor:
                    parseExtensionDescriptor(ID, binaryFields, position, tables);
                    break;
                default:
                    break;
            }
            position += descriptorLength * byteBinaryLength;
        }
    }

    private void parseServiceDescriptor(int serviceID, byte[] binaryFields, int position, TSTableData tables) {
        short serviceType = (short) binToInt(binaryFields, position, position += serviceTypeLength);
        short serviceProviderNameLength = (short) binToInt(binaryFields, position, position += serviceNameLengthLength);
        String serviceProviderName;
        try {
            serviceProviderName = new String(new String(parseNchars(binaryFields, position, serviceProviderNameLength * charSize), "ISO-8859-5").getBytes(), "UTF-8");
        } catch (Exception ex) {
            return;
        }
        short serviceNameLength = (short) binToInt(binaryFields, position += serviceProviderNameLength * charSize, position += serviceNameLengthLength);
        String serviceName;
        try {
            serviceName = new String(new String(parseNchars(binaryFields, position, serviceNameLength * charSize), "ISO-8859-5").getBytes(), "UTF-8");
        } catch (Exception ex) {
            return;
        }
        tables.updateProgramNameMap(serviceID, serviceName.trim());
        tables.updateProviderNameMap(serviceID, serviceProviderName.trim());
        tables.updateProgramTypeMap(serviceID, serviceType);
    }

    private void parseNetworkDescriptor(short descriptorLength, int networkID, byte[] binaryFields, int position, TSTableData tables) {
        byte[] sp = parseNchars(binaryFields, position, descriptorLength * charSize);
        String networkName;
        try {
            networkName = new String(new String(parseNchars(binaryFields, position, descriptorLength * charSize), "ISO-8859-5").getBytes(), "UTF-8");
            tables.updateNetworkName(networkName);
        } catch (UnsupportedEncodingException ex) {
        }
    }

    private void parseExtensionDescriptor(int transportStreamID, byte[] binaryFields, int position, TSTableData tables) {
        short descriptorTagExtension = (short) binToInt(binaryFields, position, position += serviceTypeLength);
        if (descriptorTagExtension == t2_delivery_system_descriptor) {
            short plpId = (short) binToInt(binaryFields, position, position += eightBitslength);
            short t2SystemId = (short) binToInt(binaryFields, position, position += sixteenBitslength);
            tables.updateTransportStreamMap(transportStreamID, plpId);
        }
    }

}
