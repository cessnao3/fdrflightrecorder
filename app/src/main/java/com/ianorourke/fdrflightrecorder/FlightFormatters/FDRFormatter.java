package com.ianorourke.fdrflightrecorder.FlightFormatters;

import com.ianorourke.fdrflightrecorder.FlightData.FlightDataEvent;
import com.ianorourke.fdrflightrecorder.FlightData.FlightDataLog;

import java.text.SimpleDateFormat;

/**
 * Created by ian on 5/8/15.
 */
public class FDRFormatter {
    public final static  String FILE_EXT = ".fdr";

    public static String formatLog(FlightDataLog data) {

        StringBuilder logBuffer = new StringBuilder();

        logBuffer.append("A\n2\n");
        logBuffer.append('\n');

        logBuffer.append("COMM, This FDR File was created by FDR Flight Recorder\n");
        logBuffer.append('\n');

        String formattedDate = (new SimpleDateFormat("MM/dd/yyyy")).format(data.getTime().getTime());
        String formattedTime = (new SimpleDateFormat("HH:mm:ss")).format(data.getTime().getTime());

        logBuffer.append("DATE," + formattedDate + ",\n");
        logBuffer.append("TIME," + formattedTime + ",\n");

        if (data.getPlane() != null && data.getPlane() != "") logBuffer.append("ACFT," + data.getPlane() + ",\n");
        if (data.getTail() != null && data.getTail() != "") logBuffer.append("TAIL," + data.getTail() + ",\n");
        if (data.getPressure() != null && data.getPressure() != "") logBuffer.append("PRES," + data.getPressure() + ",\n");
        if (data.getTemperature() != null && data.getTemperature() != "") logBuffer.append("TEMP," + data.getTemperature() + ",\n");

        logBuffer.append('\n');

        for (FlightDataEvent event : data.getFlightDataEvents()) {
            appendDataEvent(logBuffer, event);
        }

        return logBuffer.toString();
    }

    private static void appendDataEvent(StringBuilder builder, FlightDataEvent event) {
        builder.append("DATA,");

        builder.append(event.getSeconds());
        builder.append(',');

        builder.append(event.getTemperature());
        builder.append(',');

        builder.append(event.getLon());
        builder.append(',');

        builder.append(event.getLat());
        builder.append(',');

        builder.append(event.getAltitude());
        builder.append(',');

        //Radar Height, Aileron Ratio, Elevator Ratio, Rudder Ratio
        builder.append("0,0,0,0,");

        builder.append(String.format("%.2f", event.getPitch()));
        builder.append(',');

        builder.append(String.format("%.2f", event.getRoll()));
        builder.append(',');

        builder.append(event.getHeading());
        builder.append(',');

        //Rates
        builder.append("0,0,0,0,0,0,0,0,0,0,0,");

        //Landing Gear - Down
        builder.append("1,1,1,1,");

        builder.append("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,");
        builder.append("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,");

        builder.append(event.getPressure());
        builder.append(',');

        builder.append("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,");
        builder.append("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,");
        builder.append("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,");
        builder.append("0,0,0,0,0,0,0,0,0,0,");

        builder.append('\n');
    }
}