package com.ianorourke.fdrflightrecorder.Weather;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class METARRetriever {

    public static class GetMetarAsync extends AsyncTask<Void, Void, METAR> {
        protected METAR doInBackground(Void... args) {
            METARRetriever retriever = new METARRetriever();
            return retriever.getLatestMETARReport("test", "test");
        }

        protected void onPostExecute(METAR result) {
            Log.v("FDR", "Successful Mission");
        }
    }

    public METAR getLatestMETARReport(String urlString, String request) {
        METAR tempMetar = null;

        try {
            URL url = new URL(urlString);

            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();

            httpConnection.setRequestMethod("POST");

            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);

            System.setProperty("http.keepAlive", "false");
            httpConnection.setRequestProperty("Content-Type", "text/xml");

            httpConnection.setUseCaches(true);

            httpConnection.setReadTimeout(10000);

            httpConnection.getOutputStream().write(request.getBytes());
            if (httpConnection.getOutputStream() != null) httpConnection.getOutputStream().close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream(), "UTF-8"));

            try {
                tempMetar = parseWithXmlBuffer(reader);
            } catch (Exception e) {
                e.printStackTrace();
            }

            httpConnection.getInputStream().close();
            httpConnection.disconnect();
        } catch (Exception e) {
            Log.v("FDR", e.toString());
        }

        return tempMetar;
    }

    private static class METAR_TAGS {
        public static String RAW_TEXT = "raw_text";
        public static String STATION_ID = "station_id";
        public static String OBSERVATION_TIME = "observation_time";
        public static String TEMP_C = "temp_c";
        public static String DEWPOINT_C = "dewpoint_c";
        public static String WIND_DIR = "wind_dir";
        public static String WIND_SPEED_KT = "wind_speed_kt";
        public static String WIND_GUST_KT = "wind_gust_kt";
        public static String VISIBILITY_S_MI = "visibility_statute_mi";
        public static String ALTIM_IN_HG = "altim_in_hg";
    }

    public METAR parseWithXmlBuffer(BufferedReader xmlInput) throws XmlPullParserException, IOException {
        XmlPullParser xmlParser = Xml.newPullParser();
        xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

        xmlParser.setInput(xmlInput);
        int eventType = xmlParser.getEventType();

        String raw = "";
        String station = "";
        String observation_time = "";
        String temp_c = "";
        String dewpoint_c = "";
        String wind_dir = "";
        String wind_speed_kt = "";
        String wind_gust_kt = "";
        String visibility_smi = "";
        String pressure = "";

        String startTag = "";

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG)
                startTag = xmlParser.getName();
            else if (eventType == XmlPullParser.TEXT) {
                String text = xmlParser.getText();

                if (startTag.equalsIgnoreCase(METAR_TAGS.RAW_TEXT))
                    raw = text;
                else if (startTag.equalsIgnoreCase(METAR_TAGS.STATION_ID))
                    station = text;
                else if (startTag.equalsIgnoreCase(METAR_TAGS.OBSERVATION_TIME))
                    observation_time = text;
                else if (startTag.equalsIgnoreCase(METAR_TAGS.TEMP_C))
                    temp_c = text;
                else if (startTag.equalsIgnoreCase(METAR_TAGS.DEWPOINT_C))
                    dewpoint_c = "";
                else if (startTag.equalsIgnoreCase(METAR_TAGS.WIND_DIR))
                    wind_dir = text;
                else if (startTag.equalsIgnoreCase(METAR_TAGS.WIND_SPEED_KT))
                    wind_speed_kt = text;
                else if (startTag.equalsIgnoreCase(METAR_TAGS.WIND_GUST_KT))
                    wind_gust_kt = text;
                else if (startTag.equalsIgnoreCase(METAR_TAGS.VISIBILITY_S_MI))
                    visibility_smi = text;
                else if (startTag.equalsIgnoreCase(METAR_TAGS.ALTIM_IN_HG))
                    pressure = text;
                else
                    Log.v("FDR", "Extra Tag: " + startTag);
            }
        }

        return new METAR(raw, station, observation_time, temp_c, dewpoint_c, wind_dir, wind_speed_kt, wind_gust_kt, visibility_smi, pressure);
    }
}
