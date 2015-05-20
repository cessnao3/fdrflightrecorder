package com.ianorourke.fdrflightrecorder.FDR;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ian on 5/8/15.
 */
public class FDRLog {
    StringBuilder logBuffer;

    FileWriter fileWriter;
    File file;

    public FDRLog(Calendar time, String aircraft, String tail, String pressure, String temp) {
        logBuffer = new StringBuilder();

        logBuffer.append("A\n2\n");
        logBuffer.append('\n');

        logBuffer.append("COMM, This FDR File was created by FDR Flight Recorder\n");
        logBuffer.append('\n');

        String formattedDate = (new SimpleDateFormat("MM/dd/yyyy")).format(time.getTime());
        String formattedTime = (new SimpleDateFormat("HH:mm:ss")).format(time.getTime());

        logBuffer.append("DATE," + formattedDate + ",\n");
        logBuffer.append("TIME," + formattedTime + ",\n");

        if (aircraft != null && aircraft != "") logBuffer.append("ACFT," + aircraft + ",\n");
        if (tail != null && tail != "") logBuffer.append("TAIL," + tail + ",\n");
        if (pressure != null && pressure != "") logBuffer.append("PRES," + pressure + ",\n");
        if (temp != null && temp != "") logBuffer.append("TEMP," + temp + ",\n");

        logBuffer.append('\n');
    }

    public FDRLog(Calendar time, String aircraft, String tail, String pressure, String temp, File file) {
        this(time, aircraft, tail, pressure, temp);
        this.file = file;

        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(logBuffer.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void appendData(FDRFormatter logFile) {
        logBuffer.append(logFile.getData());

        if (fileWriter != null) {
            try {
                fileWriter.write(logFile.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getLog() {
        return logBuffer.toString();
    }

    public void close() {
        if (fileWriter != null) {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fileWriter = null;
    }
}
