package com.ianorourke.fdrflightrecorder.FlightData;

public class FlightDataEvent {
    double seconds = 0;

    double lon = 0.0;
    double lat = 0.0;
    int mslHeight = 0;

    int groundSpeed = 0;

    int heading = 0;
    double pitch = 0.0;
    double roll = 0.0;

    public FlightDataEvent() {
        //Empty Constructor
    }

    public FlightDataEvent(FlightDataEvent event) {
        this.seconds = event.seconds;
        this.lon = event.lon;
        this.lat = event.lat;
        this.mslHeight = event.mslHeight;
        this.heading = event.heading;
        this.pitch = event.pitch;
        this.roll = event.roll;
        this.groundSpeed = event.groundSpeed;
    }

    public void setSeconds(double s) {
        seconds = s;
    }

    public double getSeconds() {
        return seconds;
    }

    public void setLon(double _lon) {
        lon = _lon;
    }

    public double getLon() {
        return lon;
    }

    public void setLat(double _lat) {
        lat = _lat;
    }

    public double getLat() {
        return lat;
    }

    public void setAltitude(int alt) {
        mslHeight = alt;
    }

    public int getAltitude() {
        return mslHeight;
    }

    public void setGroundSpeed(int gs) {
        groundSpeed = gs;
    }

    public int getGroundSpeed() {
        return groundSpeed;
    }

    public void setHeading(int h) {
        heading = h;
    }

    public int getHeading() {
        return heading;
    }

    public void setPitch(double _pitch) {
        pitch = _pitch;
    }

    public double getPitch() {
        return pitch;
    }

    public void setRoll(double _roll) {
        roll = _roll;
    }

    public double getRoll() {
        return roll;
    }

    public FlightDataEvent clone() {
        return new FlightDataEvent(this);
    }
}
