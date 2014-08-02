package com.kciray.android.commons.sys;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Looper;

public class AppUtils {
    public static String getInternalDir() {
        PackageManager m = Global.getContext().getPackageManager();
        String s = Global.getContext().getPackageName();
        PackageInfo p = null;
        try {
            p = m.getPackageInfo(s, 0);
            return p.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void restart() {
        Intent i = Global.getContext().getPackageManager()
                .getLaunchIntentForPackage(Global.getContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        Global.getContext().startActivity(i);
    }

    public static boolean weInUiThread() {
        return (Looper.myLooper() == Looper.getMainLooper());
    }
}