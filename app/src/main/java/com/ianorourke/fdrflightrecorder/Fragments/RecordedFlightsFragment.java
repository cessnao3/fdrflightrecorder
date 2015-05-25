package com.ianorourke.fdrflightrecorder.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.ianorourke.fdrflightrecorder.Database.FlightDatabaseHelper;
import com.ianorourke.fdrflightrecorder.Database.FlightRow;
import com.ianorourke.fdrflightrecorder.FlightFormatters.FDRFormatter;
import com.ianorourke.fdrflightrecorder.FlightFormatters.WriteLog;
import com.ianorourke.fdrflightrecorder.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RecordedFlightsFragment extends Fragment {

    private static String[] RECORDED_MENU_OPTIONS = {"Export Flight", "Delete Flight"};

    ArrayList<FlightRow> flightRows;
    ArrayList<Map<String, String>> flightData;

    ListView listView;

    public RecordedFlightsFragment() {
        //Empty Constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_recorded_flights, container, false);

        listView = (ListView) v.findViewById(R.id.list_recorded_flights);

        flightRows = new ArrayList<>();
        flightData = new ArrayList<>();

        UpdateLists();

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(RECORDED_MENU_OPTIONS, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FlightDatabaseHelper databaseHelper = FlightDatabaseHelper.getInstance(getActivity().getApplicationContext());

                                    if (which == 0) {
                                        FDRFormatter fdrFormatter = new FDRFormatter();
                                        WriteLog.saveLog(getActivity(), databaseHelper.getFlight(flightRows.get(position)), fdrFormatter);
                                    } else if (which == 1) {
                                            databaseHelper.removeFlight(flightRows.get(position));

                                            UpdateLists();
                                        }
                                    }
                                })
                        .setCancelable(true)
                        .create().show();

                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), flightRows.get(position).flight_name, Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    public void UpdateLists() {
        List<FlightRow> newRows = FlightDatabaseHelper.getInstance(getActivity().getApplicationContext()).getFlightList();
        Collections.reverse(newRows);

        flightRows.clear();
        flightData.clear();

        flightRows.addAll(newRows);

        Iterator<FlightRow> i = flightRows.iterator();

        while (i.hasNext()) {
            FlightRow row = i.next();

            if (row.in_progress) {
                i.remove();
            } else {
                Map<String, String> datum = new HashMap<>(2);
                datum.put("title", row.getTitle());
                datum.put("subtitle", row.getDate());
                flightData.add(datum);
            }
        }

        final SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                flightData,
                android.R.layout.simple_list_item_2,
                new String[] {"title", "subtitle"},
                new int[] {android.R.id.text1, android.R.id.text2});

        listView.setAdapter(adapter);
    }


}
