package com.kciray.android.commons.sys;

import android.text.TextUtils;

import java.util.Calendar;

public final class KLog {
    private static final String TAG = "KLog";

    public static void v(String msg) {
        String messageAndLocation = getLocation() + msg;


        android.util.Log.v(TAG, messageAndLocation);
    }

    private static String getLocation() {
        final String className = KLog.class.getName();
        final StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        boolean found = false;

        for (StackTraceElement trace : traces) {
            try {
                if (found) {
                    if (!trace.getClassName().startsWith(className)) {
                        Class<?> clazz = Class.forName(trace.getClassName());
                        return " [" + getClassName(clazz) + ":" + trace.getMethodName() + ":" + trace.getLineNumber() + "]: ";
                    }
                } else if (trace.getClassName().startsWith(className)) {
                    found = true;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return "[]: ";
    }

    private static String getClassName(Class<?> clazz) {
        if (clazz != null) {
            if (!TextUtils.isEmpty(clazz.getSimpleName())) {
                return clazz.getSimpleName();
            }

            return getClassName(clazz.getEnclosingClass());
        }

        return "";
    }

    private static String getDateTimeFormatted() {
        return String.format("%td.%<tm.%<ty  %<tl:%<tM:%<tS.%<tL", Calendar.getInstance());
    }

}