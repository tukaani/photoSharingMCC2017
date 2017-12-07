package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Observable;

/**
 * Activity that observes the state of the user.
 * When the state changes (e.g. the user no longer belongs to a group),
 * calls shouldGoOn() method. If that returns false, goes back to the main activity.
 */
public abstract class UserSensitiveActivity extends AppCompatActivity {

    private UserStateObserver observer = new UserStateObserver();
    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getApplicationContext();

        user = User.get();

        if (user == null) {

            String userId = FirebaseAuth.getInstance().getUid();

            if (userId == null) {
                user = new User("DUMMY", context);
                returnToLoginActivity();
            }
            else {
                user = User.set(userId, context);
            }

        }
    }

    private void returnToActivity(Class activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @MainThread
    protected void returnToLoginActivity() {
        returnToActivity(LoginActivity.class);
    }

    @MainThread
    protected void returnToMainActivity() {
        returnToActivity(MainActivity.class);
    }

    @Override
    protected void onStart() {
        super.onStart();

        getUser().addObserver(observer);

        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        });

        onUserStateChanged();
    }

    @Override
    protected void onStop() {
        getUser().deleteObserver(observer);

        super.onStop();
    }

    protected User getUser() {
        return user;
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
            if (getUser().isEnded()) {
                returnToLoginActivity();
                return;
            }
            onUserStateChanged();
        }
    }

}
