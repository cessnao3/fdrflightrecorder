package com.ianorourke.fdrflightrecorder.FDR;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ian on 5/18/15.
 */
public class FDRFileSaver {
    private final File file;
    private final FDRLog log;

    private FileWriter fileWriter;

    public FDRFileSaver(File file, FDRLog log) {
        this.file = file;
        this.log = log;
    }

    public void open() throws IOException {
        if (fileWriter != null) return;

        fileWriter = new FileWriter(file, false);
    }

    public void checkWrite() throws IOException {

    }

    public void flush() throws IOException {

    }
}
