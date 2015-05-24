package com.ianorourke.fdrflightrecorder;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
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
import com.ianorourke.fdrflightrecorder.Receivers.MapReceiver;
import com.ianorourke.fdrflightrecorder.Services.BackgroundLocationService;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements MapReceiver.MapDataInterface, BackgroundLocationService.BackgroundLocationServiceInterface {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private Marker mLocationMarker;
    private Polyline mPolyLine;

    private static ArrayList<LatLng> markerPoints = new ArrayList<>();

    private Button startStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        setUpMapIfNeeded();

        MapReceiver.dataInterface = this;

        BackgroundLocationService.serviceInterface = this;

        startStopButton = (Button) findViewById(R.id.button_start_stop);

        backgroundServiceChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                                    // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(34.738, -92.894), 4.0f));
    }

    public void startStopButtonPressed(View v) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Intent serviceIntent = new Intent(this, BackgroundLocationService.class);
        serviceIntent.putExtra(getString(R.string.service_soundstart), sharedPreferences.getBoolean(getString(R.string.pref_sound_start), false));
        serviceIntent.putExtra(getString(R.string.service_soundstop), sharedPreferences.getBoolean(getString(R.string.pref_sound_stop), false));
        serviceIntent.putExtra(getString(R.string.service_pilot_name), sharedPreferences.getString(getString(R.string.pref_pilot_name), ""));

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
    public void backgroundServiceChanged() {
        if (BackgroundLocationService.isRunning())
            startStopButton.setText("Stop Service");
        else
            startStopButton.setText("Start Service");

        startStopButton.setEnabled(true);
    }

    public void onLocationReceive(LatLng location) {
        if (mMap != null) {
            CameraUpdate cameraUpdate;

            if (mLocationMarker == null) {
                mLocationMarker = mMap.addMarker(new MarkerOptions().title("Current Location").position(location).draggable(false));
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(mLocationMarker.getPosition(), 12.0f);
                mMap.moveCamera(cameraUpdate);
            } else {
                mLocationMarker.setPosition(location);
                cameraUpdate = CameraUpdateFactory.newLatLng(mLocationMarker.getPosition());
                mMap.animateCamera(cameraUpdate);
            }

            markerPoints.add(location);

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
