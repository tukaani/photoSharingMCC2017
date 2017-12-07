package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.UUID;

public class MainActivity extends UserSensitiveActivity {
    static final int REQUEST_PHOTO = 1;

    private volatile String photoFilename = null;
    private final FirebaseAuth mAuth;
    private final FirebaseDatabase mFirebaseDatabase;
    private final LocalDatabase mDatabase;

    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkChangeReceiver receiver = new NetworkChangeReceiver();

    public MainActivity() {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = LocalDatabase.getInstance(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Register BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        getApplicationContext().registerReceiver(receiver, filter);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        final MenuItem[] menuItems = new MenuItem[] {
            new MenuItem(R.string.gallery, R.drawable.ic_collections_black_24dp) {
                @Override
                public void launch(Context context) {
                    Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
                    MainActivity.this.startActivity(intent);
                }
            },
            new MenuItem(R.string.takePhoto, R.drawable.ic_add_a_photo_black_24dp) {
                @Override
                public void launch(Context context) {
                    if (!getUser().canTakePhoto()) {
                        showInitializingToast();
                        return;
                    }
                    takePhoto();
                }
            },
            new MenuItem(R.string.groups, R.drawable.ic_group_black_24dp) {
                @Override
                public void launch(Context context) {
                    if (!getUser().canManageGroups()) {
                        showInitializingToast();
                        return;
                    }
                    Class klass;
                    if (getUser().isInGroup())
                        klass = GroupActivity.class;
                    else
                        klass = GroupManagementActivity.class;
                    Intent intent = new Intent(MainActivity.this, klass);
                    MainActivity.this.startActivity(intent);
                }
            },
            new MenuItem(R.string.settings, R.drawable.ic_settings_black_24dp) {
                @Override
                public void launch(Context context) {
                    //TODO
                    //Toast.makeText(context, "Tryin' to fix smth?", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                    MainActivity.this.startActivity(intent);
                }
            }
        };

        GridView gridView = findViewById(R.id.gridView);
        gridView.setAdapter(new MenuItemAdapter(this, menuItems));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                MenuItem menuItem = menuItems[position];
                menuItem.launch(MainActivity.this);
            }
        });
    }

    @Override
    protected boolean shouldGoOn() {
        return true;
    }

    private void showInitializingToast() {
        Toast.makeText(this, R.string.initializing, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("photoFilename", this.photoFilename);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState == null)
            return;

        this.photoFilename = savedInstanceState.getString("photoFilename", null);
    }

    void takePhoto(){
        String filename = UUID.randomUUID().toString() + ".jpg";

        this.photoFilename = filename;

        File photoFile = FileTools.get(filename);
        Uri uri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".provider", photoFile);

        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        if(takePhoto.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePhoto, REQUEST_PHOTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK) {
            final File photoFile = FileTools.get(this.photoFilename);
            final String uid = getUser().getUserId();

            ThreadTools.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {

                    String groupId = getUser().getGroupId();

                    boolean keepOffline = hasBarcode(photoFile) || groupId == null;

                    Photo photo = new Photo();
                    photo.author = getUser().getUserName();

                    if (keepOffline) {
                        photo.albumId = Album.PRIVATE_ALBUM_ID;
                        photo.photoId = UUID.randomUUID().toString();
                    }
                    else {
                        photo.albumId = groupId;
                        DatabaseReference photoRef = mFirebaseDatabase
                                .getReference("photos").child(groupId).push();
                        photoRef.child("author").setValue(uid);
                        photo.photoId = photoRef.getKey();
                    }

                    mDatabase.galleryDao().insertPhotos(photo);
                    mDatabase.galleryDao().tryUpdatePhotoFile(photo.photoId, photoFile.getName(),
                            ResolutionTools.calculateResolution(photoFile.getAbsolutePath()));

                    if (!keepOffline) {
                        getUser().getSynchronizer().uploadPhoto(photo.photoId);
                    }
                }
            });
        }
    }

    boolean hasBarcode(File photoFile){
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).build();
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);
        if(barcodes.size()> 0 ) return true;
        else return false;


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        User.end();
        mAuth.signOut();
        finish();
    }
}


