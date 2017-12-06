package com.appspot.mccfall2017g12.photoorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends UserSensitiveActivity {

    private MemberAdapter memberAdapter;
    private RecyclerView recyclerView;
    private final DatabaseReference membersReference;
    private final DatabaseReference usersReference;
    private final FirebaseDatabase firebaseDatabase;

    public GroupActivity()
    {
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference = firebaseDatabase.getReference("users");
        membersReference = firebaseDatabase.getReference("groups")
                .child(User.get().getGroupId())
                .child("members");
    }

    private FirebaseDatabase firebaseDatabase;
    private TextView mGroup;
    private TextView mExpiration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
<<<<<<< HEAD
        firebaseDatabase = FirebaseDatabase.getInstance();
        mGroup = (TextView) findViewById(R.id.textView3);
        mGroup.setText(User.get().getGroupName());
        mExpiration = (TextView) findViewById(R.id.textView5);
        mExpiration.setText(User.get().getExpirationDate());
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
=======

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GroupActivity.this, QRActivity.class);
                startActivity(intent);
            }
        });
>>>>>>> a0a59a6a009ee09d00df636f914cfd5094ee1d00

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
        return User.get().isInGroup();
    }

    private class Member implements Diffable<Member> {
        public String username;
        public String userId;

        @Override
        public boolean isTheSameAs(Member other) {
            return TextUtils.equals(this.userId, other.userId);
        }

        @Override
        public boolean hasTheSameContentAs(Member other) {
            return TextUtils.equals(this.username, other.username);
        }
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
