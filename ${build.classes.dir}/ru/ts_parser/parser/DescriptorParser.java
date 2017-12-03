package ru.ts_parser.parser;


import java.io.UnsupportedEncodingException;
import ru.ts_parser.model.descriptors.Descriptor;
import ru.ts_parser.model.descriptors.ServiceDescriptor;
import java.util.ArrayList;
import java.util.List;
import static ru.ts_parser.Tools.binToInt;
import static ru.ts_parser.MpegCommonData.*;
import ru.ts_parser.model.Tables;

public class DescriptorParser extends Parser {

    public List<Descriptor> loadDescriptors(int serviceID, byte[] binaryFields, int size, int position, Tables tables) {

        List<Descriptor> descriptors = new ArrayList<>();

        for(size += position; position < size;) {
            short descriptorTag = (short) binToInt(binaryFields, position, position += descriptorTagLength );
            short descriptorLength = (short) binToInt(binaryFields, position, position += descriptorLengthLength);

            switch ( descriptorTag ) {
                case service_descriptor:
                    descriptors.add(analyzeServiceDescriptor(descriptorLength, serviceID, binaryFields, position, tables));
                    break;
                case short_event_descriptor:
                    break;
                case extended_event_descriptor:
                    break;
                case network_name_descriptor:
                    break;
                default:
                    break;
                //TODO all descriptors
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
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }     
        short serviceNameLength = (short) binToInt(binaryFields, position += serviceProviderNameLength * charSize, position += serviceNameLengthLength);
        String serviceName = "";
        try {
            serviceName = new String(new String(parseNchars(binaryFields, position, serviceNameLength * charSize), "ISO-8859-5").getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }    
        tables.updateProgramMap(serviceID,serviceName.trim());
        System.out.println(serviceName + "\n");
        tables.updateProviderMap(serviceID, serviceProviderName.trim());
        tables.updateServiceName(serviceID, serviceName.trim());
        return new ServiceDescriptor((short) service_descriptor, descriptorLength, serviceType, serviceProviderNameLength, serviceProviderName, serviceNameLength, serviceName);
    }

    
}
