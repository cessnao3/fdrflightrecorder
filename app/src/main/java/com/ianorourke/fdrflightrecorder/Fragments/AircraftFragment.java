package com.ianorourke.fdrflightrecorder.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.ianorourke.fdrflightrecorder.Database.AircraftRow;
import com.ianorourke.fdrflightrecorder.Database.FlightDatabaseHelper;
import com.ianorourke.fdrflightrecorder.R;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AircraftFragment extends Fragment {

    FloatingActionButton floatingActionButton;
    ArrayList<AircraftRow> aircraftRows;
    ArrayList<Map<String, String>> aircraftData;

    ListView listView;

    final static String[] AIRCRAFT_MENU_OPTIONS = {"Delete"};

    public AircraftFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_aircraft, container, false);

        listView = (ListView) v.findViewById(R.id.list_aircraft);
        listView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setItems(AIRCRAFT_MENU_OPTIONS, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                FlightDatabaseHelper databaseHelper = FlightDatabaseHelper.getInstance(getActivity().getApplicationContext());
                                                databaseHelper.removeAircraft(aircraftRows.get(position));

                                                UpdateLists();
                                            }
                                        }
                                    })
                                .setCancelable(true)
                                .create().show();

                        return true;
                    }
                });

        floatingActionButton = (FloatingActionButton) v.findViewById(R.id.fab_aircraft);
        floatingActionButton.attachToListView(listView);

        floatingActionButton.show();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater layoutInflater = getActivity().getLayoutInflater();

                final View alertView = layoutInflater.inflate(R.layout.alert_new_aircraft, null);

                builder.setView(alertView)
                        .setTitle("Add an Aircraft")
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FlightDatabaseHelper databaseHelper = FlightDatabaseHelper.getInstance(getActivity());

                                EditText aircraftType = (EditText) alertView.findViewById(R.id.aircraft_type);
                                EditText aircraftTail = (EditText) alertView.findViewById(R.id.aircraft_tail);

                                databaseHelper.addAircraft(aircraftType.getText().toString(), aircraftTail.getText().toString());

                                UpdateLists();
                            }
                        })
                        .setCancelable(true)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();
            }
        });

        aircraftRows = new ArrayList<>();
        aircraftData = new ArrayList<>();

        UpdateLists();

        return v;
    }

    public void UpdateLists() {
        List<AircraftRow> newRows = FlightDatabaseHelper.getInstance(getActivity().getApplicationContext()).getAllAircraft();

        aircraftRows.clear();
        aircraftData.clear();

        aircraftRows.addAll(newRows);

        Iterator<AircraftRow> i = aircraftRows.iterator();

        while (i.hasNext()) {
            AircraftRow row = i.next();

            Map<String, String> datum = new HashMap<>(2);
            datum.put("title", row.getAircraft());
            datum.put("subtitle", row.getTail());
            aircraftData.add(datum);
        }

        final SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                aircraftData,
                android.R.layout.simple_list_item_2,
                new String[] {"title", "subtitle"},
                new int[] {android.R.id.text1, android.R.id.text2});

        listView.setAdapter(adapter);
    }
}