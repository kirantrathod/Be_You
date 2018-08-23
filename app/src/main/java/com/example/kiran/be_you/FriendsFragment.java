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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment {
    private static final String TAG = "FriendsFragment";

    @BindView(R.id.friends_recycler_view) RecyclerView mFriendsRecyclerView;

    private DatabaseReference mDatabaseRef, mFriendsDatabase;

    public FriendsFragment() {} // Required empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserUid = currentUser.getUid();
            mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");
            mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("friends").child(currentUserUid);

            setRecyclerView();
        } else {
            startActivity(new Intent(getActivity(),LoginActivity.class));
            getActivity().finish();
        }
    }

    private void setRecyclerView() {
        mFriendsRecyclerView.setHasFixedSize(true);
        mFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseRecyclerAdapter adapter = newAdapter();
        mFriendsRecyclerView.setAdapter(adapter);
    }

    protected FirebaseRecyclerAdapter newAdapter() {

        FirebaseRecyclerOptions<Friend> options =
                new FirebaseRecyclerOptions.Builder<Friend>()
                        .setQuery(mFriendsDatabase,Friend.class)
                        .setLifecycleOwner(this)
                        .build();

        return new FirebaseRecyclerAdapter<Friend, FriendsViewHolder>(options) {
                    @NonNull
                    @Override
                    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_layout,parent,false);
                        return new FriendsViewHolder(mView);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friend model) {
                        holder.setDate(model.getDate());

                        final String listUserUid = getRef(position).getKey();
                        mDatabaseRef.child(listUserUid ).addValueEventListener(new ValueEventListener() {
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
                                        chatintent.putExtra("user_id",listUserUid );
                                        chatintent.putExtra("user_name",username);
                                        chatintent.putExtra("user_profileimage",userthumb_img);
                                        startActivity(chatintent);
                                    }
                                });
                            }

                            @Override public void onCancelled(DatabaseError databaseError) {}
                        });
                    }
                };
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
