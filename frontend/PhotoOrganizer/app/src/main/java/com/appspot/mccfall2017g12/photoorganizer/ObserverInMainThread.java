package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Context;
import android.support.annotation.MainThread;

import java.util.Observable;
import java.util.Observer;

public abstract class ObserverInMainThread implements Observer {

    @Override
    public void update(final Observable observable, final Object arg) {
        ThreadTools.MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                updateInternal(observable, arg);
            }
        });
    }

    @MainThread
    protected abstract void updateInternal(Observable observable, Object arg);
}
