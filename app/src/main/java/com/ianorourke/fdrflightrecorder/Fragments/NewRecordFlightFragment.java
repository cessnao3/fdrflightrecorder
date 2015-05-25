package com.ianorourke.fdrflightrecorder.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ianorourke.fdrflightrecorder.Database.AircraftRow;
import com.ianorourke.fdrflightrecorder.Database.FlightDatabaseHelper;
import com.ianorourke.fdrflightrecorder.R;
import com.ianorourke.fdrflightrecorder.Receivers.MapReceiver;
import com.ianorourke.fdrflightrecorder.Services.BackgroundLocationService;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

public class NewRecordFlightFragment extends Fragment implements MapReceiver.MapDataInterface, BackgroundLocationService.BackgroundLocationServiceInterface, FragmentTag {
    @Override
    public String getFragmentTag() {
        return "map_fragment";
    }

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private Marker mLocationMarker;
    private Polyline mPolyLine;

    private static ArrayList<LatLng> markerPoints = new ArrayList<>();

    private FloatingActionButton startStopButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_flight, container, false);

        setUpMapIfNeeded();

        MapReceiver.dataInterface = this;

        BackgroundLocationService.serviceInterface = this;

        startStopButton = (FloatingActionButton) v.findViewById(R.id.fab_new_flight);
        //startStopButton.show();

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

                final Intent serviceIntent = new Intent(getActivity(), BackgroundLocationService.class);

                if (BackgroundLocationService.isRunning()) {
                    getActivity().stopService(serviceIntent);
                } else {
                    markerPoints.clear();

                    mMap.clear();

                    if (mLocationMarker != null) mLocationMarker.remove();
                    mLocationMarker = null;

                    if (mPolyLine != null) mPolyLine.remove();
                    mPolyLine = null;

                    FlightDatabaseHelper databaseHelper = FlightDatabaseHelper.getInstance(getActivity());

                    final ArrayList<AircraftRow> aircraft = new ArrayList<>(databaseHelper.getAllAircraft());
                    String[] aircraftItems = new String[aircraft.size()];

                    for (int i = 0; i < aircraft.size(); ++i) {
                        AircraftRow current = aircraft.get(i);
                        aircraftItems[i] = current.getAircraft() + " - " + current.getTail();
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Record New Flight")
                            .setItems(aircraftItems, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    serviceIntent.putExtra(getString(R.string.service_soundstart), sharedPreferences.getBoolean(getString(R.string.pref_sound_start), false));
                                    serviceIntent.putExtra(getString(R.string.service_soundstop), sharedPreferences.getBoolean(getString(R.string.pref_sound_stop), false));
                                    serviceIntent.putExtra(getString(R.string.service_pilot_name), sharedPreferences.getString(getString(R.string.pref_pilot_name), ""));
                                    serviceIntent.putExtra(getString(R.string.service_plane_type), aircraft.get(which).getAircraft());
                                    serviceIntent.putExtra(getString(R.string.service_plane_tail), aircraft.get(which).getTail());

                                    getActivity().startService(serviceIntent);
                                }
                            })
                            .setCancelable(true)
                            .create().show();

                    //getActivity().startService(serviceIntent);
                }

                startStopButton.setEnabled(false);
            }
        });

        backgroundServiceChanged();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private SupportMapFragment getMapFragment() {
        FragmentManager fm;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            fm = getFragmentManager();
        } else {
            fm = getChildFragmentManager();
        }

        return (SupportMapFragment) fm.findFragmentById(R.id.map);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = getMapFragment().getMap();
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
    public void backgroundServiceChanged() {
        if (BackgroundLocationService.isRunning())
            //startStopButton.setImageDrawable(getActivity().getResources().getDrawable(android.R.drawable.ic_media_pause));
            startStopButton.setTag(android.R.drawable.ic_media_pause);
        else
            //startStopButton.setImageDrawable(getActivity().getResources().getDrawable(android.R.drawable.ic_media_play));
            startStopButton.setTag(android.R.drawable.ic_media_play);

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
