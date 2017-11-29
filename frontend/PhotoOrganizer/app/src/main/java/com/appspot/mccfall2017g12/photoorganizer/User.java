package com.appspot.mccfall2017g12.photoorganizer;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Observable;

/**
 * Created by Edgar on 27/11/2017.
 */

public class User extends Observable {

    private static User user;

    private final String userId;
    private String userName;
    private String groupId;
    private final FirebaseDatabase firebaseDatabase;
    private final Object lock = new Object();

    private User(String userId) {
        this.userId = userId;
        this.firebaseDatabase = FirebaseDatabase.getInstance();

        init();
    }

    private void init() {
        firebaseDatabase.getReference("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                synchronized (lock) {
                    userName = dataSnapshot.child("username").getValue(String.class);
                    groupId = dataSnapshot.child("group").getValue(String.class);
                }

                notifyObservers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean canTakePhoto() {
        synchronized (lock) {
            return userName != null;
        }
    }

    public String getUserName() {
        synchronized (lock) {
            return userName;
        }
    }

    public String getGroupId() {
        synchronized (lock) {
            return groupId;
        }
    }

    public static synchronized User get() {
        return user;
    }

    public static synchronized void set(@NonNull String userId) {
        if (user != null) {
            throw new IllegalStateException();
        }

        user = new User(userId);
    }

    public static synchronized void end() {
        user = null;
    }
}
