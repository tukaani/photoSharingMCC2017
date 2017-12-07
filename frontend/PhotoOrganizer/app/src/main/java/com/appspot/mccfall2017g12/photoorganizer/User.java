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

public class User extends Observable {

    private static final String TAG = "user";

    private static final int STATE_USERNAME_OK = 0x1;
    private static final int STATE_IN_GROUP = 0x2;
    private static final int STATE_GROUP_OK = 0x4;
    private static final int STATE_ID_TOKEN_OK = 0x8;

    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_CREATOR = 2;

    private static User user;

    private final String userId;
    private final DatabaseReference userRef;
    private String userName;
    private String groupId;
    private String Idtoken;
    private String groupName;
    private String expirationDate;
    private String creatorId;

    private int state = 0x00;

    private final Object lock = new Object();

    private final LocalDatabase database;
    private final FirebaseDatabase firebaseDatabase;
    private final Executor executor;
    private final PhotoSynchronizer.Factory synchronizerFactory;
    private final ValueEventListener groupListener;

    private PhotoSynchronizer synchronizer;

    private User(String userId, final Context context) {
        this.userId = userId;
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.database = LocalDatabase.getInstance(context);
        this.executor = ThreadTools.EXECUTOR;
        this.userRef = firebaseDatabase.getReference("users").child(userId);

        this.synchronizerFactory = new PhotoSynchronizer.Factory() {
            @Override
            public PhotoSynchronizer create(String groupId) {
                return new PhotoSynchronizer(groupId, context);
            }
        };

        this.groupListener = new AsyncValueEventListener(executor) {

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
        };

        init();
    }

    private void init() {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                initAlbum(Album.PRIVATE_ALBUM_ID, "Private");
            }
        });

        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true)
                .addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
            @Override
            public void onSuccess(GetTokenResult getTokenResult) {
                synchronized (lock) {
                    Idtoken = getTokenResult.getToken();
                    setState(STATE_ID_TOKEN_OK, true);
                }
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

        userRef.child("group").addValueEventListener(groupListener);
    }

    private void stop() {
        userRef.child("group").removeEventListener(groupListener);
        user.setGroupId(null);
    }

    private void initAlbum(String albumId, String name) {
        Album album = new Album();
        album.albumId = albumId;
        album.name = name;

        database.galleryDao().insertAlbums(album);
    }

    private void initGroup(final String groupId) {
        firebaseDatabase.getReference("groups").child(groupId)
                .addListenerForSingleValueEvent(
                new AsyncValueEventListener(executor) {
                    @Override
                    public void onDataChangeAsync(DataSnapshot dataSnapshot) {

                        synchronized (lock) {
                            groupName = dataSnapshot.child("name").getValue(String.class);
                            expirationDate = dataSnapshot.child("end_time").getValue(String.class);
                            creatorId = dataSnapshot.child("creator").getValue(String.class);
                            setState(STATE_GROUP_OK, true);
                        }

                        initAlbum(groupId, groupName);
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
            initGroup(newGroupId);
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
        return getState(STATE_USERNAME_OK);
    }

    public boolean canManageGroups() {
        return getState(STATE_ID_TOKEN_OK);
    }

    public boolean isInGroup() {
        synchronized (lock) {
            return getState(STATE_IN_GROUP);
        }
    }

    public int getUserStatus() {
        synchronized (lock) {
            if (creatorId == null)
                return STATUS_UNKNOWN;
            else if (TextUtils.equals(creatorId, userId))
                return STATUS_CREATOR;
            else
                return STATUS_NORMAL;
        }
    }

    private boolean getState(int flag) {
        synchronized (lock) {
            return (state & flag) > 0;
        }
    }

    private void setState(int flag, boolean value) {
        boolean changed;
        synchronized (lock) {
            int oldState = state;
            if (value)
                state |= flag;
            else
                state &= ~flag;
            changed = (state != oldState);
        }
        if (changed) {
            setChanged();
            notifyObservers();
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
            user.stop();
        }

        user = new User(userId, context);
    }

    public static synchronized void end() {
        if (user != null)
            user.stop();

        user = null;
    }

    public String getUserId() {
        return userId;
    }

    public String getIdtoken() {
        synchronized (lock) {
            return Idtoken;
        }
    }

    public String getGroupName() {
        synchronized (lock) {
            return groupName;
        }
    }

    public String getExpirationDate() {
        synchronized (lock) {
            return expirationDate;
        }
    }
}
