package com.example.kiran.be_you;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    @BindView(R.id.profile_image) ImageView mProfileImage;
    @BindView(R.id.profile_name) TextView mProfileName;
    @BindView(R.id.profile_status) TextView mProfileStatus;
    @BindView(R.id.btn_send_request) Button mSendBtn;
    @BindView(R.id.btn_decline_request) Button mDeclineBtn;

    private DatabaseReference mDbRef, mOtherUserRef;
    private String mCurrentUserUid, mOtherUserUid;
    private ProgressDialog mProgress, mProgress2, mProgress3;
    private String mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        mCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mOtherUserUid = getIntent().getStringExtra("userUid");
        mDbRef = FirebaseDatabase.getInstance().getReference();
        mDbRef.child("users/" + mCurrentUserUid + "/online").setValue("true");

        mOtherUserRef = mDbRef.child("users/" + mOtherUserUid);
        mOtherUserRef.keepSynced(true);
        mDbRef.child("friend_req").keepSynced(true);
        mDbRef.child("friends").keepSynced(true);
        mDbRef.child("notifications").keepSynced(true);
        mDbRef.child("messages").keepSynced(true);

        populateUserProfile();
    }

    private void populateUserProfile() {

        mProgress2 = new ProgressDialog(this);
        mProgress3 = new ProgressDialog(this);
        mProgress=new ProgressDialog(this);
        mProgress.setTitle("Loading user's data..");
        mProgress.setMessage("Please wait while loading!");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mOtherUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String profileName = dataSnapshot.child("name").getValue().toString();
                String profileStatus = dataSnapshot.child("status").getValue().toString();
                String thumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                mProfileName.setText(profileName);
                mProfileStatus.setText(profileStatus);

                String gender = dataSnapshot.child("gender").getValue().toString();
                if (gender.equals("female")) {
                    Picasso.with(ProfileActivity.this).load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.mipmap.female_avatar).into(mProfileImage);
                } else {
                    Picasso.with(ProfileActivity.this).load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.mipmap.male).into(mProfileImage);
                }

                checkIfFriend();
            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void checkIfFriend() {
        mCurrentState = "not_friends";
        mDbRef.child("friend_req/" + mCurrentUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mOtherUserUid)) {
                    String requestType = dataSnapshot.child(mOtherUserUid + "/request_type").getValue().toString();

                    if (requestType.equals("received")){
                        mCurrentState = "req_received";
                        mSendBtn.setText("Accept Friend Request");
                        mDeclineBtn.setVisibility(View.VISIBLE);
                    }
                    else if (requestType.equals("sent")){
                        mCurrentState = "req_sent";
                        mSendBtn.setText("Cancel Friend Request");
                    }
                }
                else { //After accepting request saving state as friends and offer unfriend option
                    mDbRef.child("friends/" + mCurrentUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(mOtherUserUid)){
                                mCurrentState="friends";
                                mSendBtn.setText("Unfriend");
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                            }
                        }

                        @Override public void onCancelled(DatabaseError databaseError) {}
                    });
                }
                mProgress.dismiss();
            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @OnClick(R.id.btn_send_request) void onSendClick() {
        mSendBtn.setEnabled(false);

        switch (mCurrentState) {
            case "not_friends":
                sendFriendRequest();
                break;
            case "req_sent":
                cancelFriendRequest();
                break;
            case "req_received":
                acceptFriendRequest();
                break;
            case "friends":
                unfriend();
                break;
        }
    }

    private void sendFriendRequest() {
        mProgress2.setTitle("Sending Request..");
        mProgress2.setMessage("Please wait,it takes less time to send");
        mProgress2.setCanceledOnTouchOutside(false);
        mProgress2.show();

        String newNotificationId = mDbRef.child("notifications/" + mOtherUserUid).push().getKey();

        HashMap<String, String> notificationData = new HashMap();
        notificationData.put("from", mCurrentUserUid);
        notificationData.put("type", "request");

        Map requestmap = new HashMap();
        requestmap.put("friend_req/" + mCurrentUserUid + "/" + mOtherUserUid + "/request_type", "sent");
        requestmap.put("friend_req/" + mOtherUserUid + "/" + mCurrentUserUid + "/request_type", "received");
        requestmap.put("notifications/" + mOtherUserUid + "/" + newNotificationId, notificationData);

        mDbRef.updateChildren(requestmap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Toast.makeText(ProfileActivity.this, "There was some error sending friend request", Toast.LENGTH_LONG).show();
                }
                mCurrentState = "req_sent";
                mSendBtn.setText("Cancel Friend Request");
                mSendBtn.setEnabled(true);
                mProgress2.dismiss();
                Toast.makeText(ProfileActivity.this, "Request sent successfully!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cancelFriendRequest() {
        mProgress3.setTitle("Canceling Request");
        mProgress3.setMessage("Please wait,it will take less time!");
        mProgress3.setCanceledOnTouchOutside(false);
        mProgress3.show();

        mDbRef.child("friend_req/" + mCurrentUserUid + "/" + mOtherUserUid)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDbRef.child("friend_req/" + mOtherUserUid + "/" + mCurrentUserUid)
                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mProgress3.dismiss();
                        mCurrentState = "not_friends";
                        mSendBtn.setText("Send Friend Request");
                        mSendBtn.setEnabled(true);
                    }
                });

            }
        });
    }

    private void acceptFriendRequest() {
        final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        Map friendsMap = new HashMap();
        friendsMap.put("friends/" + mCurrentUserUid + "/" + mOtherUserUid + "/date", date);
        friendsMap.put("friends/" + mOtherUserUid + "/" + mCurrentUserUid + "/date", date);
        friendsMap.put("friend_req/" + mCurrentUserUid + "/" + mOtherUserUid, null);
        friendsMap.put("friend_req/" + mOtherUserUid + "/" + mCurrentUserUid, null);

        mDbRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    mCurrentState = "friends";
                    mSendBtn.setText("Unfriend");
                    mSendBtn.setEnabled(true);

                    mDeclineBtn.setVisibility(View.INVISIBLE);
                    mDeclineBtn.setEnabled(false);
                }

                mOtherUserRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        Toast.makeText(ProfileActivity.this, "You are now friend with\t" + name, Toast.LENGTH_LONG).show();
                    }

                    @Override public void onCancelled(DatabaseError databaseError) {}
                });
            }
        });
    }

    private void unfriend() {

        Map friendsMap = new HashMap();
        friendsMap.put("friends/" + mCurrentUserUid + "/" + mOtherUserUid, null);
        friendsMap.put("friends/" + mOtherUserUid + "/" + mCurrentUserUid, null);
        friendsMap.put("messages/" + mCurrentUserUid + "/" + mOtherUserUid, null);
        friendsMap.put("messages/" + mOtherUserUid + "/" + mCurrentUserUid, null);

        mDbRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    mCurrentState = "not_friends";
                    mSendBtn.setText("Send Friend Request");
                    mSendBtn.setEnabled(true);
                }
            }
        });
        /*mFriendsDatabase.child(mCurrentUserUid).child(mOtherUserUid)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mFriendsDatabase.child(mOtherUserUid).child(mCurrentUserUid)
                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mSendBtn.setEnabled(true);
                        mCurrentState="not_friends";
                        mSendBtn.setText("Send Friend Request");

                        // TODO: add an option to delete the chats independently of the person
                        mMessagedatabse.child(mCurrentUserUid).child(mOtherUserUid)
                                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mMessagedatabse.child(mOtherUserUid).child(mCurrentUserUid)
                                        .removeValue();
                            }
                        });
                    }
                });
            }
        });*/
    }

    @OnClick(R.id.btn_decline_request) void onDeclineClick() {
        mDeclineBtn.setVisibility(View.INVISIBLE);

        mDbRef.child("friend_req/" + mCurrentUserUid + "/" + mOtherUserUid)
                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDbRef.child("friend_req/" + mOtherUserUid + "/" + mCurrentUserUid)
                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mCurrentState="not_friends";
                        mSendBtn.setText("Send Friend Request");
                        mSendBtn.setEnabled(true);
                        Toast.makeText(ProfileActivity.this,"Successfully declined request!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
