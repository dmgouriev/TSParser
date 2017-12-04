package ru.ts_parser.parser;

import java.io.UnsupportedEncodingException;
import ru.ts_parser.entity.descriptors.Descriptor;
import ru.ts_parser.entity.descriptors.ServiceDescriptor;
import java.util.ArrayList;
import java.util.List;
import static ru.ts_parser.tools.Tools.binToInt;
import static ru.ts_parser.base.MpegCommonData.*;
import ru.ts_parser.entity.Tables;

public class DescriptorParser extends Parser {

    public List<Descriptor> loadDescriptors(int ID, byte[] binaryFields, int size, int position, Tables tables) {

        List<Descriptor> descriptors = new ArrayList<>();

        for (size += position; position < size;) {
            short descriptorTag = (short) binToInt(binaryFields, position, position += descriptorTagLength);
            short descriptorLength = (short) binToInt(binaryFields, position, position += descriptorLengthLength);

            switch (descriptorTag) {
                case service_descriptor:
                    descriptors.add(analyzeServiceDescriptor(descriptorLength, ID, binaryFields, position, tables));
                    break;
                case network_name_descriptor:
                    descriptors.add(analyzeNetworkDescriptor(descriptorLength, ID, binaryFields, position, tables));
                    break;
                case extension_descriptor:
                    descriptors.add(analyzeT2DeliverySystemDescriptor(descriptorLength, ID, binaryFields, position, tables));
                    break;
                default:
                    break;
            }
            position += descriptorLength * byteBinaryLength;
        }
        return descriptors;
    }

    private Descriptor analyzeServiceDescriptor(short descriptorLength, int serviceID, byte[] binaryFields, int position, Tables tables) {
        short serviceType = (short) binToInt(binaryFields, position, position += serviceTypeLength);
        short serviceProviderNameLength = (short) binToInt(binaryFields, position, position += serviceNameLengthLength);
        byte[] sp = parseNchars(binaryFields, position, serviceProviderNameLength * charSize);
        String serviceProviderName = "";
        try {
            serviceProviderName = new String(new String(parseNchars(binaryFields, position, serviceProviderNameLength * charSize), "ISO-8859-5").getBytes(), "UTF-8");
        } catch (Exception ex) {
            return null;
        }
        short serviceNameLength = (short) binToInt(binaryFields, position += serviceProviderNameLength * charSize, position += serviceNameLengthLength);
        String serviceName = "";
        try {
            serviceName = new String(new String(parseNchars(binaryFields, position, serviceNameLength * charSize), "ISO-8859-5").getBytes(), "UTF-8");
        } catch (Exception ex) {
            return null;
        }
        tables.updateProgramMap(serviceID, serviceName.trim());
        tables.updateProviderMap(serviceID, serviceProviderName.trim());
        tables.updateServiceName(serviceID, serviceName.trim());
        return new ServiceDescriptor((short) service_descriptor, descriptorLength, serviceType, serviceProviderNameLength, serviceProviderName, serviceNameLength, serviceName);
    }

    private Descriptor analyzeNetworkDescriptor(short descriptorLength, int networkID, byte[] binaryFields, int position, Tables tables) {
        byte[] sp = parseNchars(binaryFields, position, descriptorLength * charSize);
        String networkName = "";
        try {
            networkName = new String(new String(parseNchars(binaryFields, position, descriptorLength * charSize), "ISO-8859-5").getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
//        System.out.println(networkName);
        return null;
    }

    private Descriptor analyzeT2DeliverySystemDescriptor(short descriptorLength, int transportStreamID, byte[] binaryFields, int position, Tables tables) {
        short descriptorTagExtension = (short) binToInt(binaryFields, position, position += serviceTypeLength);
        if (descriptorTagExtension == t2_delivery_system_descriptor) {
            short plpId = (short) binToInt(binaryFields, position, position += eightBitslength);
            short t2SystemId = (short) binToInt(binaryFields, position, position += sixteenBitslength);
//            System.out.println("plpId = " + plpId + ", t2SystemId = " + t2SystemId);
        }
        return null;
    }

}
