package com.kciray.commons;

import com.kciray.commons.core.Consumer;

import java.util.concurrent.CountDownLatch;

public class ThreadUtils {
    public static Thread run(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }

    public static void runAndWait(Consumer<CountDownLatch> consumer) {
        CountDownLatch lock = new CountDownLatch(1);
        try {
            run(() -> {
                consumer.accept(lock);
            });
            lock.await();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    public static void runAndWait(Runnable runnable) {
        CountDownLatch lock = new CountDownLatch(1);
        try {
            Thread thread = new Thread(() -> {
                runnable.run();
            });
            thread.start();
            thread.join();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
