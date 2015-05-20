package com.ianorourke.fdrflightrecorder;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class FlightSettings {
    private static SharedPreferences pilotPreferences;
    private static SharedPreferences planePreferences;

    private static String currentPilot;

    private static void updatePreferences(Context c) {
        Context app = c.getApplicationContext();

        if (pilotPreferences == null) pilotPreferences = c.getSharedPreferences(c.getString(R.string.prefs_pilots), c.MODE_PRIVATE);
        if (planePreferences == null) planePreferences = c.getSharedPreferences(c.getString(R.string.prefs_aircraft), c.MODE_PRIVATE);
    }

    public static void addPilot(Context c, String newPilot) {
        updatePreferences(c);

        TreeSet<String> pilots = new TreeSet<>(pilotPreferences.getStringSet(c.getString(R.string.prefs_pilots_all), new HashSet<String>()));
        pilots.add(newPilot);

        pilotPreferences.edit().putStringSet(c.getString(R.string.prefs_pilots_all), pilots).commit();
    }

    public static void removePilot(Context c, String removePilot) {
        updatePreferences(c);

        TreeSet<String> pilots = new TreeSet<>(pilotPreferences.getStringSet(c.getString(R.string.prefs_pilots_all), new HashSet<String>()));
        Iterator<String> i = pilots.iterator();

        while (i.hasNext()) {
            String name = i.next();
            if (name == removePilot)
                i.remove();
        }
        pilots.add(removePilot);

        pilotPreferences.edit().putStringSet(c.getString(R.string.prefs_pilots_all), pilots).commit();
    }

    public static String[] getPilots(Context c) {
        updatePreferences(c);

        Set<String> pilots = pilotPreferences.getStringSet(c.getString(R.string.prefs_pilots_all), new HashSet<String>());
        return pilots.toArray(new String[pilots.size()]);
    }

    public static String getCurrentPilot(Context c) {
        updatePreferences(c);
        return pilotPreferences.getString(c.getString(R.string.prefs_pilots_current), "");
    }
}
