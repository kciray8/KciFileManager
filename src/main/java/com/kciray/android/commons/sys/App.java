package com.kciray.android.commons.sys;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.File;

public class App {
    public static File getInternalDir() {
        PackageManager m = Global.context.getPackageManager();
        String s = Global.context.getPackageName();
        PackageInfo p = null;
        try {
            p = m.getPackageInfo(s, 0);
            return new File(p.applicationInfo.dataDir);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void restart(){
        Intent i = Global.context.getPackageManager()
                .getLaunchIntentForPackage(Global.context.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Global.context.startActivity(i);
    }
}