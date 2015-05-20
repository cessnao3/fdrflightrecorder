package com.ianorourke.fdrflightrecorder.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.ianorourke.fdrflightrecorder.R;

/**
 * Created by ian on 5/13/15.
 */
public class MapReceiver extends BroadcastReceiver {
    public interface MapDataInterface {
        void onLocationReceive(LatLng location);
    }

    public static MapDataInterface dataInterface = null;

    @Override
    public void onReceive(Context c, Intent i) {
        double lat = i.getDoubleExtra(c.getString(R.string.map_lat), 0.0);
        double lon = i.getDoubleExtra(c.getString(R.string.map_lon), 0.0);



        if (dataInterface != null) dataInterface.onLocationReceive(new LatLng(lat, lon));
    }
}
