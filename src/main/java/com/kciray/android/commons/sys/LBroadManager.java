package com.kciray.android.commons.sys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public class LBroadManager {
    public static void registerReceiver(BroadcastReceiver receiver, String event){
        LocalBroadcastManager.getInstance(Global.getContext()).
                registerReceiver(receiver, new IntentFilter(event));
    }

    public static void send(Intent event){
        LocalBroadcastManager.getInstance(Global.getContext()).sendBroadcast(event);
    }
}
