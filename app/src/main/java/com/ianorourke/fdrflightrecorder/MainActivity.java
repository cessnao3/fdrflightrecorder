package com.ianorourke.fdrflightrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements MapReceiver.MapDataInterface {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    protected Marker mLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();

        MapReceiver.dataInterface = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    public void onStartStopClick(View v) {
        Button button = (Button) v;

        Intent serviceIntent = new Intent(getApplicationContext(), BackgroundLocationService.class);
        serviceIntent.putExtra(getString(R.string.service_soundstart), ((CheckBox) findViewById(R.id.checkbox_sound_start)).isChecked());

        if (!BackgroundLocationService.isRunning()) {
            getApplicationContext().startService(serviceIntent);
            button.setText("Stop Service");
            Log.v("FDR", "Service Started");
        } else {
            stopService(serviceIntent);
            button.setText("Start Service");
            Log.v("FDR", "Service Stopped");
        }
    }

    public void onLocationReceive(double lat, double lon) {
        if (mMap != null) {
            CameraUpdate cameraUpdate;

            if (mLocationMarker == null) {
                mLocationMarker = mMap.addMarker(new MarkerOptions().title("Current Location").position(new LatLng(lat, lon)));
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(mLocationMarker.getPosition(), 12.0f);
                mMap.moveCamera(cameraUpdate);
            } else {
                mLocationMarker.setPosition(new LatLng(lat, lon));
                cameraUpdate = CameraUpdateFactory.newLatLng(mLocationMarker.getPosition());
                mMap.animateCamera(cameraUpdate);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        MapReceiver.dataInterface = null;
    }
}
