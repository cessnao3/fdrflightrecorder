package com.ianorourke.fdrflightrecorder.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ianorourke.fdrflightrecorder.R;
import com.ianorourke.fdrflightrecorder.Weather.GetMetarAsync;
import com.ianorourke.fdrflightrecorder.Weather.Metar;

public class WeatherFragment extends Fragment implements GetMetarAsync.MetarAsyncInterface {

    public WeatherFragment() {
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
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    public void metarReceived(Metar metar) {
        if (metar != null)
            ((TextView) getView().findViewById(R.id.weather_raw_view)).setText(metar.raw);

        Log.v("FDR", "Time: " + metar.getMetarTime());
    }

    @Override
    public void onResume() {
        super.onResume();

        ((TextView) getView().findViewById(R.id.weather_raw_view)).setText(GetMetarAsync.GetLatestMetar(getActivity()).raw);

        new GetMetarAsync(getActivity(), this).execute();
    }
}