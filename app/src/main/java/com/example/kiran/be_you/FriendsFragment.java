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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kiran.be_you.model.Friend;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    private RecyclerView mFriendlist;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mDatabase2;
    private FirebaseAuth mAuth;
    private String mcurrent_userid;
    private View mMainview;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainview = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendlist = (RecyclerView) mMainview.findViewById(R.id.friendslist);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser()!=null)
        {
            mcurrent_userid = mAuth.getCurrentUser().getUid();
        }
        else
        {
            startActivity(new Intent(getActivity(),LoginActivity.class));

        }

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friend").child(mcurrent_userid);
        mDatabase2= FirebaseDatabase.getInstance().getReference().child("users");
        mFriendlist.setHasFixedSize(true);
        mFriendlist.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainview;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Friend> optionfiriends=
                new FirebaseRecyclerOptions.Builder<Friend>()
                .setQuery(mFriendsDatabase,Friend.class)
                .setLifecycleOwner(this)
                .build();
        FirebaseRecyclerAdapter<Friend, FriendsViewHolder> friendsRecyclerAdapter = new
                FirebaseRecyclerAdapter<Friend, FriendsViewHolder>(optionfiriends)

                {
                    @NonNull
                    @Override
                    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_layout,parent,false);
                        return new FriendsViewHolder(mView);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friend model) {
                        holder.setDate(model.getDate());

                        final String list_user_id=getRef(position).getKey();
                        mDatabase2.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String username=dataSnapshot.child("name").getValue().toString();
                                //final String img=dataSnapshot.child("image").getValue().toString();
                                final String userthumb_img=dataSnapshot.child("thumb_image").getValue().toString();
                                String userstatus=dataSnapshot.child("status").getValue().toString();
                                // String useronline=dataSnapshot.child("online").getValue().toString();
                                holder.setName(username);
                                holder.setStatus(userstatus);
                                holder.setImage(userthumb_img,getContext());
                                if (dataSnapshot.hasChild("online")){
                                    String useronline=dataSnapshot.child("online").getValue().toString();
                                    holder.setUserOnline(useronline);
                                }


                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent chatintent=new Intent(getContext(),ChatActivity.class);
                                        chatintent.putExtra("user_id",list_user_id);
                                        chatintent.putExtra("user_name",username);
                                        chatintent.putExtra("user_profileimage",userthumb_img);
                                        startActivity(chatintent);
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                /*    @Override
                    protected void populateViewHolder(final FriendsViewHolder friendsviewholder, final Friend
                            friends, int position) {

                        friendsviewholder.setDate(friends.getDate());

                        final String list_user_id=getRef(position).getKey();
                        mDatabase2.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                 final String username=dataSnapshot.child("name").getValue().toString();
                                 final String userthumb_img=dataSnapshot.child("thumb_image").getValue().toString();
                                String userstatus=dataSnapshot.child("status").getValue().toString();
                               // String useronline=dataSnapshot.child("online").getValue().toString();
                                friendsviewholder.setName(username);
                                friendsviewholder.setStatus(userstatus);
                                friendsviewholder.setThumb_image(userthumb_img,getContext());
                                if (dataSnapshot.hasChild("online")){
                                    String useronline=dataSnapshot.child("online").getValue().toString();
                                    friendsviewholder.setUserOnline(useronline);
                                }


                                friendsviewholder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Intent chatintent=new Intent(getContext(),ChatActivity.class);
                                        chatintent.putExtra("user_id",list_user_id);
                                        chatintent.putExtra("user_name",username);
                                        chatintent.putExtra("user_profileimage",userthumb_img);
                                        startActivity(chatintent);
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }*/
                };

        mFriendlist.setAdapter(friendsRecyclerAdapter);
    }
    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
     View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }
        public  void setDate(String date)
        {
            TextView datefrom=(TextView) mView.findViewById(R.id.item_date);
            datefrom.setText(date);

        }
        public void setName(String name){
            TextView usernameview=(TextView) mView.findViewById(R.id.item_name);
            usernameview.setText(name);
        }
        public void setStatus(String status){
            TextView userstatus=(TextView) mView.findViewById(R.id.item_status);
            userstatus.setText(status);
        }

      public void setImage(String image,Context ctx){
            CircleImageView circleImageView2=(CircleImageView)mView.findViewById(R.id.item_image);
            Picasso.with(ctx).load(image).placeholder(R.mipmap.icon).into(circleImageView2);
        }
        public  void setUserOnline(String online_status){
            ImageView useronline=(ImageView) mView.findViewById(R.id.online_icon);
            if (online_status.equals("true")){
                useronline.setVisibility(View.VISIBLE);
            }
            else {
                useronline.setVisibility(View.INVISIBLE);
            }
        }

    }
}
