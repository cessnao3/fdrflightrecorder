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
import android.widget.CheckBox;
import android.widget.TextView;

import com.ianorourke.fdrflightrecorder.Database.AircraftRow;
import com.ianorourke.fdrflightrecorder.Database.FlightDatabaseHelper;
import com.ianorourke.fdrflightrecorder.FlightData.FlightDataEvent;
import com.ianorourke.fdrflightrecorder.R;
import com.ianorourke.fdrflightrecorder.Receivers.MapReceiver;
import com.ianorourke.fdrflightrecorder.Services.BackgroundLocationService;
import com.melnykov.fab.FloatingActionButton;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

public class RecordNewFlight extends Fragment implements MapReceiver.MapDataInterface, BackgroundLocationService.BackgroundLocationServiceInterface {

    private FloatingActionButton startStopButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_new_flight, container, false);

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
                    FlightDatabaseHelper databaseHelper = FlightDatabaseHelper.getInstance(getActivity());

                    final ArrayList<AircraftRow> aircraft = new ArrayList<>(databaseHelper.getAllAircraft());
                    String[] aircraftItems = new String[aircraft.size()];

                    for (int i = 0; i < aircraft.size(); ++i) {
                        AircraftRow current = aircraft.get(i);
                        aircraftItems[i] = current.getAircraft() + " - " + current.getTail();
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    final AlertDialog newFlightDialog = builder
                            .setTitle("Select Aircraft for Flight")
                            .setItems(aircraftItems, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    serviceIntent.putExtra(getString(R.string.service_soundstart), sharedPreferences.getBoolean(getString(R.string.pref_sound_start), false));
                                    serviceIntent.putExtra(getString(R.string.service_soundstop), sharedPreferences.getBoolean(getString(R.string.pref_sound_stop), false));
                                    serviceIntent.putExtra(getString(R.string.service_pilot_name), sharedPreferences.getString(getString(R.string.pref_pilot_name), ""));
                                    serviceIntent.putExtra(getString(R.string.service_plane_type), aircraft.get(which).getAircraft());
                                    serviceIntent.putExtra(getString(R.string.service_plane_tail), aircraft.get(which).getTail());
                                    serviceIntent.putExtra(getString(R.string.service_debug), sharedPreferences.getBoolean(getString(R.string.pref_debug_enabled), false));

                                    getActivity().startService(serviceIntent);
                                }
                            })
                            .setCancelable(true)
                            .create();

                    boolean shouldShowAlert = sharedPreferences.getBoolean(getString(R.string.pref_show_new_flight_alert), true);

                    if (shouldShowAlert) {
                        View alert_view = getActivity().getLayoutInflater().inflate(R.layout.alert_new_flight, null);
                        final CheckBox newBox = (CheckBox) alert_view.findViewById(R.id.checkbox_orientation_alert);

                        AlertDialog.Builder orientation_builder = new AlertDialog.Builder(getActivity());
                        orientation_builder
                                //.setTitle("Phone Orientation")
                                .setView(alert_view)
                                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        sharedPreferences.edit().putBoolean(getString(R.string.pref_show_new_flight_alert), newBox.isChecked()).apply();
                                        dialog.dismiss();
                                        newFlightDialog.show();
                                    }
                                }).create().show();
                    } else
                        newFlightDialog.show();

                    //getActivity().startService(serviceIntent);
                }

                startStopButton.setEnabled(false);
            }
        });

        backgroundServiceChanged();

        return v;
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

    public void onLocationReceive(FlightDataEvent dataEvent) {
        try {
            TextView locationView = (TextView) getView().findViewById(R.id.flight_loc);

            locationView.setText(String.format(Locale.US, "%.3f, %.3f", dataEvent.getLat(), dataEvent.getLon()));
        } catch (NullPointerException e) {
            // Do Nothing
        }

        try {
            TextView altView = (TextView) getView().findViewById(R.id.flight_alt);

            String alt_string = String.format(Locale.US, "%d ft", dataEvent.getAltitude());
            altView.setText(alt_string);
        } catch (NullPointerException e) {
            // Do Nothing
        }

        try {
            TextView rollView = (TextView) getView().findViewById(R.id.flight_roll);
            String roll_string = String.format(Locale.US, "%.2f deg", dataEvent.getRoll());
            rollView.setText(roll_string);
        } catch (NullPointerException e) {
            // Do Nothing
        }

        try {
            TextView pitchView = (TextView) getView().findViewById(R.id.flight_pitch);
            String pitch_string = String.format(Locale.US, "%.2f deg", dataEvent.getPitch());
            pitchView.setText(pitch_string);
        } catch (NullPointerException e) {
            // Do Nothing
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        MapReceiver.dataInterface = null;
    }
}
