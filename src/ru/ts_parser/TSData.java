package ru.ts_parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static ru.ts_parser.base.MpegCommonData.*;
import ru.ts_parser.entity.Tables;

public class TSData {

    private final Tables tables;

    public TSData(Tables tables) {
        this.tables = tables;
        this.tables.setProgramMap(createPrograms(tables.getPMTmap(), tables.getProgramNameMap()));
    }

    private Map createPrograms(Map pmtMap, Map programNameMap) {
        HashMap<Integer, String> outputMap = new HashMap<>();
        Set<Integer> keys = pmtMap.keySet(); // The set of keys in the map.
        for (Integer key : keys) {
            Integer value = (Integer) pmtMap.get(key);
            String name = (String) programNameMap.get(value);
            outputMap.put(value, (name == null ? ("Service: " + Integer.toString(value)) : name));
        }
        return outputMap;
    }
    
    
    public Tables getTables() {
        return tables;
    }

    public String getPMTString() {
        HashMap<Integer, String> programMap = (HashMap<Integer, String>) this.tables.getProgramMap();
        HashMap<Integer, String> providerMap = (HashMap<Integer, String>) this.tables.getProviderNameMap();
        HashMap<Integer, Integer> PMTmap = (HashMap<Integer, Integer>) this.tables.getPMTmap();
        HashMap<Integer, Integer> ESmap = (HashMap<Integer, Integer>) this.tables.getESmap(); 
        
        this.tables.getServiceNamesMap();

        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("Program Map Table");
        builder.append("\n");
        for (Map.Entry<Integer, String> programEntry : programMap.entrySet()) {
            builder.append("            Program : " + programEntry.getKey() + " (" + getProgramName(programEntry.getKey()) + " - " + providerMap.get(programEntry.getKey()) + ")");
            builder.append("\n");
            int index = 0;
            for (Map.Entry<Integer, Integer> PMTentry : PMTmap.entrySet()) {
                if (programEntry.getKey().equals(PMTentry.getValue())) {
                    for (Map.Entry<Integer, Integer> ESentry : ESmap.entrySet()) {
                        if (ESentry.getKey().equals(PMTentry.getKey())) {
                            builder.append("                Component " + index++ + ": ");
                            builder.append("\n");
                            builder.append("                PID: " + toHex(ESentry.getKey()) + " (" + ESentry.getKey() + ")");
                            builder.append("\n");
                            builder.append("                Stream type: " + getElementaryStreamDescriptor(ESentry.getValue()));
                            builder.append("\n");
                        }
                    }
                }
            }
        }
        return builder.toString();
    }

    private String toHex(int pid) {
        return String.format("0x%04X", pid & 0xFFFFF);
    }

    public String getProgramName(int pid) {
        Map map = this.tables.getProgramNameMap();
        Object obj = map.get(pid);
        return obj == null ? String.valueOf(pid) : obj.toString();
    }

}
