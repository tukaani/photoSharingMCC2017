package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_PHOTO = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;




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
                    dispatchTakePhoto();
                    Toast.makeText(context, "Take a selfie?", Toast.LENGTH_SHORT).show();
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
    }




    private void dispatchTakePhoto() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) takePhoto();
                else {
                    if(shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                        Toast.makeText(this, "app needs to be able to take Pictures", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_WRITE_EXTERNAL_STORAGE);

                }

            }
            else {
                if(shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "app needs to be able to take Pictures", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }

        }
        else takePhoto();
    }

    void takePhoto(){
        Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePhoto.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePhoto, REQUEST_PHOTO);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");


            Boolean isBarcode = false;

            PhotoSaver p= new PhotoSaver(getApplicationContext());

                    p.save(imageBitmap, "test");

            // saveToInternalStorage(imageBitmap);
        }
    }

}
