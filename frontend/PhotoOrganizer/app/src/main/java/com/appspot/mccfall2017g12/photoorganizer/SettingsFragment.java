package com.appspot.mccfall2017g12.photoorganizer;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Ilkka on 20.11.2017.
 */

//public static in example, why doesn't work that way?
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
