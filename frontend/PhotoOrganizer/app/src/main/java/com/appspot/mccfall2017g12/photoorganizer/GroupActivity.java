package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.appspot.mccfall2017g12.photoorganizer.http.DeleteGroupRequest;
import com.appspot.mccfall2017g12.photoorganizer.http.Endpoints;
import com.appspot.mccfall2017g12.photoorganizer.http.SimpleGroupClient;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends UserSensitiveActivity {

    private MemberAdapter memberAdapter;
    private RecyclerView recyclerView;
    private DatabaseReference membersReference;
    private DatabaseReference usersReference;
    private FirebaseDatabase firebaseDatabase;
    private TextView groupTextView;
    private TextView expirationTextView;
    private Button addButton;
    private Button leaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference = firebaseDatabase.getReference("users");
        membersReference = firebaseDatabase.getReference("groups")
                .child(getUser().getGroupId())
                .child("members");

        groupTextView = findViewById(R.id.textView3);
        expirationTextView = findViewById(R.id.textView5);
        addButton = findViewById(R.id.addButton);
        leaveButton = findViewById(R.id.leaveButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupActivity.this, QRActivity.class);
                startActivity(intent);
            }
        });

        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteGroupRequest request = new DeleteGroupRequest();
                request.setUser_id(getUser().getUserId());
                request.setGroup_id(getUser().getGroupId());
                SimpleGroupClient.post(Endpoints.DELETE, request, getUser().getIdtoken(),
                        new SimpleGroupClient.Callback() {
                            @Override
                            public void onSuccess() {
                                returnToMainActivity();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                e.printStackTrace();
                            }
                        });
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        updateUI();
    }

    @MainThread
    private void updateUI() {
        groupTextView.setText(getUser().getGroupName());
        expirationTextView.setText(getUser().getExpirationDate());
        switch (getUser().getUserStatus()) {
            case User.STATUS_AUTHOR:
                leaveButton.setText("Delete");
                leaveButton.setVisibility(View.VISIBLE);
                break;
            case User.STATUS_NORMAL:
                leaveButton.setText("Leave");
                leaveButton.setVisibility(View.VISIBLE);
                break;
            default:
                leaveButton.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    @MainThread
    protected void onUserStateChanged() {
        super.onUserStateChanged();
        updateUI();
    }

    @Override
    protected void onStart() {
        super.onStart();

        memberAdapter = new MemberAdapter();

        recyclerView.setAdapter(memberAdapter);
        membersReference.addChildEventListener(memberAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        membersReference.removeEventListener(memberAdapter);
        recyclerView.setAdapter(null);

        memberAdapter = null;
    }

    @Override
    protected boolean shouldGoOn() {
        return getUser().isInGroup();
    }

    private class Member {
        public String username;
        public String userId;
    }

    private class MemberAdapter extends RecyclerView.Adapter<MemberViewHolder>
            implements ChildEventListener {

        private List<Member> members = new ArrayList<>();

        @Override
        public void onBindViewHolder(MemberViewHolder holder, int position) {
            Member member = members.get(position);
            holder.textView.setText(member.username);
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        @Override
        public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater
                    .from(GroupActivity.this)
                    .inflate(R.layout.layout_member, parent, false);
            return new MemberViewHolder(view);
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            final String userId = dataSnapshot.getValue(String.class);
            usersReference.child(userId).child("username").addListenerForSingleValueEvent(
                    new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Member member = new Member();
                            member.username = dataSnapshot.getValue(String.class);
                            member.userId = userId;

                            members.add(member);
                            notifyItemInserted(members.size() - 1);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    });
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String userId = dataSnapshot.getValue(String.class);

            int i = 0;
            for (Member member : members) {
                if (TextUtils.equals(member.userId, userId))
                    break;
                i++;
            }

            members.remove(i);
            notifyItemRemoved(i);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

        @Override
        public void onCancelled(DatabaseError databaseError) { }
    }

    private class MemberViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public MemberViewHolder(View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.nameTextView);
        }
    }
}
