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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Sent_Requests extends AppCompatActivity {
    private RecyclerView msent_requestlist;
    private DatabaseReference mDatabase;
    private DatabaseReference muserref;
    private Query db;
    private FirebaseAuth mAuth2;
    private String mcurrent_userid2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent__requests);
        mAuth2 = FirebaseAuth.getInstance();
        mcurrent_userid2 = mAuth2.getCurrentUser().getUid();
        msent_requestlist=(RecyclerView) findViewById(R.id.sent_request_recyclerview);
        msent_requestlist.setHasFixedSize(true);
        msent_requestlist.setLayoutManager(new LinearLayoutManager(this));
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mcurrent_userid2);
        db=mDatabase.orderByChild("request_type").equalTo("sent");
        muserref=FirebaseDatabase.getInstance().getReference().child("users");

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Friend_req> option3=
                new  FirebaseRecyclerOptions.Builder<Friend_req>()
                .setQuery(db,Friend_req.class)
                .setLifecycleOwner(this)
                .build();
        FirebaseRecyclerAdapter<Friend_req,Friend_req_sentViewHolder> sentrecycleradapter=new
                FirebaseRecyclerAdapter<Friend_req, Friend_req_sentViewHolder>(option3) {
                    @NonNull
                    @Override
                    public Friend_req_sentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        return new Friend_req_sentViewHolder(LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.user_single_layout, parent, false));
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final Friend_req_sentViewHolder holder, int position, @NonNull Friend_req model) {
                        final String list_user_id2 = getRef(position).getKey();
                        muserref.child(list_user_id2).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String username = dataSnapshot.child("name").getValue().toString();
                                final String userthumb_img = dataSnapshot.child("thumb_image").getValue().toString();
                                String userstatus = dataSnapshot.child("status").getValue().toString();
                                // String useronline=dataSnapshot.child("online").getValue().toString();
                                holder.setName(username);
                                holder.setStatus(userstatus);
                                holder.setThumb_image(userthumb_img, getApplicationContext());

                                holder.mview3.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent8 = new Intent(getApplicationContext(), ProfileActivity.class);
                                        intent8.putExtra("user_id", list_user_id2);
                                        startActivity(intent8);
                                    }
                                });


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

             /*       @Override
            protected void populateViewHolder(final Friend_req_sentViewHolder viewHolder, Friend_req model, int position) {
                final String list_user_id2 = getRef(position).getKey();
                muserref.child(list_user_id2).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("name").getValue().toString();
                        final String userthumb_img = dataSnapshot.child("thumb_image").getValue().toString();
                        String userstatus = dataSnapshot.child("status").getValue().toString();
                        // String useronline=dataSnapshot.child("online").getValue().toString();
                        viewHolder.setName(username);
                        viewHolder.setStatus(userstatus);
                        viewHolder.setThumb_image(userthumb_img, getApplicationContext());

                        viewHolder.mview3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent8 = new Intent(getApplicationContext(), ProfileActivity.class);
                                intent8.putExtra("user_id", list_user_id2);
                                startActivity(intent8);
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }*/
        };
        msent_requestlist.setAdapter(sentrecycleradapter);
    }
    public static class Friend_req_sentViewHolder extends RecyclerView.ViewHolder {
        View mview3;
        public Friend_req_sentViewHolder(View itemView) {
            super(itemView);
            mview3=itemView;
        }
        public void setName(String name){
            TextView usernameview=(TextView) mview3.findViewById(R.id.singledisplayname);
            usernameview.setText(name);
        }
        public void setStatus(String status){
            TextView userstatus=(TextView) mview3.findViewById(R.id.singlestatus);
            userstatus.setText(status);
        }

        public void setThumb_image(String thumb_image,Context ctx){
            CircleImageView circleImageView2=(CircleImageView)mview3.findViewById(R.id.singleprofile_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.mipmap.icon).into(circleImageView2);
        }
    }


}
