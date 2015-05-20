package com.ianorourke.fdrflightrecorder;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class PilotFragment extends Fragment {

    private Spinner mNameSpinner;

    public PilotFragment() {
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
        final View mainView = inflater.inflate(R.layout.fragment_pilots, container, false);

        mNameSpinner = (Spinner) mainView.findViewById(R.id.pilot_spinner);
        setPilotNames();

        Button createPilotButton = (Button) mainView.findViewById(R.id.pilot_create_button);
        createPilotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText text = (EditText) mainView.findViewById(R.id.pilot_name);

                String name = text.getText().toString();
                FlightSettings.addPilot(getActivity(), name);

                setPilotNames();
            }
        });

        return mainView;
    }

    public void setPilotNames() {
        String[] pilotArray = FlightSettings.getPilots(getActivity());
        if (pilotArray.length == 0) {
            pilotArray = new String[1];
            pilotArray[0] = "Please Enter Pilot Name Below";
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, pilotArray);
        mNameSpinner.setAdapter(spinnerAdapter);
    }
}
