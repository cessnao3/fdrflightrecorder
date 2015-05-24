package com.ianorourke.fdrflightrecorder.Database;

/**
 * Created by ian on 5/24/15.
 */
public class AircraftRow {
    private String aircraft;
    private String tail;

    public AircraftRow(String aircraft, String tail) {
        this.aircraft = aircraft;
        this.tail = tail;
    }

    public String getAircraft() {
        return aircraft;
    }

    public String getTail() {
        return tail;
    }
}
