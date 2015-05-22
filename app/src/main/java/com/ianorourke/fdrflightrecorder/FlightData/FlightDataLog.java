package com.ianorourke.fdrflightrecorder.FlightData;

import com.ianorourke.fdrflightrecorder.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class FlightDataLog {
    String pressure;
    String temperature;

    String pilot;
    String plane;
    String tail;

    Calendar time;

    ArrayList<FlightDataEvent> dataEvents;

    public FlightDataLog(String pilot, String plane, String tail, String pressure, String temperature, Calendar time) {
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
        SimpleDateFormat zuluFormatter = new SimpleDateFormat("MM-dd-yyyy-HH-mm-ss");
        zuluFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return zuluFormatter.format(time.getTime());
    }
}
