package com.example.kiran.be_you;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



public class ProfileActivity extends AppCompatActivity {
    private ImageView mprofileimage;
    private EmojiTextView mdisplayname,mdisplaystatus;
    private Button msendbtn,mdecline_btn;
    private DatabaseReference mDatabaseref,mnotificationDatabase;
    private ProgressDialog mprogress,mprogress2,mprogress3;
    private String mcurrent_state;
    private DatabaseReference mFriendreqDatabase,mMessagedatabse,mchatdatabase;
    private DatabaseReference mFriendsDatabase,mrootref;
    private FirebaseUser mcurrentuser;
    private DatabaseReference muserref;
    //private  Picasso picasso;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //-----------------------------------online status--------------------------
        auth=FirebaseAuth.getInstance();
        muserref= FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser().getUid());
        muserref.child("online").setValue("true");
        //picasso=Picasso.with(this);
        mchatdatabase=FirebaseDatabase.getInstance().getReference().child("chat");

        //progressDialogs
        mprogress2=new ProgressDialog(this);
        mprogress3=new ProgressDialog(this);

        mprofileimage=(ImageView)findViewById(R.id.displayimage);
        mdisplayname=(EmojiTextView) findViewById(R.id.chat_displayname);
        mdisplaystatus=(EmojiTextView) findViewById(R.id.status);
       msendbtn=(Button)findViewById(R.id.sendfriendrequestbtn);
        mdecline_btn=(Button)findViewById(R.id.declinefriendrequestbtn);
        mdecline_btn.setVisibility(View.INVISIBLE);
        //state of user
        mcurrent_state="not_friends";

        mrootref=FirebaseDatabase.getInstance().getReference();

        //current user_id.
        mcurrentuser= FirebaseAuth.getInstance().getCurrentUser();
        final String userid=getIntent().getStringExtra("user_id");
        //Other user's user_id.
        mDatabaseref= FirebaseDatabase.getInstance().getReference().child("users").child(userid);
        mDatabaseref.keepSynced(true);
        //Friendrequest Database
        mFriendreqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendreqDatabase.keepSynced(true);
        //Friends Database
        mFriendsDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendsDatabase.keepSynced(true);
        //Notification Database
        mnotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        mnotificationDatabase.keepSynced(true);

        //message databse
        mMessagedatabse=FirebaseDatabase.getInstance().getReference().child("messages");
        mMessagedatabse.keepSynced(true);

        //progressDialog
        mprogress=new ProgressDialog(this);
        mprogress.setTitle("Loading user's data..");
        mprogress.setMessage("Please wait while loading!");
        mprogress.setCanceledOnTouchOutside(false);
        mprogress.show();


        mDatabaseref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name=dataSnapshot.child("name").getValue().toString();
                String display_status=dataSnapshot.child("status").getValue().toString();
                String display_image=dataSnapshot.child("image").getValue().toString();
                String Thumb_image=dataSnapshot.child("thumb_image").getValue().toString();
                mdisplayname.setText(display_name);
                mdisplaystatus.setText(display_status);
                String gender=dataSnapshot.child("gender").getValue().toString();

                if (gender.equals("female"))
                {

                    Picasso.with(ProfileActivity.this).load(Thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.mipmap.female_avatar).fit().into(mprofileimage, new Callback() {
                        @Override
                        public void onSuccess() {
                            mprogress.dismiss();
                        }
                        @Override
                        public void onError() {
                            mprogress.dismiss();
                            Toast.makeText(ProfileActivity.this,"Unable to load image Please check Internet!",Toast.LENGTH_LONG).show();
                        }
                    });

                }
                else
                {
                    Picasso.with(ProfileActivity.this).load(Thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.mipmap.male).fit().into(mprofileimage, new Callback() {
                        @Override
                        public void onSuccess() {
                            mprogress.dismiss();
                        }

                        @Override
                        public void onError() {
                            mprogress.dismiss();
                            Toast.makeText(ProfileActivity.this,"Unable to load image Please check Internet!",Toast.LENGTH_LONG).show();
                        }
                    });

                }

                //------CHECKING TO OWN ACCOUNT TO RESTRICT------------------
                if (mDatabaseref.child(mcurrentuser.getUid()).equals(mDatabaseref.child(userid))){
                    msendbtn.setVisibility(View.INVISIBLE);
                    mdecline_btn.setVisibility(View.INVISIBLE);
                    //mprogress.dismiss();
                   // Intent intentpro=new Intent(ProfileActivity.this,AccountSettingActivity.class);
                   // startActivity(intentpro);
                }

                //--------Friends List/Accept request------------------
                mFriendreqDatabase.child(mcurrentuser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(userid))
                        {
                            String req_type=dataSnapshot.child(userid).child("request_type").getValue().toString();
                            if (req_type.equals("received")){
                                mcurrent_state="req_received";
                                msendbtn.setText("Accept Friend Request");
                                mdecline_btn.setVisibility(View.VISIBLE);
                            }
                            else if(req_type.equals("sent")){
                                mcurrent_state="req_sent";
                                msendbtn.setText("Cancel Friend Request");
                            }
                            mprogress.dismiss();
                        }
                        //After accepting request checking state as friends and offer option as unfriend
                        else {
                            mFriendsDatabase.child(mcurrentuser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userid)){
                                        mcurrent_state="friends";
                                        msendbtn.setText("UnFriend");
                                        mprogress.dismiss();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mprogress.dismiss();
                                }
                            });
                        }

                       mprogress.dismiss();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        msendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                msendbtn.setEnabled(false);

                //------------------Not Friends state---------------------------------------------------
                if(mcurrent_state.equals("not_friends")){
                    mprogress2.setTitle("Sending Request..");
                    mprogress2.setMessage("Please wait,it takes less time to send");
                    mprogress2.setCanceledOnTouchOutside(false);
                    mprogress2.show();

                    DatabaseReference newNotificationref=mrootref.child("notifications").child(userid).push();
                    String newNotificationid=newNotificationref.getKey();

                    HashMap<String,String> notificationData=new HashMap<String, String>();
                    notificationData.put("from",mcurrentuser.getUid());
                    notificationData.put("type","request");

                    Map requestmap=new HashMap();
                    requestmap.put("Friend_req/" + mcurrentuser.getUid() + "/" + userid +"/request_type" ,"sent");
                    requestmap.put("Friend_req/" + userid + "/" + mcurrentuser.getUid() + "/request_type","received");
                    requestmap.put("notifications/" +userid +"/" +newNotificationid,notificationData);
                    mrootref.updateChildren(requestmap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError!=null){
                                Toast.makeText(ProfileActivity.this,"There was some error in sending friend request",Toast.LENGTH_LONG).show();
                            }
                            msendbtn.setEnabled(true);
                            mcurrent_state="req_sent";
                            msendbtn.setText("Cancel Friend Request");
                            mprogress2.dismiss();
                            Toast.makeText(ProfileActivity.this,"Request sent successfully!",Toast.LENGTH_LONG).show();
                        }
                    });
                }

