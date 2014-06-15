package com.kciray.android.commons.gui;

import android.content.Context;

import com.kciray.android.commons.sys.Global;


public class Metric {
    public static int dpToPx(int dp) {
        float density = Global.getContext().getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
