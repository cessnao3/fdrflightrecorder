package com.ianorourke.fdrflightrecorder.Weather;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MetarRetriever {


    public Metar getLatestMETARReport(String urlString) {
        Metar tempMetar = null;

        Log.v("FDR", urlString);

        try {
            URL url = new URL(urlString);

            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();

            httpConnection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));

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

        Log.v("FDR", tempMetar.toString());
        return tempMetar;
    }

    private static class METAR_TAGS {
        public static String RAW_TEXT = "raw_text";
        public static String STATION_ID = "station_id";
        public static String OBSERVATION_TIME = "observation_time";
        public static String TEMP_C = "temp_c";
        public static String DEWPOINT_C = "dewpoint_c";
        public static String WIND_DIR = "wind_dir_degrees";
        public static String WIND_SPEED_KT = "wind_speed_kt";
        public static String WIND_GUST_KT = "wind_gust_kt";
        public static String VISIBILITY_S_MI = "visibility_statute_mi";
        public static String ALTIM_IN_HG = "altim_in_hg";
    }

    public Metar parseWithXmlBuffer(BufferedReader xmlInput) throws XmlPullParserException, IOException {
        XmlPullParser xmlParser = Xml.newPullParser();
        xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

        xmlParser.setInput(xmlInput);
        int eventType = xmlParser.getEventType();

        StringBuilder raw = new StringBuilder();
        StringBuilder station = new StringBuilder();
        StringBuilder observation_time = new StringBuilder();
        StringBuilder temp_c = new StringBuilder();
        StringBuilder dewpoint_c = new StringBuilder();
        StringBuilder wind_dir = new StringBuilder();
        StringBuilder wind_speed_kt = new StringBuilder();
        StringBuilder wind_gust_kt = new StringBuilder();
        StringBuilder visibility_smi = new StringBuilder();
        StringBuilder pressure = new StringBuilder();

        String startTag = "";

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                startTag = xmlParser.getName();
            } else if (eventType == XmlPullParser.TEXT) {
                String text = xmlParser.getText();

                if (startTag.equalsIgnoreCase(METAR_TAGS.RAW_TEXT))
                    raw.append(text);
                else if (startTag.equalsIgnoreCase(METAR_TAGS.STATION_ID))
                    station.append(text);
                else if (startTag.equalsIgnoreCase(METAR_TAGS.OBSERVATION_TIME))
                    observation_time.append(text);
                else if (startTag.equalsIgnoreCase(METAR_TAGS.TEMP_C))
                    temp_c.append(text);
                else if (startTag.equalsIgnoreCase(METAR_TAGS.DEWPOINT_C))
                    dewpoint_c.append(text);
                else if (startTag.equalsIgnoreCase(METAR_TAGS.WIND_DIR))
                    wind_dir.append(text);
                else if (startTag.equalsIgnoreCase(METAR_TAGS.WIND_SPEED_KT))
                    wind_speed_kt.append(text);
                else if (startTag.equalsIgnoreCase(METAR_TAGS.WIND_GUST_KT))
                    wind_gust_kt.append(text);
                else if (startTag.equalsIgnoreCase(METAR_TAGS.VISIBILITY_S_MI))
                    visibility_smi.append(text);
                else if (startTag.equalsIgnoreCase(METAR_TAGS.ALTIM_IN_HG))
                    pressure.append(text);
                else;
                    //Log.v("FDR", "Extra Tag: " + startTag);
            } else if (eventType == XmlPullParser.END_TAG) {
                if (xmlParser.getName().equalsIgnoreCase("METAR")) break;
            }

            eventType = xmlParser.next();
        }

        return new Metar(
                raw.toString(),
                station.toString(),
                observation_time.toString(),
                temp_c.toString(),
                dewpoint_c.toString(),
                wind_dir.toString(),
                wind_speed_kt.toString(),
                wind_gust_kt.toString(),
                visibility_smi.toString(),
                pressure.toString()
        );
    }
}