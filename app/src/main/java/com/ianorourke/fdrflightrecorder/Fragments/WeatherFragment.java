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

import java.text.SimpleDateFormat;
import java.util.Calendar;

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
        new GetMetarAsync(getActivity(), this).execute();
        return inflater.inflate(R.layout.fragment_weather, container, false);
    }

    public void metarReceived(Metar metar) {
        if (metar != null) {
            ((TextView) getView().findViewById(R.id.weather_raw_view)).setText(metar.raw);

            if (metar.getMetarTime() != null) {
                SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM dd, HH:mm 'Z'");
                dateFormatter.setTimeZone(metar.getMetarTime().getTimeZone());

                ((TextView) getView().findViewById(R.id.weather_time)).setText(getString(R.string.weather_time_label) + dateFormatter.format(metar.getMetarTime().getTime()) + " (" + metar.MinutesSinceUpdate() + " min ago)");
            } else {
                ((TextView) getView().findViewById(R.id.weather_time)).setText(getString(R.string.weather_time_label) + "Not Available");
            }

            ((TextView) getView().findViewById(R.id.weather_wind)).setText(getString(R.string.weather_wind_label) + metar.wind_speed_kt + " " + ((metar.wind_speed_kt == 1) ? "kt" : "kts") + " from " + metar.wind_dir + ((metar.wind_gust_kt != 0) ? " gusting to " + metar.wind_gust_kt + ((metar.wind_gust_kt == 1) ? "kt" : "kts") : ""));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Metar latestMetar = GetMetarAsync.GetLatestMetar(getActivity());
        metarReceived((latestMetar));

        new GetMetarAsync(getActivity(), this).execute();
    }
}