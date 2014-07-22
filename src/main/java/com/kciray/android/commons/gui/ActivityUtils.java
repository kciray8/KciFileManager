package com.kciray.android.commons.gui;

import android.content.Context;
import android.content.Intent;

import com.kciray.android.commons.sys.Global;

public class ActivityUtils {
    public static void start(Class<?> cls) {
        Intent intent = new Intent(Global.getContext(), cls);
        Global.getContext().startActivity(intent);
    }
}
