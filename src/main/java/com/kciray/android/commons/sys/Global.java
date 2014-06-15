package com.kciray.android.commons.sys;

import android.content.Context;

public class Global {
    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        Global.context = context;
    }
}
