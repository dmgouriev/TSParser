package ru.ts_parser;

import java.util.*;
import ru.ts_parser.tools.DescriptionTools;

public class TSTableData {

    /* Current transport stream */
    private int currentTransportStreamID;

    /* PAT */
    private final Map<Integer, Integer> PATmap;

    /* PMT */
    private final Map<Integer, Integer> PMTmap;
    private final Map<Integer, Integer> ESmap;
    private final Set<Integer> PMTset; // индикатор проанализированных PID из таблицы PAT

    /* SDT */
    private final Map<Integer, String> programNameMap;
    private final Map<Integer, String> providerNameMap;
    private final Map<Integer, Integer> programTypeMap;
    private final Map<Integer, Boolean> programFreeCAModeMap;
    private final Map<Integer, Integer> programTransportStreamMap;

    /* NIT */
    private String networkName;
    private final Map<Integer, Integer> transportStreamMap;
    private final Set<Integer> transportStreamSet; // индикатор проанализированных transportStreamID из таблицы NIT

    /* fill after parse */
    private Map<Integer, String> programMap;

    public TSTableData() {
        currentTransportStreamID = -1;

        PATmap = new HashMap<>();

        PMTmap = new HashMap();
        ESmap = new HashMap();
        PMTset = new HashSet();

        programNameMap = new HashMap();
        programTransportStreamMap = new HashMap();
        providerNameMap = new HashMap();
        programTypeMap = new HashMap();
        programFreeCAModeMap = new HashMap();

        networkName = "";
        transportStreamMap = new HashMap();
        transportStreamSet = new HashSet();

        programMap = new HashMap();
    }

    @Override
    public String toString() {
        createProgramMap();
        StringBuilder builder = new StringBuilder();
        builder.append("Current transport stream ID: " + currentTransportStreamID + "\n");
        builder.append("Network name: " + networkName + "\n");
        builder.append("Network transport stream info:\n");
        for (Map.Entry<Integer, Integer> tsEntry : transportStreamMap.entrySet()) {
            builder
                    .append("            ")
                    .append("transport stream ID: " + tsEntry.getKey() + ", PLP: " + tsEntry.getValue() + "\n");
        }
        builder.append("Program Map Simple Table (total: " + programNameMap.keySet().size() + ")");
        builder.append("\n");
        for (Map.Entry<Integer, String> programEntry : programNameMap.entrySet()) {
            builder
                    .append("            ")
                    .append(programEntry.getKey() + "   ")
                    .append(getSimpleInfoString(programEntry.getKey(), getProgramName(programEntry.getKey())) + "\n");
        }
        builder.append("Program Map Full Table (total: " + programMap.keySet().size() + ")");
        builder.append("\n");
        for (Map.Entry<Integer, String> programEntry : programMap.entrySet()) {
            builder
                    .append("            ")
                    .append("Program number: " + programEntry.getKey() + "\n")
                    .append("            ")
                    .append("Program name: " + getProgramName(programEntry.getKey()) + "\n")
                    .append("            ")
                    .append("Provider name: " + providerNameMap.get(programEntry.getKey()) + "\n")
                    .append("            ")
                    .append("Program type name: " + DescriptionTools.getServiceType(programTypeMap.get(programEntry.getKey())) + "\n")
                    .append("            ")
                    .append("Free CA mode: " + getFreeCAModeName(programFreeCAModeMap.get(programEntry.getKey())) + "\n");
            int index = 0;
            for (Map.Entry<Integer, Integer> PMTentry : PMTmap.entrySet()) {
                if (programEntry.getKey().equals(PMTentry.getValue())) {
                    for (Map.Entry<Integer, Integer> ESentry : ESmap.entrySet()) {
                        if (ESentry.getKey().equals(PMTentry.getKey())) {
                            builder
                                    .append("                        ")
                                    .append("Component " + index++ + ":\n")
                                    .append("                        ")
                                    .append("       PID: " + ESentry.getKey() + "\n")
                                    .append("                        ")
                                    .append("       Stream type: " + DescriptionTools.getElementaryStreamDescriptor(ESentry.getValue()) + "\n");
                        }
                    }
                }
            }
        }
        return builder.toString();
    }

    private void createProgramMap() {
        Set<Integer> keys = PMTmap.keySet();
        for (Integer key : keys) {
            Integer value = (Integer) PMTmap.get(key);
            String name = (String) programNameMap.get(value);
            programMap.put(value, (name == null ? ("Service: " + Integer.toString(value)) : name));
        }
    }

    public String getProgramName(int pid) {
        Object obj = programNameMap.get(pid);
        return obj == null ? String.valueOf(pid) : obj.toString();
    }

    public String getFreeCAModeName(boolean value) {
        return (!value ? "clear" : "encoded");
    }

    public String getSimpleInfoString(int serviceID, String programName) {
        Object obj = programTransportStreamMap.get(serviceID);
        int streamId = -1;
        if (obj != null) {
            streamId = (int) obj;
        }
        String text = (transportStreamMap.get(streamId)!=null?("     PLP: " + transportStreamMap.get(streamId)):"") + "       " + programName + "     ";
        text += ((streamId != currentTransportStreamID)?"NOT IN CURRENT STREAM" : "");
        return text;
    }

    /*      PAT     */
    public void updatePATMap(int programNum, int programMapPID) {
        PATmap.put(programNum, programMapPID);
    }

    public void incrPMTSet(int PID) {
        PMTset.add(PID);
    }

    public void setCurrentTransportStreamID(int transportStreamID) {
        currentTransportStreamID = transportStreamID;
    }

    /*      PMT     */
    public void updatePMTMap(int elementaryPID, int programNum) {
        PMTmap.put(elementaryPID, programNum);
    }

    public void updateESMap(int elementaryPID, int streamType) {
        ESmap.put(elementaryPID, streamType);
    }

    public boolean decrPMTSet(int PID) {
        return PMTset.remove(PID);
    }

    /*      SDT     */
    public void updateProgramNameMap(int serviceID, String serviceName) {
        programNameMap.put(serviceID, serviceName);
    }

    public void updateProviderNameMap(int serviceID, String providerName) {
        providerNameMap.put(serviceID, providerName);
    }

    public void updateProgramTypeMap(int serviceID, int programType) {
        programTypeMap.put(serviceID, programType);
    }

    public void updateProgramFreeCAModeMap(int serviceID, int freeCAModeValue) {
        programFreeCAModeMap.put(serviceID, freeCAModeValue == 1);
    }

    public void updateProgramTransportStreamMap(int serviceID, int transportStreamID) {
        programTransportStreamMap.put(serviceID, transportStreamID);
    }

    public void decrTransportStreamSet(int transportStreamID) {
        transportStreamSet.remove(transportStreamID);
    }

    /*      NIT     */
    public void updateTransportStreamMap(int transportStreamID, int plpNum) {
        transportStreamMap.put(transportStreamID, plpNum>=0 ? plpNum : null);
    }

    public void updateNetworkName(String newNetworkName) {
        networkName = newNetworkName;
    }

    public void incrTransportStreamSet(int transportStreamID) {
        transportStreamSet.add(transportStreamID);
    }

    /*      StreamParser     */
    public Set<Integer> getPMTSet() {
        return PMTset;
    }

    public Set<Integer> getTransportStreamSet() {
        return transportStreamSet;
    }

}
