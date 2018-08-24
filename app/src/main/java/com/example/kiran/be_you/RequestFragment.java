package com.example.kiran.be_you;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {
    private RecyclerView mFriend_reqlist1;
    private DatabaseReference mFriends_reqDatabase1;
    private DatabaseReference mDatabase1;
    private FirebaseAuth mAuth1;
    private String mcurrent_userid1;
    private View mMainview1;
    private Query db;
        public RequestFragment() {
            // Required empty public constructor
        }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainview1 =inflater.inflate(R.layout.fragment_request, container, false);
        mFriend_reqlist1 = (RecyclerView) mMainview1.findViewById(R.id.requestlist);
        mAuth1 = FirebaseAuth.getInstance();
        if (mAuth1.getCurrentUser()!=null)
        {
            mcurrent_userid1 = mAuth1.getCurrentUser().getUid();
        }
       else
        {
            startActivity(new Intent(getActivity(),LoginActivity.class));
        }


        mFriends_reqDatabase1 = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mcurrent_userid1);
         db=mFriends_reqDatabase1.orderByChild("request_type").equalTo("received");
        //mFriends_reqDatabase1.child("").child("request_type").equals("received");
        mDatabase1= FirebaseDatabase.getInstance().getReference().child("users");
        mFriend_reqlist1.setHasFixedSize(true);
        mFriend_reqlist1.setLayoutManager(new LinearLayoutManager(getContext()));
         return mMainview1;

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Friend_req> option_request=
                new FirebaseRecyclerOptions.Builder<Friend_req>()
                .setQuery(db,Friend_req.class)
                .setLifecycleOwner(this)
                .build();
        FirebaseRecyclerAdapter<Friend_req,Friends_reqViewHolder> friend_reqrecycleradapter=new
                FirebaseRecyclerAdapter<Friend_req, Friends_reqViewHolder>(option_request) {
                    @NonNull
                    @Override
                    public Friends_reqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_layout,parent,false);
                        return new Friends_reqViewHolder(mView);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final Friends_reqViewHolder holder, int position, @NonNull Friend_req model) {
                        // String uid=mFriends_reqDatabase1.child("from").getRef().toString();
                        // final String from_uid=model.getFrom();
                        final String list_user_id1 = getRef(position).getKey();
                        mDatabase1.child(list_user_id1).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String username = dataSnapshot.child("name").getValue().toString();
                                final String userthumb_img = dataSnapshot.child("thumb_image").getValue().toString();
                                String userstatus = dataSnapshot.child("status").getValue().toString();
                                // String useronline=dataSnapshot.child("online").getValue().toString();
                                holder.setName(username);
                                holder.setStatus(userstatus);
                                holder.setThumb_image(userthumb_img, getContext());

                                holder.mview1.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent8 = new Intent(getContext(), ProfileActivity.class);
                                        intent8.putExtra("user_id", list_user_id1);
                                        startActivity(intent8);
                                    }
                                });


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

               /*     @Override
            protected void populateViewHolder(final Friends_reqViewHolder viewHolder, Friend_req model, int position) {
              // String uid=mFriends_reqDatabase1.child("from").getRef().toString();
               // final String from_uid=model.getFrom();
                final String list_user_id1 = getRef(position).getKey();
                    mDatabase1.child(list_user_id1).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String username = dataSnapshot.child("name").getValue().toString();
                            final String userthumb_img = dataSnapshot.child("thumb_image").getValue().toString();
                            String userstatus = dataSnapshot.child("status").getValue().toString();
                            // String useronline=dataSnapshot.child("online").getValue().toString();
                            viewHolder.setName(username);
                            viewHolder.setStatus(userstatus);
                            viewHolder.setThumb_image(userthumb_img, getContext());

                            viewHolder.mview1.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent8 = new Intent(getContext(), ProfileActivity.class);
                                    intent8.putExtra("user_id", list_user_id1);
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
        mFriend_reqlist1.setAdapter(friend_reqrecycleradapter);
    }

    public static class Friends_reqViewHolder extends RecyclerView.ViewHolder {
        View mview1;
        public Friends_reqViewHolder(View itemView) {
            super(itemView);
            mview1=itemView;
        }
        public void setName(String name){
            TextView usernameview=(TextView) mview1.findViewById(R.id.item_name);
            usernameview.setText(name);
        }
        public void setStatus(String status){
            TextView userstatus=(TextView) mview1.findViewById(R.id.item_status);
            userstatus.setText(status);
        }

        public void setThumb_image(String thumb_image,Context ctx){
            CircleImageView circleImageView2=(CircleImageView)mview1.findViewById(R.id.item_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.mipmap.icon).into(circleImageView2);
        }
    }
}
