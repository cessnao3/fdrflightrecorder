package com.ianorourke.fdrflightrecorder.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.ianorourke.fdrflightrecorder.FlightData.FlightDatabaseHelper;
import com.ianorourke.fdrflightrecorder.FlightData.FlightRow;
import com.ianorourke.fdrflightrecorder.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordedFlightsFragment extends Fragment {

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

        ArrayList<FlightRow> rows = new ArrayList<>(FlightDatabaseHelper.getInstance(getActivity().getApplicationContext()).getFlightList());

        List<Map<String, String>> data = new ArrayList<>();
        for (FlightRow row : rows) {
            Map<String, String> datum = new HashMap<>(2);
            datum.put("title", row.getTitle());
            datum.put("subtitle", row.getDate());
            data.add(datum);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                getActivity(),
                data,
                android.R.layout.simple_list_item_2,
                new String[] {"title", "subtitle"},
                new int[] {android.R.id.text1, android.R.id.text2});

        listView.setAdapter(adapter);

        return v;
    }
}
