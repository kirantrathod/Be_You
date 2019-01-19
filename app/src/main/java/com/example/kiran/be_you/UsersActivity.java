package com.example.kiran.be_you;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import static android.R.attr.id;
import static android.R.attr.mode;

import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import de.hdodenhof.circleimageview.CircleImageView;



public class UsersActivity extends AppCompatActivity {
    private RecyclerView muserlist;
    private DatabaseReference mDatabase;
    private DatabaseReference muserref;
    private FirebaseAuth auth;
    private LinearLayoutManager mlinearlayout;
    static String gender;

    private FirebaseRecyclerAdapter<Users,UserViewHolder> firebaseRecyclerAdapter;
   // private FirebaseUser mcurrentuser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        muserlist=(RecyclerView) findViewById(R.id.alluserrecyclerview);

        mlinearlayout=new LinearLayoutManager(this);
        muserlist.setHasFixedSize(true);
        muserlist.setLayoutManager(mlinearlayout);
        //mlinearlayout.setReverseLayout(true);
        //mlinearlayout.setStackFromEnd(true);

        mDatabase= FirebaseDatabase.getInstance().getReference().child("users");

        //--------------------------offline capablities---------------------------

        mDatabase.keepSynced(true);

        //-----------------------------------online status--------------------------
        auth=FirebaseAuth.getInstance();
        muserref= FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser().getUid());
        muserref.child("online").setValue("true");


    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Users> options=
                new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(mDatabase,Users.class)
                .setLifecycleOwner(this)
                .build();

        firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UserViewHolder>(options) {

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new UserViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.user_single_layout, parent, false));

            }



            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull Users model) {
                final String user_id=getRef(position).getKey();
                holder.setName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setThumb_image(model.getThumb_image(),getApplicationContext());
               // gender=model.getGender().toString();


                holder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent9=new Intent(UsersActivity.this,ProfileActivity.class);
                        intent9.putExtra("user_id",user_id);
                        startActivity(intent9);
                    }
                });
            }
        };
        muserlist.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        View mview;
        public UserViewHolder(View itemView) {
            super(itemView);
            mview=itemView;
        }
        public void setName(String name){
            EmojiTextView usernameview=(EmojiTextView ) mview.findViewById(R.id.singledisplayname);
            usernameview.setText(name);
        }
        public void setStatus(String status){
            EmojiTextView statusview=(EmojiTextView) mview.findViewById(R.id.singlestatus);
            statusview.setText(status);
        }

        public void setThumb_image(String thumb_image,Context ctx){
            CircleImageView circleImageView2=(CircleImageView)mview.findViewById(R.id.singleprofile_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.mipmap.icon).into(circleImageView2);
        }

    }

}
