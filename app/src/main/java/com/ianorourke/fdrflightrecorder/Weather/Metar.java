package com.ianorourke.fdrflightrecorder.Weather;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class Metar {
    public Calendar getMetarTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Calendar c = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
            c.setTime(simpleDateFormat.parse(observation_time.trim()));
            return c;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public final static int METAR_TIME_ERROR = -10;

    public final String raw;
    public final String station;
    public final String observation_time;
    public final double temp_c;
    public final double dewpoint_c;
    public final int wind_dir;
    public final int wind_speed_kt;
    public final int wind_gust_kt;
    public final double visibility_smi;
    public final double pressure;

    public Metar(String raw, String station, String observation_time, double temp_c, double dewpoint_c, int wind_dir, int wind_speed_kt, int wind_gust_kt, double visibility_smi, double pressure) {
        this.raw = raw;
        this.station = station;
        this.observation_time = observation_time;
        this.temp_c = temp_c;
        this.dewpoint_c = dewpoint_c;
        this.wind_dir = wind_dir;
        this.wind_speed_kt = wind_speed_kt;
        this.wind_gust_kt = wind_gust_kt;
        this.visibility_smi = visibility_smi;
        this.pressure = pressure;

        //Log.v("FDR", "SUCCESS!");
    }

    public int MinutesSinceUpdate() {
        if (getMetarTime() == null) return METAR_TIME_ERROR;

        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeZone(getMetarTime().getTimeZone());

        //TODO: Fix Time Bug at Midnight-ish UTC

        long timeDifference = currentTime.getTimeInMillis() - getMetarTime().getTimeInMillis();
        return (int) (timeDifference / 1000) / 60;
    }
}