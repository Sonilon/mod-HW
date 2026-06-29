package com.chatguard.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class LogTailer {

    private final File file;
    private long pos = 0;

    public LogTailer(File file) {
        this.file = file;
    }

    public List<String> poll() {

        List<String> out = new ArrayList<>();

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

            if (pos > raf.length()) pos = 0;

            raf.seek(pos);

            String line;
            while ((line = raf.readLine()) != null) {
                out.add(new String(line.getBytes("ISO-8859-1"), "UTF-8"));
            }

            pos = raf.getFilePointer();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return out;
    }
}
