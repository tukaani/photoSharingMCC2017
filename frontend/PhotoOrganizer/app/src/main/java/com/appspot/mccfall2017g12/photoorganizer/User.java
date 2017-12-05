package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Observable;
import java.util.concurrent.Executor;

/**
 * Created by Edgar on 27/11/2017.
 */

public class User {

    private static final String TAG = "user";

    private static final int STATE_USERNAME_OK = 0x1;
    private static final int STATE_PRIVATE_ALBUM_OK = 0x2;
    private static final int STATE_IN_GROUP = 0x4;
    private static final int STATE_GROUP_ALBUM_OK = 0x8;

    private static User user;

    private final String userId;
    private String userName;
    private String groupId;
    private String Idtoken;
    private String groupName;
    private PhotoSynchronizer synchronizer;
    private String expirationDate;
    private int state = 0;
    private final LocalDatabase database;
    private final FirebaseDatabase firebaseDatabase;
    private final Object lock = new Object();
    private final Executor executor;
    private final PhotoSynchronizer.Factory synchronizerFactory;

    private User(String userId, final Context context) {
        this.userId = userId;
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.database = LocalDatabase.getInstance(context);
        this.executor = ThreadTools.EXECUTOR;
        this.synchronizerFactory = new PhotoSynchronizer.Factory() {
            @Override
            public PhotoSynchronizer create(String groupId) {
                return new PhotoSynchronizer(groupId, context);
            }
        };

        init();
    }

    private void init() {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                initPrivateAlbum();
            }
        });

        DatabaseReference userRef = firebaseDatabase.getReference("users").child(userId);

        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
            @Override
            public void onSuccess(GetTokenResult getTokenResult) {
              Idtoken = getTokenResult.getToken();
            }
        });

        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setUserName(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Cannot read username");
            }
        });

        userRef.child("group").addValueEventListener(new AsyncValueEventListener(executor) {

            @Override
            @WorkerThread
            public void onDataChangeAsync(DataSnapshot dataSnapshot) {
                setGroupId(dataSnapshot.getValue(String.class));
            }

            @Override
            @WorkerThread
            public void onCancelledAsync(DatabaseError databaseError) {
                setGroupId(null);
            }
        });
    }

    @WorkerThread
    private void initPrivateAlbum() {
        Album album = new Album();
        album.albumId = Album.PRIVATE_ALBUM_ID;
        album.name = "Private";

        database.galleryDao().insertAlbums(album);

        setState(STATE_PRIVATE_ALBUM_OK, true);
    }

    private void initGroupAlbum(final String groupId) {
        firebaseDatabase.getReference("groups").child(groupId)
                .addListenerForSingleValueEvent(
                new AsyncValueEventListener(executor) {
                    @Override
                    public void onDataChangeAsync(DataSnapshot dataSnapshot) {
                        Album album = new Album();

                        album.albumId = groupId;
                        album.name = dataSnapshot.child("name").getValue(String.class);
                        expirationDate = dataSnapshot.child("end_time").getValue(String.class);
                        groupName = album.name;
                        database.galleryDao().insertAlbums(album);

                        setState(STATE_GROUP_ALBUM_OK, true);
                    }

                    @Override
                    public void onCancelledAsync(DatabaseError databaseError) {
                        Log.d(TAG, "Cannot read group name");
                    }
                }
        );
    }

    private void setUserName(String newUserName) {
        if (newUserName == null) {
            Log.d(TAG, "Username null");
            return;
        }

        synchronized (lock) {
            userName = newUserName;
            setState(STATE_USERNAME_OK, true);
        }
    }

    @WorkerThread
    private void setGroupId(@Nullable String newGroupId) {
        PhotoSynchronizer newSynchronizer;

        if (newGroupId == null) {
            newSynchronizer = null;
        }
        else {
            newSynchronizer = synchronizerFactory.create(newGroupId);
            initGroupAlbum(newGroupId);
        }

        synchronized (lock) {
            if (!TextUtils.equals(groupId, newGroupId)) {
                groupId = newGroupId;

                if (synchronizer != null)
                    synchronizer.stop();

                synchronizer = newSynchronizer;

                if (synchronizer != null)
                    synchronizer.listen();

                setState(STATE_IN_GROUP, groupId != null);
            }
        }
    }

    public PhotoSynchronizer getSynchronizer() {
        synchronized (lock) {
            return synchronizer;
        }
    }

    public boolean canTakePhoto() {
        synchronized (lock) {
            return getState(STATE_USERNAME_OK) && getState(STATE_PRIVATE_ALBUM_OK);
        }
    }

    private boolean getState(int flag) {
        synchronized (lock) {
            return (state & flag) > 0;
        }
    }

    private void setState(int flag, boolean value) {
        synchronized (lock) {
            if (value)
                state |= flag;
            else
                state &= ~flag;
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

    public static synchronized void set(@NonNull String userId, Context context) {
        if (user != null) {
            throw new IllegalStateException();
        }

        user = new User(userId, context);
    }

    public static synchronized void end() {
        if (user != null)
            user.setGroupId(null);

        user = null;
    }

    public String getIdtoken() {
        return Idtoken;
    }

    public String getGroupName() { return groupName; }

    public String getExpirationDate() { return expirationDate; }
}
