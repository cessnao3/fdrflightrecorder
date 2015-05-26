package com.ianorourke.fdrflightrecorder.Weather;


import android.util.Log;

public class METAR {
    public METAR(String raw, String station, String observation_time, String temp_c, String dewpoint_c, String wind_dir, String wind_speed_kt, String wind_gust_kt, String visibility_smi, String pressure) {
        Log.v("FDR", "SUCCESS!");
    }
}