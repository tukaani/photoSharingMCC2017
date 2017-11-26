package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_PHOTO = 1;

    private File photoFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



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

        if (!PhotoEventListener.isListening) {
            FirebaseDatabase.getInstance().getReference("photos").child("a1").addChildEventListener(
                    new PhotoEventListener("a1", this));
            PhotoEventListener.isListening = true;
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

            Boolean isBarcode = false;

            Photo photo = new Photo();
            photo.author = "Me"; //TODO real user
            photo.photoId = UUID.randomUUID().toString(); //TODO from firebase (unless private)
            photo.file = this.photoFile.getName();
            photo.albumId = Album.PRIVATE_ALBUM_ID;
            photo.resolution.local = ResolutionTools.calculateResolution(
                    this.photoFile.getAbsolutePath());

            GalleryDatabase.initialize(this);
            new GalleryDatabase.InsertPhotoTask().execute(photo);
        }
    }

}
