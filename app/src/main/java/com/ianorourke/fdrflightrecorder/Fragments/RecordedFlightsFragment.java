package com.ianorourke.fdrflightrecorder.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.ianorourke.fdrflightrecorder.Database.FlightDatabaseHelper;
import com.ianorourke.fdrflightrecorder.Database.FlightRow;
import com.ianorourke.fdrflightrecorder.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RecordedFlightsFragment extends Fragment {

    ArrayList<FlightRow> flightRows;
    ArrayList<Map<String, String>> flightData;

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

        ListView listView = (ListView) v.findViewById(R.id.list_recorded_flights);

        flightRows = new ArrayList<>();
        flightData = new ArrayList<>();

        UpdateLists();

        final SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                flightData,
                android.R.layout.simple_list_item_2,
                new String[] {"title", "subtitle"},
                new int[] {android.R.id.text1, android.R.id.text2});

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), flightRows.get(position).flight_name, Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    public void UpdateLists() {
        flightRows.clear();
        flightData.clear();

        flightRows.addAll(FlightDatabaseHelper.getInstance(getActivity().getApplicationContext()).getFlightList());

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
    }


}
