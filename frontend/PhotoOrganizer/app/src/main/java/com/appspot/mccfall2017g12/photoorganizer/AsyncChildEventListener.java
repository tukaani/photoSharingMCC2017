package com.appspot.mccfall2017g12.photoorganizer;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.concurrent.Executor;

/**
 * Firebase event callbacks are invoked in the main thread. This class provides subclasses
 * with an easy interface to run callbacks (denoted with *Async) in a worker thread using
 * {@link Executor} given as parameter to a constructor.
 *
 * Conceptually similar to:
 * https://github.com/CodingDoug/white-label-event-app/commit/917ff279febce1977635226fe9181cc1ff099656
 */
public abstract class AsyncChildEventListener implements ChildEventListener {

    protected final Executor executor;

    public AsyncChildEventListener(Executor executor) {

        this.executor = executor;
    }

    @Override
    public void onChildAdded(final DataSnapshot dataSnapshot, final String previousChildName) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                onChildAddedAsync(dataSnapshot, previousChildName);
            }
        });
    }

    @Override
    public void onChildChanged(final DataSnapshot dataSnapshot, final String previousChildName) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                onChildChangedAsync(dataSnapshot, previousChildName);
            }
        });
    }

    @Override
    public void onChildRemoved(final DataSnapshot dataSnapshot) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                onChildRemovedAsync(dataSnapshot);
            }
        });
    }

    @Override
    public void onChildMoved(final DataSnapshot dataSnapshot, final String previousChildName) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                onChildMovedAsync(dataSnapshot, previousChildName);
            }
        });
    }

    @Override
    public void onCancelled(final DatabaseError databaseError) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                onCancelledAsync(databaseError);
            }
        });
    }

    public abstract void onChildAddedAsync(DataSnapshot dataSnapshot, String previousChildName);

    public abstract void onChildChangedAsync(DataSnapshot dataSnapshot, String previousChildName);

    public abstract void onChildRemovedAsync(DataSnapshot dataSnapshot);

    public abstract void onChildMovedAsync(DataSnapshot dataSnapshot, String previousChildName);

    public abstract void onCancelledAsync(DatabaseError databaseError);
}
