package com.appspot.mccfall2017g12.photoorganizer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Handler;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;

public abstract class AsyncObserver<T> implements Observer<T> {
    private final Executor executor;
    @Nullable
    private final Handler mainHandler;

    public AsyncObserver(Executor executor) {
        this(executor, null);
    }

    public AsyncObserver(Executor executor, @Nullable Handler mainHandler) {
        this.executor = executor;
        this.mainHandler = mainHandler;
    }

    @Override
    public void onChanged(@Nullable final T t) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                onChangedAsync(t);
            }
        });
    }

    protected abstract void onChangedAsync(@Nullable T t);

    protected void removeFrom(final LiveData<T> observable) {
        if (mainHandler == null)
            throw new IllegalStateException();

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                observable.removeObserver(AsyncObserver.this);
            }
        });
    }
}
