package com.example.kiran.be_you;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kiran.be_you.model.User;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private static final String TAG = "UsersActivity";

    @BindView(R.id.users_recyclerview) RecyclerView mUsersRecyclerView;

    private String mCurrentUid;
    UserAdapter mAdapter;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        ButterKnife.bind(this);

        mUsersRecyclerView.setHasFixedSize(true);
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCurrentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase.keepSynced(true);  // offline capabilities
        mDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/online").setValue("true");

        setRecyclerView();
    }

    private void setRecyclerView() {
        mUsersRecyclerView.setHasFixedSize(true);
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new UserAdapter();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> list = dataSnapshot.getChildren();

                // Filter current user
                List<User> userList = new ArrayList<>();
                for (DataSnapshot user : list) {
                    if (!user.getKey().equals(mCurrentUid)) {
                        userList.add(user.getValue(User.class));
                    }
                }
                // Setting data
                mAdapter.setItems(userList);
                mAdapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        mUsersRecyclerView.setAdapter(mAdapter);
    }

    private class UserAdapter extends RecyclerView.Adapter<UserViewHolder> {

        private List<User> userList = new ArrayList<>();

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new UserViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_item_layout, parent, false));
        }

        public void setItems(List<User> userList) {
            this.userList = userList;
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = userList.get(position);
            holder.bind(user);

            // TODO: move this to holder.bind() method
            holder.setThumb_image(user.getThumb_image(),getApplicationContext());
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }
    }

    private class UserViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UserViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void bind(User user) {
            TextView nameView = mView.findViewById(R.id.item_name);
            nameView.setText(user.getName());

            TextView statusView = mView.findViewById(R.id.item_status);
            statusView.setText(user.getStatus());


            final String uid = user.getUid();
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(UsersActivity.this, ProfileActivity.class);
                    intent.putExtra("user_id", uid);
                    startActivity(intent);
                }
            });
        }

        public void setThumb_image(String thumb_image, Context ctx) {
            CircleImageView imageView = mView.findViewById(R.id.item_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.mipmap.icon).into(imageView);
        }
    }
}
