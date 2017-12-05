package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by Ilkka on 27.11.2017.
 */

public class GroupManagementActivity extends UserSensitiveActivity {




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
                        GroupManagementActivity.this.startActivity(new Intent(GroupManagementActivity.this,JoinActivity.class));
                        Toast.makeText(context, "Join your friends", Toast.LENGTH_SHORT).show();
                    }
                },
                new MenuItem(R.string.leavegroup, R.drawable.ic_person_black_24dp) {
                    @Override
                    public void launch(Context context) {
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

    @Override
    protected boolean shouldGoOn() {
        return !User.get().isInGroup();
    }
}
