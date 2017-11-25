package com.appspot.mccfall2017g12.photoorganizer;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

/**
 * Similar to AsyncChildEventListener.
 */
public abstract class AsyncValueEventListener implements ValueEventListener {

    private final Executor executor;

    public AsyncValueEventListener(Executor executor) {

        this.executor = executor;
    }

    @Override
    public void onDataChange(final DataSnapshot dataSnapshot) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                onDataChangeAsync(dataSnapshot);
            }
        });
    }

    public abstract void onDataChangeAsync(DataSnapshot dataSnapshot);
}
