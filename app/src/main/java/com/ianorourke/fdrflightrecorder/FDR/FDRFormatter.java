package com.ianorourke.fdrflightrecorder.FDR;

/**
 * Created by ian on 5/8/15.
 */
public class FDRFormatter {
    int seconds = 0;

    double temp = 0.0;

    double lon = 0.0;
    double lat = 0.0;
    int mslHeight = 0;

    int heading = 0;
    double pitch = 0.0;
    double roll = 0.0;

    double airspeed = 0.0;

    double pressure = 29.92;

    public void setSeconds(int s) {
        seconds = s;
    }

    public void setTemp(double _temp) {
        temp = _temp;
    }

    public void setLon(double _lon) {
        lon = _lon;
    }

    public void setLat(double _lat) {
        lat = _lat;
    }

    public void setAltitude(int alt) {
        mslHeight = alt;
    }

    public void setHeading(int h) {
        heading = h;
    }

    public void setPitch(double _pitch) {
        pitch = _pitch;
    }

    public void setRoll(double _roll) {
        roll = _roll;
    }

    public void setAirspeed(double _airspeed) {
        airspeed = _airspeed;
    }

    public void setPressure(double _pressure) {
        pressure = _pressure;
    }

    public String getData() {

        StringBuilder builder = new StringBuilder();

        builder.append("DATA,");

        builder.append(seconds);
        builder.append(',');

        builder.append(temp);
        builder.append(',');

        builder.append(lon);
        builder.append(',');

        builder.append(lat);
        builder.append(',');

        builder.append(mslHeight);
        builder.append(',');

        //Radar Height, Aileron Ratio, Elevator Ratio, Rudder Ratio
        builder.append("0,0,0,0,");

        builder.append(String.format("%.2f", pitch));
        builder.append(',');

        builder.append(String.format("%.2f", roll));
        builder.append(',');

        builder.append(heading);
        builder.append(',');

        builder.append(airspeed);
        builder.append(',');

        //Rates
        builder.append("0,0,0,0,0,0,0,0,0,0,");

        //Landing Gear - Down
        builder.append("1,1,1,1,");

        builder.append("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,");
        builder.append("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,");

        builder.append(pressure);
        builder.append(',');

        builder.append("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,");
        builder.append("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,");
        builder.append("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,");
        builder.append("0,0,0,0,0,0,0,0,0,0,");

        builder.append('\n');

        return builder.toString();
    }
}
