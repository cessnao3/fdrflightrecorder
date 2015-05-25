package com.ianorourke.fdrflightrecorder.Fragments;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;

import com.ianorourke.fdrflightrecorder.R;

public class PrefsFragment extends PreferenceFragment implements FragmentTag {
    //http://www.cs.dartmouth.edu/~campbell/cs65/lecture12/lecture12.html

    @Override
    public String getFragmentTag() {
        return "prefs_fragment";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
