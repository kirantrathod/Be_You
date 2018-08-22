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

import com.example.kiran.be_you.model.Message;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
public class ChatFragment extends Fragment {
    private RecyclerView mchatist;
    private DatabaseReference mchatDatabase;
    private DatabaseReference mDatabase2,muserref2;
    private FirebaseAuth mAuth2;
    private TextView mtime;
    private FirebaseRecyclerAdapter<Message,chatviewholder> chatrecycleradapter;
    private String mcurrent_userid2;
    private View mMainview2;
   // private Query db2;
    private  DatabaseReference mRootref;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container1,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainview2 =inflater.inflate(R.layout.fragment_chat, container1, false);
        mchatist = (RecyclerView) mMainview2.findViewById(R.id.chatlist);
        mAuth2 = FirebaseAuth.getInstance();
        if (mAuth2.getCurrentUser()!=null)
        {
            mcurrent_userid2 = mAuth2.getCurrentUser().getUid();
        }
        else
        {
            startActivity(new Intent(getActivity(),LoginActivity.class));

        }

        mchatDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mcurrent_userid2);
        mchatDatabase.keepSynced(true);
        mRootref=FirebaseDatabase.getInstance().getReference();
        mDatabase2= FirebaseDatabase.getInstance().getReference().child("users");
        mDatabase2.keepSynced(true);
        mchatist.setHasFixedSize(true);
        mchatist.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainview2;

      //  return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        muserref2= FirebaseDatabase.getInstance().getReference().child("users").child(mAuth2.getCurrentUser().getUid());
        muserref2.child("online").setValue("true");
        FirebaseRecyclerOptions<Message> options2=
                new FirebaseRecyclerOptions.Builder<Message>()
                .setQuery(mchatDatabase,Message.class)
                .setLifecycleOwner(this)
                .build();
                chatrecycleradapter=new
                            FirebaseRecyclerAdapter<Message, chatviewholder>(options2) {
                    @NonNull
                    @Override
                    public chatviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_layout_single,parent,false);
                        return new ChatFragment.chatviewholder(mView);
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final chatviewholder holder, int position, @NonNull Message model) {
                        // viewHolder2.setMessage(model.getMessage());
                        // viewHolder2.setType(model.getType());
                        final String list_user_id1 = getRef(position).getKey();
                        DatabaseReference sequesnce=mRootref.child("chat").child(mcurrent_userid2).child(list_user_id1);
                        sequesnce.orderByChild("tiimestamp").addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                mDatabase2.child(list_user_id1).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String username = dataSnapshot.child("name").getValue().toString();
                                        final String userthumb_img = dataSnapshot.child("thumb_image").getValue().toString();
                                        final String userstatus=dataSnapshot.child("status").getValue().toString();
                                        final String gender=dataSnapshot.child("gender").getValue().toString();
                                        if (dataSnapshot.hasChild("online")){
                                            String useronline=dataSnapshot.child("online").getValue().toString();
                                            holder.setUserOnline(useronline);
                                        }
                                        //  String userstatus = dataSnapshot.child("status").getValue().toString();
                                        // String useronline=dataSnapshot.child("online").getValue().toString();
                                        holder.setName(username);
                                        //viewHolder2.setStatus(userstatus);
                                        holder.setThumb_image(userthumb_img, getContext());


                                        holder.mview2.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent10 = new Intent(getContext(), ChatActivity.class);
                                                intent10.putExtra("user_id", list_user_id1);
                                                intent10.putExtra("user_name",username);
                                                intent10.putExtra("user_status",userstatus);
                                                intent10.putExtra("user_profileimage",userthumb_img);
                                                intent10.putExtra("gender",gender);
                                                startActivity(intent10);
                                                muserref2.child("online").setValue("true");
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });



                        mchatDatabase.child(list_user_id1).orderByChild("time").addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot Snapshot, String s) {
                                String message=Snapshot.child("message").getValue().toString();
                                holder.setMessage(message);
                                String lastmsg_time=Snapshot.child("time").getValue().toString();
                                //String image=dataSnapshot.child("image").getValue().toString();

                                GetTimeAgo getTimeAgo=new GetTimeAgo();
                                long lastTime=Long.parseLong(lastmsg_time);

                                String last_message_time=getTimeAgo.getTimeAgo(lastTime,getContext());
                                //  mtime.setText(last_message_time);
                                holder.setTime(last_message_time);


                            }

                            @Override
                            public void onChildChanged(DataSnapshot Snapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot Snapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot Snapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

       /*             @Override
            protected void populateViewHolder(final chatviewholder viewHolder2, Message model, int position) {
               // viewHolder2.setMessage(model.getMessage());
               // viewHolder2.setType(model.getType());
                final String list_user_id1 = getRef(position).getKey();
                DatabaseReference sequesnce=mRootref.child("chat").child(mcurrent_userid2).child(list_user_id1);
                sequesnce.orderByChild("tiimestamp").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        mDatabase2.child(list_user_id1).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String username = dataSnapshot.child("name").getValue().toString();
                                final String userthumb_img = dataSnapshot.child("thumb_image").getValue().toString();
                                final String userstatus=dataSnapshot.child("status").getValue().toString();
                                if (dataSnapshot.hasChild("online")){
                                    String useronline=dataSnapshot.child("online").getValue().toString();
                                    viewHolder2.setUserOnline(useronline);
                                }
                                //  String userstatus = dataSnapshot.child("status").getValue().toString();
                                // String useronline=dataSnapshot.child("online").getValue().toString();
                                viewHolder2.setName(username);
                                //viewHolder2.setStatus(userstatus);
                                viewHolder2.setThumb_image(userthumb_img, getContext());


                                viewHolder2.mview2.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent10 = new Intent(getContext(), ChatActivity.class);
                                        intent10.putExtra("user_id", list_user_id1);
                                        intent10.putExtra("user_name",username);
                                        intent10.putExtra("user_status",userstatus);
                                        intent10.putExtra("user_profileimage",userthumb_img);
                                        startActivity(intent10);
                                        muserref2.child("online").setValue("true");
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



                mchatDatabase.child(list_user_id1).orderByChild("time").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot Snapshot, String s) {
                        String message=Snapshot.child("message").getValue().toString();
                        viewHolder2.setMessage(message);
                        String lastmsg_time=Snapshot.child("time").getValue().toString();
                        //String image=dataSnapshot.child("image").getValue().toString();

                            GetTimeAgo getTimeAgo=new GetTimeAgo();
                            long lastTime=Long.parseLong(lastmsg_time);

                            String last_message_time=getTimeAgo.getTimeAgo(lastTime,getContext());
                          //  mtime.setText(last_message_time);
                            viewHolder2.setTime(last_message_time);


                    }

                    @Override
                    public void onChildChanged(DataSnapshot Snapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot Snapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot Snapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }*/
        };
        mchatist.setAdapter(chatrecycleradapter);
    }
    public static class chatviewholder extends RecyclerView.ViewHolder {
        View mview2;
        public chatviewholder(View itemView) {
            super(itemView);
            mview2=itemView;
        }
        public void setName(String name){
            TextView usernameview1=(TextView) mview2.findViewById(R.id.singlemessagedisplayname);
            usernameview1.setText(name);
        }
       public void setMessage(String message){
           TextView usermessage=(TextView) mview2.findViewById(R.id.singlemessageview);
           usermessage.setText(message);
       }


        public void setThumb_image(String thumb_image,Context ctx){
            CircleImageView circleImageView21=(CircleImageView)mview2.findViewById(R.id.singlemessageprofile_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.mipmap.icon).into(circleImageView21);
        }
        public  void setUserOnline(String online_status){
            TextView useronline=(TextView) mview2.findViewById(R.id.online_messageicon);
            if (online_status.equals("true")){
                useronline.setVisibility(View.VISIBLE);
                //Toast.makeText(ChatFragment.this,)
            }
            else {
                useronline.setVisibility(View.INVISIBLE);
            }
        }
        public void setTime(String time){
            TextView timeview=(TextView) mview2.findViewById(R.id.time);
            timeview.setText(time);
        }
    }

}
