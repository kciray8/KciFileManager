package com.kciray.android.commons.sys;

import android.text.TextUtils;

import java.util.Calendar;

public final class KLog {
    private static final String TAG = "KLog";
    private static StringBuilder stringBuilder = new StringBuilder();

    public static void v(Object object) {
        String messageAndLocation = getLocation() + ((object != null) ? object.toString() : "null");

        android.util.Log.v(TAG, messageAndLocation);
        stringBuilder.append(messageAndLocation);
    }

    public static void d(Object object) {
        String messageAndLocation = getLocation() + ((object != null) ? object.toString() : "null");

        android.util.Log.d(TAG, messageAndLocation);
        stringBuilder.append(messageAndLocation);
    }

    public static void mark() {
        v("call...");
    }

    public static String getLog(){
        return stringBuilder.toString();
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
                        return "at " +  clazz.getPackage().getName() + "(" + getClassName(clazz) +
                                ".java:" + trace.getLineNumber() + ")."+ trace.getMethodName()  + "(): ";
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