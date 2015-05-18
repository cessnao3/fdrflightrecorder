package com.ianorourke.fdrflightrecorder.FDR;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by ian on 5/8/15.
 */
public class FDRLog {
    StringBuilder logBuffer;

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

    public void appendData(FDRFormatter logFile) {
        logBuffer.append(logFile.getData());
    }

    public String getLog() {
        return logBuffer.toString();
    }

    public StringBuilder getBuffer() {
        return logBuffer;
    }
}
