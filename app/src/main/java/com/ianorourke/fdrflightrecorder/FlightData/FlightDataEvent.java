package com.ianorourke.fdrflightrecorder.FlightData;

public class FlightDataEvent {
    double seconds = 0;

    double temp = 0.0;

    double lon = 0.0;
    double lat = 0.0;
    int mslHeight = 0;

    int heading = 0;
    double pitch = 0.0;
    double roll = 0.0;

    double pressure = 29.92;

    public FlightDataEvent() {
        //Empty Constructor
    }

    public FlightDataEvent(FlightDataEvent event) {
        this.seconds = event.seconds;
        this.temp = event.temp;
        this.lon = event.lon;
        this.lat = event.lat;
        this.mslHeight = event.mslHeight;
        this.heading = event.heading;
        this.pitch = event.pitch;
        this.roll = event.roll;
        this.pressure = event.pressure;
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

    public void setPressure(double _pressure) {
        pressure = _pressure;
    }

    public double getPressure() {
        return pressure;
    }

    public void setTemperature(double _temperature) {
        temp = _temperature;
    }

    public double getTemperature() {
        return temp;
    }

    public FlightDataEvent clone() {
        return new FlightDataEvent(this);
    }
}
