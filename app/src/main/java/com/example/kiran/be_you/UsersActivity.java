package com.example.kiran.be_you;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
    @BindView(R.id.search_input) EditText mSearchInput;

    private String mCurrentUid;
    private List<User> mUserList;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.users_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.users_action_search:
                showSearch();
                break;
            default:
                return false;
        }
        return true;
    }

    private void showSearch() {
        mSearchInput.setVisibility(View.VISIBLE);
        if(mSearchInput.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mSearchInput, InputMethodManager.SHOW_IMPLICIT);
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.BELOW, R.id.search_input);
        mUsersRecyclerView.setLayoutParams(params);
    }

    @Override
    public void onBackPressed() {
        // TODO: close keyboard and search with the same press of the back button
        if (mSearchInput.getVisibility() == View.VISIBLE) {
            mSearchInput.setVisibility(View.INVISIBLE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            mUsersRecyclerView.setLayoutParams(params);
        } else {
            finish();
        }
    }

    private void setRecyclerView() {
        mUsersRecyclerView.setHasFixedSize(false);
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new UserAdapter();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> list = dataSnapshot.getChildren();

                // Filter current user
                mUserList = new ArrayList<>();
                for (DataSnapshot user : list) {
                    if (!user.getKey().equals(mCurrentUid)) {
                        mUserList.add(user.getValue(User.class));
                    }
                }
                // Setting data
                mAdapter.setItems(mUserList);
            }

            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        mUsersRecyclerView.setAdapter(mAdapter);

        filterUsers();
    }

    private void filterUsers() {
        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }

    private void filter(String text) {
        List<User> filteredList = new ArrayList<>();

        for (User user : mUserList) {
            if (user.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(user);
            }
        }

        if (text.equals("")) {
            mAdapter.setItems(mUserList);
        } else {
            mAdapter.setItems(filteredList);
        }
    }

    private class UserAdapter extends RecyclerView.Adapter<UserViewHolder> {
        private List<User> userList = new ArrayList<>();

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new UserViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_item_layout, parent, false));
        }

        public List<User> getItems() {
            return userList;
        }

        public void setItems(List<User> userList) {
            this.userList = userList;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = userList.get(position);
            holder.bind(user);
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

            CircleImageView imageView = mView.findViewById(R.id.item_image);
            Picasso.with(getApplicationContext()).load(user.getThumb_image()).placeholder(R.mipmap.icon).into(imageView);

            final String uid = user.getUid();
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(UsersActivity.this, ProfileActivity.class);
                    intent.putExtra("userUid", uid);
                    startActivity(intent);
                }
            });
        }
    }
}
