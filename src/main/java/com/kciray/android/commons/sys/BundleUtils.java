package com.kciray.android.commons.sys;

import android.content.Intent;
import android.os.Bundle;

import static com.kciray.android.commons.sys.KLog.v;

public class BundleUtils {
    public static void outExtras(Bundle bundle) {
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                v(String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
            }
        }else{
            v("NULL BUNDLE - " + bundle);
        }
    }
}
