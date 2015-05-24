package com.ianorourke.fdrflightrecorder.Database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(flight_name));

        return formatter.format(calendar.getTime());
    }

    public String getTitle() {
        return plane + " " + tail_number + ((pilot == null || pilot == "") ? "" : " - " + pilot);
    }
}