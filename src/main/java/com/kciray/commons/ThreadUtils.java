package com.kciray.commons;

public class ThreadUtils {
    public static Thread run(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
