package com.kciray.android.commons.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.kciray.android.commons.sys.Global;

public class ViewUtils {
    public static View viewFromRes(int resId) {
        LayoutInflater inflater = (LayoutInflater)
                Global.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(resId, null);
    }
}
