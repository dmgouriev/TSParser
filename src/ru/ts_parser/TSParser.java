package ru.ts_parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import static ru.ts_parser.MpegCommonData.nil;
import static ru.ts_parser.MpegCommonData.syncByte;
import static ru.ts_parser.MpegCommonData.tsPacketSize;

public class TSParser {

    public static void main(String[] args) throws Exception {
//        new TSParser().parse(new File("C:\\\\data\\mux_MYGICA_546000000_8000000_20171121083157.ts"));
        new TSParser().parse(new File("/home/dmgouriev/tuner/mux_MYGICA_546000000_8000000_20171127172515.ts")); //mux_MYGICA_546000000_8000000_20171121083157.ts"));
    }

    public void parse(File file) {
        parse(getBytes(file));
    }

    public static void parse(byte[] buffer) {
//        try {
//
//            for (int i = nil; i < buffer.length; i += tsPacketSize) { //prechádzanie celého transportného toku v cykle s posuvom veľkosi paketu
//
//                if (i == nil) { //na začiatku vyhľadá prvý transportný paket
//                    i = seekBeginning(buffer, 0);
//                    if (i == nil) { //ak sa nenašiel, skončí
//                        throw new IOException("File does not contain TS stream!");
//                    }
//                }
//                byte[] packetBuf = Arrays.copyOfRange(buffer, i, i + tsPacketSize);
//                if (!parser.parse(packetBuf)) {
//                    break;
//                };
//                
//            }
//            System.out.println(parser.isFinished());
//            return new TSData(parser.getTables());
//        } catch (IOException e) {
//            e.printStackTrace(System.err);
//            return null;
//        }

//        try {
//            try(PrintWriter out = new PrintWriter("/home/dmgouriev/tuner/bytes1.txt")) {
//                out.println( Tools.byteArrayToHex(buffer) );
//            }
        //01 C0 C2 C0 C1
        StreamParser parser = new StreamParser();
        parser.parse(buffer);
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//        }
    }

    private int seekBeginning(byte[] buffer, int i) {

        for (; i < buffer.length - tsPacketSize; i++) {
            if (buffer[i] == syncByte && buffer[i + tsPacketSize] == syncByte) {
                return i;
            }
        }
        return nil;
    }

    private byte[] getBytes(File file) {
        try {
            Path filepath = Paths.get(file.getAbsolutePath());
            if (filepath == null) {
                throw new IOException("File not found!");
            }
            BasicFileAttributes attr = Files.readAttributes(filepath, BasicFileAttributes.class);

            if (!attr.isRegularFile()) {
                throw new IOException("File not regular!");
            }
            if (file.length() > Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())) {//TODO not what i wanted
                throw new IOException("File too large!");
            } else {
                byte[] buffer;
                FileInputStream inputStream = new FileInputStream(file);
                buffer = new byte[Math.toIntExact(file.length())];

                if (inputStream.read(buffer) == -1) {
                    throw new IOException("EOF reached while trying to read the whole file");
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                return buffer;
            }
        } catch (Exception e) {
            return null;
        }
    }

}
