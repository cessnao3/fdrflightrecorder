package com.ianorourke.fdrflightrecorder.FlightData;

import android.support.annotation.NonNull;

import com.ianorourke.fdrflightrecorder.Database.FlightDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class FlightDataLog {
    String pressure;
    String temperature;

    String pilot;
    String plane;
    String tail;

    Calendar time;

    ArrayList<FlightDataEvent> dataEvents;

    public FlightDataLog(@NonNull String pilot,
                         @NonNull String plane,
                         @NonNull String tail,
                         @NonNull String pressure,
                         @NonNull String temperature,
                         @NonNull Calendar time) {
        this.pilot = pilot;
        this.plane = plane;
        this.tail = tail;

        this.pressure = pressure;
        this.temperature = temperature;
        this.time = time;

        dataEvents = new ArrayList<>();
    }

    public String getPilot() {
        return pilot;
    }

    public String getPlane() {
        return plane;
    }

    public String getTail() {
        return tail;
    }

    public Calendar getTime() {
        return time;
    }

    public String getPressure() {
        return pressure;
    }

    public String getTemperature() {
        return temperature;
    }

    public ArrayList<FlightDataEvent> getFlightDataEvents() {
        return dataEvents;
    }

    public void addFlightDataEvent(FlightDataEvent event) {
        dataEvents.add(event.clone());
    }

    public String getName() {
        return FlightDatabaseHelper.dateFormat.format(time.getTime());
    }

    public String getFilename() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(time.getTime());
    }
}
