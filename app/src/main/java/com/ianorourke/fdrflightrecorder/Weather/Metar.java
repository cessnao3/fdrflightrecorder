package com.ianorourke.fdrflightrecorder.Weather;


import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Metar {
    public Calendar getMetarTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
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

    public final String raw;
    public final String station;
    public final String observation_time;
    public final String temp_c;
    public final String dewpoint_c;
    public final String wind_dir;
    public final String wind_speed_kt;
    public final String wind_gust_kt;
    public final String visibility_smi;
    public final String pressure;

    public Metar(String raw, String station, String observation_time, String temp_c, String dewpoint_c, String wind_dir, String wind_speed_kt, String wind_gust_kt, String visibility_smi, String pressure) {
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
}