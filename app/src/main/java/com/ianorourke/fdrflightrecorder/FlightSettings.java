package com.ianorourke.fdrflightrecorder;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class FlightSettings {
    private static SharedPreferences pilotPreferences;
    private static SharedPreferences planePreferences;

    private static ArrayList<String> pilots;
    private static String currentPilot;

    private static void updatePreferences(Context c) {
        Context app = c.getApplicationContext();

        if (pilotPreferences == null) pilotPreferences = c.getSharedPreferences(c.getString(R.string.prefs_pilots), c.MODE_PRIVATE);
        if (planePreferences == null) planePreferences = c.getSharedPreferences(c.getString(R.string.prefs_aircraft), c.MODE_PRIVATE);
    }

    private static void updatePilotsIfNecessary(Set<String> set) {
        if (pilots == null)
            pilots = new ArrayList<>(set);
        else {
            pilots.clear();
            pilots.addAll(set);
        }
    }

    public static void addPilot(Context c, String newPilot) {
        updatePreferences(c);

        TreeSet<String> pilotSet = new TreeSet<>(pilotPreferences.getStringSet(c.getString(R.string.prefs_pilots_all), new HashSet<String>()));
        pilotSet.add(newPilot);

        updatePilotsIfNecessary(pilotSet);

        pilotPreferences.edit().putStringSet(c.getString(R.string.prefs_pilots_all), pilotSet).commit();
    }

    public static void removePilot(Context c, String removePilot) {
        updatePreferences(c);

        TreeSet<String> pilotSet = new TreeSet<>(pilotPreferences.getStringSet(c.getString(R.string.prefs_pilots_all), new HashSet<String>()));
        Iterator<String> i = pilotSet.iterator();

        while (i.hasNext()) {
            String name = i.next();
            if (name == removePilot)
                i.remove();
        }

        updatePilotsIfNecessary(pilotSet);

        pilotPreferences.edit().putStringSet(c.getString(R.string.prefs_pilots_all), pilotSet).commit();
    }

    public static ArrayList<String> getPilots(Context c) {
        updatePreferences(c);

        Set<String> pilotSet = pilotPreferences.getStringSet(c.getString(R.string.prefs_pilots_all), new HashSet<String>());
        updatePilotsIfNecessary(pilotSet);

        return pilots;
    }

    public static String getCurrentPilot(Context c) {
        updatePreferences(c);
        return pilotPreferences.getString(c.getString(R.string.prefs_pilots_current), "");
    }
}
