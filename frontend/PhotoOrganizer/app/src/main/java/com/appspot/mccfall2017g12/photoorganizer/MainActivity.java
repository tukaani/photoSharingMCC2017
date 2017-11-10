package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MenuItem[] menuItems = new MenuItem[] {
            new MenuItem(R.string.gallery, R.drawable.ic_collections_black_24dp) {
                @Override
                public void launch(Context context) {
                    //TODO
                    Toast.makeText(context, "Wanna see photos?", Toast.LENGTH_SHORT).show();
                }
            },
            new MenuItem(R.string.takePhoto, R.drawable.ic_add_a_photo_black_24dp) {
                @Override
                public void launch(Context context) {
                    //TODO
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
                    Toast.makeText(context, "Tryin' to fix smth?", Toast.LENGTH_SHORT).show();
                }
            }
        };

        GridView gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(new MenuItemAdapter(this, menuItems));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                MenuItem menuItem = menuItems[position];
                menuItem.launch(MainActivity.this);
            }
        });
    }
}
