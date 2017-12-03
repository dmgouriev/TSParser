/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ts_parser.parser;

import ru.ts_parser.MpegCommonData;

/**
 *
 * @author dmgouriev
 */
public class ParserState {
    
    MpegCommonData.PSI_TABLE_TYPE type;
    boolean needContinue = false;
    int lastPID = -1;

    public ParserState() {
        this.type = null;
    }
    
    public ParserState(MpegCommonData.PSI_TABLE_TYPE type, int lastPID) {
        this.type = type;
        this.lastPID = lastPID;
    }

        
    public MpegCommonData.PSI_TABLE_TYPE getType() {
        return type;
    }

    public int getLastPID() {
        return lastPID;
    }   
    
    public void setType(MpegCommonData.PSI_TABLE_TYPE type) {
        this.type = type;
    }

    public boolean needContinue() {
        return needContinue;
    }

    public void setNeedContinue(boolean needContinue) {
        this.needContinue = needContinue;
    }
    
}
