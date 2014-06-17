package com.kciray.android.commons.sys;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.File;

public class AppUtils {
    public static File getInternalDir() {
        PackageManager m = Global.getContext().getPackageManager();
        String s = Global.getContext().getPackageName();
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
        Intent i = Global.getContext().getPackageManager()
                .getLaunchIntentForPackage(Global.getContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Global.getContext().startActivity(i);
    }
}