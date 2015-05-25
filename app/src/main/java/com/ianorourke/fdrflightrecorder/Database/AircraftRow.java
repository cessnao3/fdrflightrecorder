package com.ianorourke.fdrflightrecorder.Database;

/**
 * Created by ian on 5/24/15.
 */
public class AircraftRow implements Comparable<AircraftRow> {
    private String aircraft;
    private String tail;

    @Override
    public int compareTo(AircraftRow other) {
        if (this.aircraft.equalsIgnoreCase(other.aircraft))
            return this.tail.compareTo(other.tail);
        else
            return this.aircraft.compareTo(other.aircraft);
    }

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
