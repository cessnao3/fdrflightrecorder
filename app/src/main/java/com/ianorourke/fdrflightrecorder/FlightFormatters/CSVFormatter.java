package com.ianorourke.fdrflightrecorder.FlightFormatters;

import com.ianorourke.fdrflightrecorder.FlightData.FlightDataEvent;
import com.ianorourke.fdrflightrecorder.FlightData.FlightDataLog;

public class CSVFormatter implements FlightFormatter {
    public final static  String FILE_EXT = ".csv";

    @Override
    public String formatLog(FlightDataLog data) {

        StringBuilder logBuffer = new StringBuilder();

        logBuffer.append("time,latitude,longitude,msl alt,heading,pitch,roll\n");

        for (FlightDataEvent event : data.getFlightDataEvents()) {
            appendDataEvent(logBuffer, event);
        }

        return logBuffer.toString();
    }

    private static void appendDataEvent(StringBuilder builder, FlightDataEvent event) {
        builder.append(event.getSeconds());
        builder.append(',');

        builder.append(event.getLat());
        builder.append(',');

        builder.append(event.getLon());
        builder.append(',');

        builder.append(event.getAltitude());
        builder.append(',');

        builder.append(event.getHeading());
        builder.append(',');

        builder.append(String.format("%.2f", event.getPitch()));
        builder.append(',');

        builder.append(String.format("%.2f", event.getRoll()));
        builder.append('\n');
    }
}