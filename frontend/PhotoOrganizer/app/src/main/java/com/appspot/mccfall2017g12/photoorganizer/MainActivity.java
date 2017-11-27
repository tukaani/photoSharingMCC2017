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

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_PHOTO = 1;

    private File photoFile = null;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    //TODO !
    private static volatile PhotoSynchronizer synchronizer;

    // The BroadcastReceiver that tracks network connectivity changes.
    private NetworkChangeReceiver receiver = new NetworkChangeReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        this.registerReceiver(receiver, filter);


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

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
                    takePhoto();
                }
            },
            new MenuItem(R.string.groups, R.drawable.ic_group_black_24dp) {
                @Override
                public void launch(Context context) {
                    //TODO

                    Toast.makeText(context, "Don't be alone!", Toast.LENGTH_SHORT).show();
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

        if (!PhotoSynchronizer.isListening) {
            PhotoSynchronizer synchronizer = new PhotoSynchronizer(User.getGroupId(), this);
            synchronizer.listen();
            MainActivity.synchronizer = synchronizer;
            PhotoSynchronizer.isListening = true;
        }
    }

    void takePhoto(){
        String filename = UUID.randomUUID().toString() + ".jpg";

        this.photoFile = FileTools.get(filename);
        Uri uri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".provider", this.photoFile);

        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        if(takePhoto.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePhoto, REQUEST_PHOTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK) {
            final File photoFile = this.photoFile;

            ThreadTools.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {

                    Boolean isBarcode = hasBarcode(photoFile);

                    Photo photo = new Photo();
                    photo.author = User.getUsername();

                    if(isBarcode) {
                        photo.albumId = Album.PRIVATE_ALBUM_ID;
                        photo.photoId = UUID.randomUUID().toString();
                    }
                    else {
                        photo.albumId = User.getGroupId();
                        DatabaseReference photoRef = mDatabase.child("photos").child(User.getGroupId()).push();
                        photoRef.child("author").setValue(mAuth.getCurrentUser().getUid());
                        photo.photoId = photoRef.getKey();
                    }

                    GalleryDatabase.initialize(MainActivity.this);
                    GalleryDatabase.getInstance().galleryDao().insertPhotos(photo);
                    GalleryDatabase.getInstance().galleryDao().tryUpdatePhotoFile(photo.photoId, photoFile.getName(),
                            ResolutionTools.calculateResolution(photoFile.getAbsolutePath()));

                    synchronizer.uploadPhoto(photo.photoId);
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
        mAuth.signOut();
        finish();
    }
}


