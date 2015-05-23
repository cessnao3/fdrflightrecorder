package com.ianorourke.fdrflightrecorder.FlightData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FlightRow {
    public long _id;
    public String flight_name;
    public String pilot;
    public String plane;
    public String tail_number;
    public String pressure;
    public String temperature;

    @Override
    public String toString() {
        return getDate();
    }

    public String getDate() {
        try {
            String file = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)).format(FlightDatabaseHelper.dateFormat.parse(flight_name));
            return file;
        } catch (ParseException e) {
            e.printStackTrace();
            return flight_name;
        }
    }

    public String getTitle() {
        return plane + " " + tail_number + " - " + pilot;
    }
}