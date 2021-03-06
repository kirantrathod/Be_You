package com.example.kiran.be_you;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.squareup.picasso.NetworkPolicy;
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
    private LinearLayoutManager mlinaerlayout;
    private String mcurrent_userid;
    private View mMainview;
    private Query queryDate;

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

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mcurrent_userid);
        queryDate=mFriendsDatabase.orderByChild("date");
        queryDate.keepSynced(true);
        mDatabase2= FirebaseDatabase.getInstance().getReference().child("users");
        mDatabase2.keepSynced(true);
        mFriendlist.setHasFixedSize(true);
        mlinaerlayout=new LinearLayoutManager(getContext());
        mFriendlist.setLayoutManager(mlinaerlayout);
        mlinaerlayout.setReverseLayout(true);
        mlinaerlayout.setStackFromEnd(true);

        return mMainview;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Friends> optionfiriends=
                new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(queryDate,Friends.class)
                .setLifecycleOwner(this)
                .build();
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerAdapter = new
                FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(optionfiriends)

                {
                    @NonNull
                    @Override
                    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout,parent,false);
                        return new FriendsViewHolder(mView);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
                        holder.setDate(model.getDate());

                        final String list_user_id=getRef(position).getKey();
                        mDatabase2.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String username=dataSnapshot.child("name").getValue().toString();
                                //final String img=dataSnapshot.child("image").getValue().toString();
                                final String userthumb_img=dataSnapshot.child("thumb_image").getValue().toString();
                                final String gender=dataSnapshot.child("gender").getValue().toString();
                                final  String status=dataSnapshot.child("status").getValue().toString();
                                String userstatus=dataSnapshot.child("status").getValue().toString();

                                holder.setName(username);
                                holder.setStatus(userstatus);
                                holder.setImage(userthumb_img,getContext());
                                if (dataSnapshot.hasChild("online")){
                                    String useronline=dataSnapshot.child("online").getValue().toString();
                                    holder.setUserOnline(useronline);
                                }

                                //////////////////////////////////////



                            /*    holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        PopupMenu popup = new PopupMenu(view.getContext(), holder.mView);
                                        //inflating menu from xml resource
                                        popup.inflate(R.menu.popup_menu);

                                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {
                                                switch (item.getItemId()) {
                                                    case R.id.action_settings_c:
                                                        mDatabase.child(post_key).removeValue();
                                                        //handle menu1 click
                                                        break;
                                                    case R.id.card_del:
                                                        //handle menu2 click
                                                        break;
                                                }
                                                return false;
                                            }
                                        });
                                        //displaying the popup
                                        popup.show();

                                    }
                                });*/


                                ///////////////////////////////////////
                                holder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ///////////////////
                                        PopupMenu popup=new PopupMenu(v.getContext(),holder.mView);
                                        popup.inflate(R.menu.popup_menu);
                                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item) {
                                                switch (item.getItemId()) {
                                                    case R.id.messageto:
                                                       // mDatabase.child(post_key).removeValue();
                                                        //handle menu1 click
                                                        Intent chatintent=new Intent(getContext(),ChatActivity.class);
                                                        chatintent.putExtra("user_id",list_user_id);
                                                        chatintent.putExtra("user_name",username);
                                                        chatintent.putExtra("user_profileimage",userthumb_img);
                                                        chatintent.putExtra("user_status",status);
                                                        chatintent.putExtra("gender",gender);
                                                        startActivity(chatintent);
                                                        break;
                                                    case R.id.toprofile:
                                                        //handle menu2 click
                                                        Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                                                        profileIntent.putExtra("user_id",list_user_id);
                                                        /*profileIntent.putExtra("user_name",username);
                                                        profileIntent.putExtra("user_profileimage",userthumb_img);
                                                        profileIntent.putExtra("user_status",status);
                                                        profileIntent.putExtra("gender",gender);*/
                                                        startActivity(profileIntent);
                                                        break;
                                                }
                                                return false;
                                            }
                                        });
                                        popup.show();

                                        ////////////////////////
                                       /* Intent chatintent=new Intent(getContext(),ChatActivity.class);
                                        chatintent.putExtra("user_id",list_user_id);
                                        chatintent.putExtra("user_name",username);
                                        chatintent.putExtra("user_profileimage",userthumb_img);
                                        chatintent.putExtra("user_status",status);
                                        chatintent.putExtra("gender",gender);
                                        startActivity(chatintent);*/
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                /*    @Override
                    protected void populateViewHolder(final FriendsViewHolder friendsviewholder, final Friends
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
            TextView datefrom=(TextView) mView.findViewById(R.id.singledate);
            datefrom.setText(date);

        }
        public void setName(String name){
            TextView usernameview=(TextView) mView.findViewById(R.id.singledisplayname);
            usernameview.setText(name);
        }
        public void setStatus(String status){
            TextView userstatus=(TextView) mView.findViewById(R.id.singlestatus);
            userstatus.setText(status);
        }

      public void setImage(String image,Context ctx){
            CircleImageView circleImageView2=(CircleImageView)mView.findViewById(R.id.singleprofile_image);
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
