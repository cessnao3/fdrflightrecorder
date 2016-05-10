package com.ianorourke.fdrflightrecorder.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ianorourke.fdrflightrecorder.R;
import com.ianorourke.fdrflightrecorder.Weather.GetMetarAsync;
import com.ianorourke.fdrflightrecorder.Weather.Metar;

import java.text.SimpleDateFormat;
import java.util.Locale;

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
            // Set the Raw METER View Screen

            TextView weatherView;

            try {
                weatherView = (TextView) getView().findViewById(R.id.weather_raw);
            } catch (NullPointerException e) {
                weatherView = null;
            }

            if (weatherView != null) {
                weatherView.setText(metar.raw);
            }

            // Set the Time View Display

            TextView timeView = (TextView) getView().findViewById(R.id.weather_time);

            if (timeView != null) {
                if (metar.getMetarTime() != null) {
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("MMMM dd, HH:mm 'Z'", Locale.US);
                    dateFormatter.setTimeZone(metar.getMetarTime().getTimeZone());

                    String dateString = getString(R.string.weather_time_label) + " ";
                    dateString += dateFormatter.format(metar.getMetarTime().getTime());
                    dateString += " (" + metar.MinutesSinceUpdate() + " min ago)";

                    timeView.setText(dateString);
                } else {
                    String timeNotAvailable = getString(R.string.weather_time_label) + "Not Available";
                    timeView.setText(timeNotAvailable);
                }
            }

            // Set the Wind Display
            TextView windView = (TextView) getView().findViewById(R.id.weather_wind);

            if (windView != null) {
                String windString = "";
                windString += (getString(R.string.weather_wind_label)) + " ";
                windString += metar.wind_speed_kt + " " + ((metar.wind_speed_kt == 1) ? "kt" : "kts");
                windString += " from " + ((metar.wind_dir < 100) ? "0" : "") + ((metar.wind_dir < 10) ? "0" : "") + metar.wind_dir;
                windString += (((metar.wind_gust_kt != 0) ? " gusting to " + metar.wind_gust_kt + ((metar.wind_gust_kt == 1) ? "kt" : "kts") : ""));

                windView.setText(windString);
            }
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