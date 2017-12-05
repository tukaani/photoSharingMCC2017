package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import java.util.Observable;
import java.util.Observer;

/**
 * Activity that observes the state of the user.
 * When the state changes (e.g. the user no longer belongs to a group),
 * calls shouldGoOn() method. If that returns false, goes back to the main activity.
 */
public class UserSensitiveActivity extends AppCompatActivity {

    private UserStateObserver observer = new UserStateObserver();

    protected void returnToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        User.get().addObserver(observer);

        checkState();
    }

    @Override
    protected void onStop() {
        User.get().deleteObserver(observer);

        super.onStop();
    }

    protected boolean shouldGoOn() {
        return true;
    }

    private void checkState() {
        if (!shouldGoOn()) {
            returnToMainActivity();
        }
    }

    private class UserStateObserver implements Observer {

        @Override
        public void update(Observable observable, Object o) {
            checkState();
        }
    }

}
