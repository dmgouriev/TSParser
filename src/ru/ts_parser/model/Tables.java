package ru.ts_parser.model;

import java.util.*;

public class Tables {

    private Map programMap;

    private Map<Integer, Integer> ESmap;
    private Map<Integer, Integer> PATmap;

    private Set<Integer> PMTset;
    private Set<Integer> SDTset;

    private Set<Integer> transportStreamIdSet;

    private Map<Integer, Integer> PMTmap;

    private Map serviceNamesMap;

    private Map programNameMap;
    private Map providerNameMap;

    private int currentTransportStreamID;

    private int currentPMTCount;

    public Tables() {
        this.programMap = new HashMap();

        this.PATmap = new HashMap<>();

        this.currentPMTCount = 0;

        this.PMTmap = new HashMap();

        this.PMTset = new HashSet();
        this.SDTset = new HashSet();

        this.transportStreamIdSet = new HashSet();

        this.serviceNamesMap = new HashMap();

        this.programNameMap = new HashMap();
        this.providerNameMap = new HashMap();

        this.currentTransportStreamID = -1;

        this.ESmap = new HashMap();
    }

    public int getCurrentTransportStreamID() {
        return currentTransportStreamID;
    }

    public void updateCurrentTransportStreamID(int transportStreamID) {
        this.currentTransportStreamID = transportStreamID;
    }

    public void updateTransportStreamIdSet(int transportStreamID) {
        this.transportStreamIdSet.add(transportStreamID);
    }

    public void updateES(Map<Integer, Integer> ESmap) {
        this.ESmap.putAll(ESmap);
    }

    public void updatePMT(Map<Integer, Integer> PMTmap) {
        this.PMTmap.putAll(PMTmap);
    }

    public Set<Integer> getPMTSet() {
        return this.PMTset;
    }

    public Set<Integer> getSDTSet() {
        return this.PMTset;
    }

    public void incrPMTSet(int PID) {
        this.PMTset.add(PID);
    }

    public boolean decrPMTSet(int PID) {
        return this.PMTset.remove(PID);
    }

    public void incrSDTSet(int PID) {
        this.PMTset.add(PID);
    }

    public boolean decrSDTSet(int PID) {
        return this.PMTset.remove(PID);
    }

    public void updatePAT(Map<Integer, Integer> PATmap, short versionNum) {
        this.PATmap = PATmap;
    }

    public void updateServiceName(int PID, String serviceName) {
        this.serviceNamesMap.put(PID, serviceName);
    }

    public void updateProgramMap(int serviceID, String serviceName) {
        programNameMap.put(serviceID, serviceName);
    }

    public void updateProviderMap(int serviceID, String providerName) {
        providerNameMap.put(serviceID, providerName);
    }

    public Map getProgramMap() {
        return programMap;
    }

    public Map<Integer, Integer> getPATmap() {
        return PATmap;
    }

    public Map getPMTmap() {
        return PMTmap;
    }

    public Map<Integer, Integer> getESmap() {
        return ESmap;
    }

    public Map getServiceNamesMap() {
        return serviceNamesMap;
    }

    public void setServiceNamesMap(Map serviceNamesMap) {
        this.serviceNamesMap = serviceNamesMap;
    }

    public void setPATmap(Map<Integer, Integer> PATmap) {
        this.PATmap = PATmap;
    }

    public void setPMTmap(Map PMTmap) {
        this.PMTmap = PMTmap;
    }

    public void setProgramMap(Map programMap) {
        this.programMap = programMap;
    }

    public Integer getPMTCount() {
        return currentPMTCount;
    }

    public void updatePMTCount() {
        this.currentPMTCount++;
    }

    public Map getProgramNameMap() {
        return programNameMap;
    }

    public void setProgramNameMap(Map programNameMap) {
        this.programNameMap = programNameMap;
    }

    public Map getProviderNameMap() {
        return providerNameMap;
    }

    public void setProviderNameMap(Map providerNameMap) {
        this.providerNameMap = providerNameMap;
    }
}
