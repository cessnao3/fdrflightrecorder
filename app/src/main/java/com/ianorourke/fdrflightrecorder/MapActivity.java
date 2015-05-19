package com.ianorourke.fdrflightrecorder;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class MapActivity extends Fragment implements MapReceiver.MapDataInterface, BackgroundLocationService.BackgroundLocationServiceInterface {
    //http://developer.android.com/guide/topics/data/data-storage.html

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private Marker mLocationMarker;
    private Polyline mPolyLine;

    private static ArrayList<LatLng> markerPoints = new ArrayList<>();

    private Button startStopButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflater.inflate(R.layout.fragment_map, container, false);
        //setContentView(R.layout.fragment_map);

        setUpMapIfNeeded();

        MapReceiver.dataInterface = this;

        BackgroundLocationService.serviceInterface = this;

        startStopButton = (Button) v.findViewById(R.id.button_start_stop);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serviceIntent = new Intent(getActivity(), BackgroundLocationService.class);
                serviceIntent.putExtra(getString(R.string.service_soundstart), ((CheckBox) getView().findViewById(R.id.checkbox_sound_start)).isChecked());
                serviceIntent.putExtra(getString(R.string.service_soundstop), ((CheckBox) getView().findViewById(R.id.checkbox_sound_stop)).isChecked());

                if (BackgroundLocationService.isRunning()) {
                    getActivity().stopService(serviceIntent);
                } else {
                    markerPoints.clear();

                    if (mLocationMarker != null) mLocationMarker.remove();
                    mLocationMarker = null;

                    if (mPolyLine != null) mPolyLine.remove();
                    mPolyLine = null;

                    getActivity().startService(serviceIntent);
                }

                startStopButton.setEnabled(false);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
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