//-----------------------------CANCEL FRIEND REQUEST  state ---------------------------------------------------------------
                if (mcurrent_state.equals("req_sent")){
                            mprogress3.setTitle("Canceling Request");
                            mprogress3.setMessage("Please wait,it will take less time!");
                            mprogress3.setCanceledOnTouchOutside(false);
                            mprogress3.show();
                       mFriendreqDatabase.child(mcurrentuser.getUid()).child(userid)
                               .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                                mFriendreqDatabase.child(userid).child(mcurrentuser.getUid())
                                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                mprogress3.dismiss();
                                msendbtn.setEnabled(true);
                                mcurrent_state="not_friends";
                                msendbtn.setText("Send Friend Request");

                        }
                    });

                        }
                    });
                        }
             //-----------req_received state handling----------------------
                 if (mcurrent_state.equals("req_received")) {
                     final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                     mFriendsDatabase.child(mcurrentuser.getUid()).child(userid).child("date")
                             .setValue(date).addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void aVoid) {
                             mFriendsDatabase.child(userid).child(mcurrentuser.getUid()).child("date")
                                     .setValue(date).addOnSuccessListener(new OnSuccessListener<Void>() {
                                 @Override
                                 public void onSuccess(Void aVoid) {
                                     mFriendreqDatabase.child(mcurrentuser.getUid()).child(userid)
                                             .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                         @Override
                                         public void onSuccess(Void aVoid) {
                                             mFriendreqDatabase.child(userid).child(mcurrentuser.getUid())
                                                     .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                 @Override
                                                 public void onSuccess(Void aVoid) {
                                                    mdecline_btn.setVisibility(View.INVISIBLE);
                                                     msendbtn.setEnabled(true);
                                                     mcurrent_state="friends";
                                                     msendbtn.setText("UnFriend");
                                                     // mnotificationDatabase.child(mcurrentuser.getUid()).removeValue();
                                                    mDatabaseref.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            String name=dataSnapshot.child("name").getValue().toString();
                                                            Toast.makeText(ProfileActivity.this,"You are now friend with\t" +name,Toast.LENGTH_LONG).show();
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                 }
                                             });

                                         }
                                     });



                                 }
                             });


                         }
                     });
                 }
                 //-----------------------------------------UNFRIEND------------------------------------------------
                 else if (mcurrent_state.equals("friends")){
                     mchatdatabase.child(mcurrentuser.getUid()).child(userid).removeValue();
                     mchatdatabase.child(userid).child(mcurrentuser.getUid()).removeValue();

                    mMessagedatabse.child(mcurrentuser.getUid()).child(userid).removeValue();
                    mMessagedatabse.child(userid).child(mcurrentuser.getUid()).removeValue();

                     mFriendsDatabase.child(mcurrentuser.getUid()).child(userid)
                             .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                         @Override
                         public void onSuccess(Void aVoid) {
                             mFriendsDatabase.child(userid).child(mcurrentuser.getUid())
                                     .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                 @Override
                                 public void onSuccess(Void aVoid) {
                                     msendbtn.setEnabled(true);
                                     mcurrent_state="not_friends";
                                     msendbtn.setText("Send Friend Request");
                                        mMessagedatabse.child(mcurrentuser.getUid()).child(userid)
                                                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mMessagedatabse.child(userid).child(mcurrentuser.getUid())
                                                        .removeValue();
                                            }
                                        });

                                 }
                             });

                         }
                     });

                 }
                 }

        });

        mdecline_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mdecline_btn.setVisibility(View.INVISIBLE);
                mFriendreqDatabase.child(mcurrentuser.getUid()).child(userid)
                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFriendreqDatabase.child(userid).child(mcurrentuser.getUid())
                                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                msendbtn.setEnabled(true);
                                mcurrent_state="not_friends";
                                Toast.makeText(ProfileActivity.this,"Successfully declined request!",Toast.LENGTH_LONG).show();                                msendbtn.setText("Send Friend Request");

                            }
                        });

                    }
                });

            }
        });


    }

}
