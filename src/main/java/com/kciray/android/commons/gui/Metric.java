package com.kciray.android.commons.gui;

import com.kciray.android.commons.sys.Global;


public class Metric {
    public static int dpToPx(float dp) {
        float density = Global.getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
