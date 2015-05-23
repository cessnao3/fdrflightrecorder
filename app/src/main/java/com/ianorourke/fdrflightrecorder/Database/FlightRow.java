package com.ianorourke.fdrflightrecorder.Database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class FlightRow {
    public long _id;
    public String flight_name;
    public String pilot;
    public String plane;
    public String tail_number;
    public String pressure;
    public String temperature;
    public boolean in_progress;

    @Override
    public String toString() {
        return getDate();
    }

    public String getDate() {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatter.format(FlightDatabaseHelper.dateFormat.parse(flight_name));
        } catch (ParseException e) {
            e.printStackTrace();
            return flight_name;
        }
    }

    public String getTitle() {
        return plane + " " + tail_number + " - " + pilot;
    }
}