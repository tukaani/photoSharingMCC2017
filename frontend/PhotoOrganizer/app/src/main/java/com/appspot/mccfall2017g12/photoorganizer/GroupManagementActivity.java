package com.appspot.mccfall2017g12.photoorganizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Ilkka on 27.11.2017.
 */

public class GroupManagementActivity extends Activity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView zXingScannerView;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_management);

        final MenuItem[] menuItems = new MenuItem[]{
                new MenuItem(R.string.creategroup, R.drawable.ic_group_add_black_24dp) {
                    @Override
                    public void launch(Context context) {
                        //Toast.makeText(context, "Create new", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(GroupManagementActivity.this, CreateGroupActivity.class);
                        GroupManagementActivity.this.startActivity(intent);
                    }
                },
                new MenuItem(R.string.joingroup, R.drawable.ic_group_black_24dp) {
                    @Override
                    public void launch(Context context) {
                        Toast.makeText(context, "Join your friends", Toast.LENGTH_SHORT).show();
                    }
                },
                new MenuItem(R.string.leavegroup, R.drawable.ic_person_black_24dp) {
                    @Override
                    public void launch(Context context) {
                        read();
                        Toast.makeText(context, "Going solo", Toast.LENGTH_SHORT).show();
                    }
                }

        };
        ListView listView = findViewById(R.id.groups_listView);
        listView.setAdapter(new MenuItemAdapter(this, menuItems));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                MenuItem menuItem = menuItems[position];
                menuItem.launch(GroupManagementActivity.this);
            }
        });
    }


    void read(){
        zXingScannerView = new ZXingScannerView(getApplicationContext());
        setContentView(zXingScannerView);
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        zXingScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        zXingScannerView.resumeCameraPreview(this);
    }
}
