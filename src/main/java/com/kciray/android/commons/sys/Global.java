package com.kciray.android.commons.sys;

import android.app.Activity;
import android.content.Context;

public class Global {
    private static Activity context;

    public static Activity getContext() {
        return context;
    }

    public static void setContext(Activity context) {
        Global.context = context;
    }
}
