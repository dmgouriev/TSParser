package ru.ts_parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class TSParser {

    public static void main(String[] args) throws Exception {
//        new TSParser().parse(new File("C:\\\\data\\mux_MYGICA_546000000_8000000_20171121083157.ts"));
        parse(new File("/home/dmgouriev/tuner/mux_MYGICA_498000000_8000000_20171206143943.ts")); //mux_MYGICA_546000000_8000000_20171121083157.ts"));
    }

    public static void parse(File file) {
        parse(getBytes(file));
    }

    public static void parse(byte[] buffer) {
        StreamParser parser = StreamParser.getInstance();
        parser.parse(buffer);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (parser.getState() == StreamParser.State.RUNNING) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                TSTableData result = parser.getResult();
                System.out.println(result.toString());
                System.exit(0);
            }
        }).start();
    }

    private static byte[] getBytes(File file) {
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
