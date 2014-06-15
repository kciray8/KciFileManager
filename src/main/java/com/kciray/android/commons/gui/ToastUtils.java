package com.kciray.android.commons.gui;

import android.widget.Toast;

import com.kciray.android.commons.sys.Global;

public class ToastUtils {
    public static void show(String text){
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(Global.getContext(), text, duration);
        toast.show();
    }
}
