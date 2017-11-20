package com.appspot.mccfall2017g12.photoorganizer;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Ilkka on 20.11.2017.
 */

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
