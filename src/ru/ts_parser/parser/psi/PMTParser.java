package ru.ts_parser.parser.psi;

import java.util.HashMap;
import java.util.Map;
import static ru.ts_parser.base.MpegCommonData.*;
import static ru.ts_parser.tools.Tools.binToInt;
import ru.ts_parser.entity.Tables;
import ru.ts_parser.entity.packet.Packet;
import ru.ts_parser.base.PSIParserAbstract;

/**
 *
 * @author dmgouriev
 */
public class PMTParser extends PSIParserAbstract {
    
    final int reserved = 2; //MPEG константа

    @Override
    protected void parseSection(Packet packet, byte[] sectionBinary, int sectionLength, int position, Tables tables) {
        int programNum = (int) binToInt(sectionBinary, position = 0, position += programNumberLength);

//        byte versionNum = (byte) binToInt(sectionBinary, position += reserved, position += versionNumLength);        
        position += 7;

        byte currentNextIndicator = sectionBinary[position++];
        if (currentNextIndicator != 1) {
            setParserState(null);
            return;
        }
        
//        byte sectionNum = (byte) binToInt(sectionBinary, position, position += sectionNumLength);
//        byte lastSectionNum = (byte) binToInt(sectionBinary, position, position += sectionNumLength);
//        short PCR_PID = (short) binToInt(sectionBinary, position += reserved + 1, position += PCR_PIDlength);
        position += 32;
        
        short programInfoLength = (short) binToInt(sectionBinary, position += (reserved*2), position += twelveLengthLength);

        int nLoopDescriptorsLength = programInfoLength * byteBinaryLength;
        position += nLoopDescriptorsLength;

        Map<Integer, Integer> ESmap = new HashMap<>();
        Map<Integer, Integer> PMTmap = new HashMap<>();
        int N = (sectionLength * byteBinaryLength) - CRClength;

        for (; position < N;) {
            int streamType = (int) binToInt(sectionBinary, position, position += streamTypeLength);
            int elementaryPID = (int) binToInt(sectionBinary, position += 3, position += elementaryPIDlength);
            int ESinfoLength = (int) binToInt(sectionBinary, position += 4, position += twelveLengthLength);
            position += ESinfoLength * byteBinaryLength;
            ESmap.put(elementaryPID, streamType);
            tables.getPMTmap().put(elementaryPID, programNum);
        }        
        
        tables.updateES(ESmap);
        tables.updatePMT(PMTmap);
        tables.decrPMTSet(packet.getPID());

        fullPackageBuffer = null;
        setParserState(null);
    }

    @Override
    protected PSI_TABLE_TYPE getParserPSITableType() {
        return PSI_TABLE_TYPE.PMT;
    }
    
}
