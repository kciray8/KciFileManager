package com.kciray.android.filemanager;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class FMPreferenceActivity extends PreferenceActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
