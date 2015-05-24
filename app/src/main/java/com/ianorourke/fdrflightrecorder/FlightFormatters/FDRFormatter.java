package com.ianorourke.fdrflightrecorder.FlightFormatters;

import com.ianorourke.fdrflightrecorder.FlightData.FlightDataEvent;
import com.ianorourke.fdrflightrecorder.FlightData.FlightDataLog;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class FDRFormatter implements FlightFormatter {
    public final static  String FILE_EXT = ".fdr";

    @Override
    public String formatLog(FlightDataLog data) {

        StringBuilder logBuffer = new StringBuilder();

        logBuffer.append("A\n2\n");
        logBuffer.append('\n');

        logBuffer.append("COMM, This FDR File was created by FDR Flight Recorder\n");
        if (data.getPilot() != null && data.getPilot() != "") logBuffer.append("COMM, Pilot: " + data.getPilot() + "\n");
        if (data.getPlane() != null && data.getPlane() != "") logBuffer.append("COMM, Aircraft: " + data.getPlane() + "\n");
        logBuffer.append('\n');

        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
        timeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        String formattedDate = dateFormatter.format(data.getTime().getTime());
        String formattedTime = timeFormatter.format(data.getTime().getTime());

        logBuffer.append("DATE," + formattedDate + ",\n");
        logBuffer.append("TIME," + formattedTime + ",\n");

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

        builder.append("0,");

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
        builder.append("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,");
        builder.append("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,");
        builder.append("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,");
        builder.append("0,0,0,0,0,0,0,0,0,0,0,");

        builder.append('\n');
    }
}
