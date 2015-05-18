package com.ianorourke.fdrflightrecorder;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements MapReceiver.MapDataInterface, BackgroundLocationService.BackgroundLocationServiceInterface {
    //http://developer.android.com/guide/topics/data/data-storage.html

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private Marker mLocationMarker;
    private Polyline mPolyLine;

    private static ArrayList<LatLng> markerPoints = new ArrayList<>();

    private Button startStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();

        MapReceiver.dataInterface = this;

        BackgroundLocationService.serviceInterface = this;

        startStopButton = (Button) findViewById(R.id.button_start_stop);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

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

    private void setUpMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(34.738, -92.894), 4.0f));
    }

    public void onStartStopClick(View v) {
        Intent serviceIntent = new Intent(getApplicationContext(), BackgroundLocationService.class);
        serviceIntent.putExtra(getString(R.string.service_soundstart), ((CheckBox) findViewById(R.id.checkbox_sound_start)).isChecked());
        serviceIntent.putExtra(getString(R.string.service_soundstop), ((CheckBox) findViewById(R.id.checkbox_sound_stop)).isChecked());

        if (BackgroundLocationService.isRunning()) {
            stopService(serviceIntent);
        } else {
            markerPoints.clear();

            if (mLocationMarker != null) mLocationMarker.remove();
            mLocationMarker = null;

            if (mPolyLine != null) mPolyLine.remove();
            mPolyLine = null;

            startService(serviceIntent);
        }

        startStopButton.setEnabled(false);
    }

    @Override
    public void backgroundServiceStarted() {
        startStopButton.setText("Stop Service");
        startStopButton.setEnabled(true);
    }

    @Override
    public void backgroundServiceStopped() {
        startStopButton.setText("Start Service");
        startStopButton.setEnabled(true);
    }

    public void onLocationReceive(double lat, double lon) {
        LatLng currentLocation = new LatLng(lat, lon);

        if (mMap != null) {
            CameraUpdate cameraUpdate;

            if (mLocationMarker == null) {
                mLocationMarker = mMap.addMarker(new MarkerOptions().title("Current Location").position(currentLocation).draggable(false));
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(mLocationMarker.getPosition(), 12.0f);
                mMap.moveCamera(cameraUpdate);
            } else {
                mLocationMarker.setPosition(new LatLng(lat, lon));
                cameraUpdate = CameraUpdateFactory.newLatLng(mLocationMarker.getPosition());
                mMap.animateCamera(cameraUpdate);
            }

            markerPoints.add(currentLocation);

            if (mPolyLine == null) {
                mPolyLine = mMap.addPolyline(new PolylineOptions().addAll(markerPoints).width(12.0f));
            } else {
                mPolyLine.setPoints(markerPoints);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        MapReceiver.dataInterface = null;
    }
}
