package com.ianorourke.fdrflightrecorder.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ianorourke.fdrflightrecorder.FlightData.FlightDataEvent;
import com.ianorourke.fdrflightrecorder.R;

public class MapReceiver extends BroadcastReceiver {
    public interface MapDataInterface {
        void onLocationReceive(FlightDataEvent dataEvent);
    }

    public static MapDataInterface dataInterface = null;

    @Override
    public void onReceive(Context c, Intent i) {
        double lat = i.getDoubleExtra(c.getString(R.string.flight_lat), 0.0);
        double lon = i.getDoubleExtra(c.getString(R.string.flight_lon), 0.0);

        double roll = i.getDoubleExtra(c.getString(R.string.flight_roll), 0.0);
        double pitch = i.getDoubleExtra(c.getString(R.string.flight_pitch), 0.0);
        int alt = i.getIntExtra(c.getString(R.string.flight_alt), 0);
        int gspeed = i.getIntExtra(c.getString(R.string.flight_ground_speed), 0);
        int heading = i.getIntExtra(c.getString(R.string.flight_heading), 0);

        double time = i.getDoubleExtra(c.getString(R.string.flight_rec_time), 0.0);

        FlightDataEvent event = new FlightDataEvent();
        event.setLat(lat);
        event.setLon(lon);
        event.setRoll(roll);
        event.setPitch(pitch);
        event.setAltitude(alt);
        event.setGroundSpeed(gspeed);
        event.setHeading(heading);
        event.setSeconds(time);

        if (dataInterface != null) dataInterface.onLocationReceive(event);
    }
}
