package com.ianorourke.fdrflightrecorder.Weather;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.ianorourke.fdrflightrecorder.R;

public class GetMetarAsync extends AsyncTask<Void, Void, Metar> {
    private static String baseUrl = "http://www.aviationweather.gov/adds/dataserver_current/httpparam?dataSource=metars&requestType=retrieve&format=xml";
    private static String stationString = "&stationString=";
    private static String hoursBeforeNow = "&hoursBeforeNow=";

    public static String station = "KPTK";
    public static String hours = "1";

    public interface MetarAsyncInterface {
        void metarReceived(Metar metar);
    }

    private boolean isRetry = false;

    MetarAsyncInterface metarAsyncInterface;
    Context mContext;

    public GetMetarAsync(Context c, MetarAsyncInterface callback_interface) {
        mContext = c;
        metarAsyncInterface = callback_interface;
    }

    protected GetMetarAsync(Context c, MetarAsyncInterface callback_interface, boolean isRetry) {
        this(c, callback_interface);
        this.isRetry = isRetry;
    }

    protected Metar doInBackground(Void... args) {
        MetarRetriever retriever = new MetarRetriever();

        String url = baseUrl + stationString + station + hoursBeforeNow + hours;

        return retriever.getLatestMETARReport(url);
    }

    protected void onPostExecute(Metar result) {
        if (result == null) {
            Log.e("FDR", "Metar Null Error");

            if (!isRetry)
                new GetMetarAsync(mContext, metarAsyncInterface, true).execute();
        } else {
            SetLatestMetar(mContext, result);
            Log.v("FDR", result.toString());
        }

        if (metarAsyncInterface != null)
            metarAsyncInterface.metarReceived(result);
    }

    private static Metar mLatestMetar;

    public static Metar GetLatestMetar(Context c) {
        if (mLatestMetar == null)
            return GetMetarFromPrefs(c);
        else
            return mLatestMetar;
    }

    private static Metar GetMetarFromPrefs(Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(c.getString(R.string.weather_pref_file), Context.MODE_PRIVATE);

        String raw = sharedPreferences.getString(c.getString(R.string.weather_last_raw), "");
        String station = sharedPreferences.getString(c.getString(R.string.weather_last_station), "");
        String observation = sharedPreferences.getString(c.getString(R.string.weather_last_observation_time), "");
        String temp_c = sharedPreferences.getString(c.getString(R.string.weather_last_temp_c), "");
        String dewpoint_c = sharedPreferences.getString(c.getString(R.string.weather_last_dewpoint_c), "");
        String wind_dir = sharedPreferences.getString(c.getString(R.string.weather_last_wind_dir), "");
        String wind_speed_kt = sharedPreferences.getString(c.getString(R.string.weather_last_wind_speed_kt), "");
        String gust_speed_kt = sharedPreferences.getString(c.getString(R.string.weather_last_wind_gust_kt), "");
        String visibility = sharedPreferences.getString(c.getString(R.string.weather_last_visibility_smi), "");
        String pressure = sharedPreferences.getString(c.getString(R.string.weather_last_pressure), "");

        return new Metar(
                raw,
                station,
                observation,
                temp_c,
                dewpoint_c,
                wind_dir,
                wind_speed_kt,
                gust_speed_kt,
                visibility,
                pressure
        );
    }

    private static void SetLatestMetar(Context c, Metar latest) {
        mLatestMetar = latest;

        SharedPreferences sharedPreferences = c.getSharedPreferences(c.getString(R.string.weather_pref_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(c.getString(R.string.weather_last_raw), latest.raw);
        editor.putString(c.getString(R.string.weather_last_station), latest.station);
        editor.putString(c.getString(R.string.weather_last_observation_time), latest.observation_time);
        editor.putString(c.getString(R.string.weather_last_temp_c), latest.temp_c);
        editor.putString(c.getString(R.string.weather_last_dewpoint_c), latest.dewpoint_c);
        editor.putString(c.getString(R.string.weather_last_wind_dir), latest.wind_dir);
        editor.putString(c.getString(R.string.weather_last_wind_speed_kt), latest.wind_speed_kt);
        editor.putString(c.getString(R.string.weather_last_wind_gust_kt), latest.wind_gust_kt);
        editor.putString(c.getString(R.string.weather_last_visibility_smi), latest.visibility_smi);
        editor.putString(c.getString(R.string.weather_last_pressure), latest.pressure);

        editor.apply();
    }
}