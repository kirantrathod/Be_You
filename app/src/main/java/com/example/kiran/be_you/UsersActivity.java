package com.example.kiran.be_you;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import static android.R.attr.id;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;



public class UsersActivity extends AppCompatActivity {
    private RecyclerView muserlist;
    private DatabaseReference mDatabase;
    private DatabaseReference muserref;
    private FirebaseAuth auth;
   // private FirebaseUser mcurrentuser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        muserlist=(RecyclerView) findViewById(R.id.alluserrecyclerview);
        muserlist.setHasFixedSize(true);
        muserlist.setLayoutManager(new LinearLayoutManager(this));
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
        FirebaseRecyclerAdapter<Users,UserViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UserViewHolder>(
                Users.class,
                R.layout.user_single_layout,
                UserViewHolder.class,
                mDatabase

        ) {
            @Override
            protected void populateViewHolder(final UserViewHolder viewHolder, Users model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setThumb_image(model.getThumb_image(),getApplicationContext());
                final String user_id=getRef(position).getKey();
                viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent9=new Intent(UsersActivity.this,ProfileActivity.class);
                        intent9.putExtra("user_id",user_id);
                        startActivity(intent9);
                    }
                });

            }

            @Override
            public void onBindViewHolder(UserViewHolder viewHolder, int position) {
                super.onBindViewHolder(viewHolder, position);
            }};
        muserlist.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        View mview;
        public UserViewHolder(View itemView) {
            super(itemView);
            mview=itemView;
        }
        public void setName(String name){
            TextView usernameview=(TextView ) mview.findViewById(R.id.singledisplayname);
            usernameview.setText(name);
        }
        public void setStatus(String status){
            TextView statusview=(TextView) mview.findViewById(R.id.singlestatus);
            statusview.setText(status);
        }

        public void setThumb_image(String thumb_image,Context ctx){
            CircleImageView circleImageView2=(CircleImageView)mview.findViewById(R.id.singleprofile_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.mipmap.icon).into(circleImageView2);
        }
    }

}
