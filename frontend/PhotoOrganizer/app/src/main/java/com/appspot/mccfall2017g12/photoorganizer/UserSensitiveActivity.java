package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;

import java.util.Observable;

/**
 * Activity that observes the state of the user.
 * When the state changes (e.g. the user no longer belongs to a group),
 * calls shouldGoOn() method. If that returns false, goes back to the main activity.
 */
public abstract class UserSensitiveActivity extends AppCompatActivity {

    private UserStateObserver observer = new UserStateObserver();

    @MainThread
    protected void returnToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        User.get().addObserver(observer);

        onUserStateChanged();
    }

    @Override
    protected void onStop() {
        User.get().deleteObserver(observer);

        super.onStop();
    }

    protected abstract boolean shouldGoOn();

    @MainThread
    protected void onUserStateChanged() {
        if (!shouldGoOn()) {
            returnToMainActivity();
        }
    }

    private class UserStateObserver extends ObserverInMainThread {

        @Override
        @MainThread
        public void updateInternal(Observable observable, Object o) {
            onUserStateChanged();
        }
    }

}
