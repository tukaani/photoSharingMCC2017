package com.appspot.mccfall2017g12.photoorganizer;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadTools {

    public static final Executor EXECUTOR = Executors.newCachedThreadPool();

    public static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public static void runInMainThread(Runnable r) {
        MAIN_HANDLER.post(r);
    }

    public static void runInWorkerThread(Runnable r) {
        EXECUTOR.execute(r);
    }

}
