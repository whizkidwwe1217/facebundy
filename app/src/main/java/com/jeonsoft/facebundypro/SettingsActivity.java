package com.jeonsoft.facebundypro;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * Created by WendellWayne on 2/14/2015.
 */
public class SettingsActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SettingsFragment())
                .commit();
    }
}
