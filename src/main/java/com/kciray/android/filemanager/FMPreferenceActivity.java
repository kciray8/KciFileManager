package com.kciray.android.filemanager;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.kciray.android.commons.sys.root.FileMgr;

public class FMPreferenceActivity extends PreferenceActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        if(!FileMgr.busyboxAndRootAvailable()) {
            Preference somePreference = findPreference(MainActivity.getInstance().getStr(R.string.engine));
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            preferenceScreen.removePreference(somePreference);
        }
    }
}
