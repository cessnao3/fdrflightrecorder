package com.ianorourke.fdrflightrecorder.UdpReader;

/**
 * UDPVals
 *
 * Created by cessn on 5/26/2016.
 */
public class UDPVals {
    public final String title;
    public final double lat;
    public final double lon;
    public final double alt;
    public final double airspeed;
    public final double pitch;
    public final double bank;
    public final double heading;

    private final int NUM_VALS = 8;

    public UDPVals(String vals) throws Exception {
        String[] vals_split = (vals.replace(";", "")).split(",");

        if (NUM_VALS != vals_split.length) throw new Exception("Unable to Create UDPVals - Number Mismatch");

        title = vals_split[0];
        lat = Double.parseDouble(vals_split[1]);
        lon = Double.parseDouble(vals_split[2]);
        alt = Double.parseDouble(vals_split[3]);
        airspeed = Double.parseDouble(vals_split[4]);
        pitch = Double.parseDouble(vals_split[5]);
        bank = Double.parseDouble(vals_split[6]);
        heading = Double.parseDouble(vals_split[7]);
    }
}
