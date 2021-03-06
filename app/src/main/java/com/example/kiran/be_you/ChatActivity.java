package com.example.kiran.be_you;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    final String TAG = "ChatActivity";
    private String mchatuser;
    private Toolbar mchattoolbar;
    private DatabaseReference mRootref,muserref1;
    private FirebaseAuth mAuth;
    private String mCurrentuserid;
    private EmojiTextView mTitleview;
    private TextView mLast_seenview;
    private CircleImageView mprofileimageview;
    private RecyclerView mMessagelist;
    private SwipeRefreshLayout mRefreshlayout;
    private final List<Messages> messagesList=new ArrayList<>();
    private MessageAdapter mAdapter;
    private DatabaseReference mchatSeen;

    EmojiPopup emojiPopup;
    EmojiEditText editText;
    ImageView emojiButton;
    ViewGroup rootView;

    private  static final int GALLERY_PICK=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //EmojiManager.install(new IosEmojiProvider());

        mAuth=FirebaseAuth.getInstance();
        muserref1= FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        muserref1.child("online").setValue("true");

        mchattoolbar=(Toolbar) findViewById(R.id.toolbarchat);
        setSupportActionBar(mchattoolbar);
        android.support.v7.app.ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        rootView = findViewById(R.id.main_activity_root_view);

        mRootref= FirebaseDatabase.getInstance().getReference();
        mchatSeen=FirebaseDatabase.getInstance().getReference().child("chat");
        //-----------------chat----------------
        mCurrentuserid=mAuth.getCurrentUser().getUid();
        mchatuser=getIntent().getStringExtra("user_id");
        final String user_name=getIntent().getStringExtra("user_name");
        final String user_status=getIntent().getStringExtra("user_status");
        final String user_profile=getIntent().getStringExtra("user_profileimage");
        final String gender=getIntent().getStringExtra("gender");

        LayoutInflater inflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=Objects.requireNonNull(inflater).inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);
//---------------------custom action bar items
        mTitleview=(EmojiTextView)findViewById(R.id.chat_displayname);
        mLast_seenview=(TextView)findViewById(R.id.chat_Lastseen);
        mprofileimageview=(CircleImageView)findViewById(R.id.chat_profile_image);
        //mchat_messageview=(EditText)findViewById(R.id.write_message);
        //msend_message_btn=(ImageButton)findViewById(R.id.send_message);
        //mAdd_image_btn=(ImageButton)findViewById(R.id.addimage);
        EmojiTextView mchat_statusview=(EmojiTextView) findViewById(R.id.chat_status);
        mchat_statusview.setSelected(true);
        mAdapter=new MessageAdapter(messagesList);
        mMessagelist=(RecyclerView)findViewById(R.id.messageslist);
        mRefreshlayout=(SwipeRefreshLayout)findViewById(R.id.swiperefresh_layout);
       // mlinearlayout=new LinearLayoutManager(this);
        mMessagelist.setHasFixedSize(true);
        //mMessagelist.setLayoutManager(mlinearlayout);
        mMessagelist.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mMessagelist.setAdapter(mAdapter);

        loadMessages();

////////////////////////////////////////////EMOJI////////////////////

        editText = findViewById(R.id.main_activity_chat_bottom_message_edittext);
        emojiButton = findViewById(R.id.main_activity_emoji);
        final ImageView sendButton = findViewById(R.id.main_activity_send);

        emojiButton.setColorFilter(ContextCompat.getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);
        sendButton.setColorFilter(ContextCompat.getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);

        emojiButton.setOnClickListener(ignore -> emojiPopup.toggle());
        sendButton.setOnClickListener(ignore -> {
            sendMessage();
        });
/////////////////////////////////////////////////////////////////////
        //===================toolbar onclicklistner============================
        mchattoolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                intent.putExtra("user_id",mchatuser);
                startActivity(intent);
                finish();
            }
        });
//=========================================================================


       /* mAdd_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentgallary=new Intent();
                intentgallary.setType("image/*");
                intentgallary.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intentgallary,"select Image"),GALLERY_PICK);
            }
        });*/

        mTitleview.setText(user_name);
        mchat_statusview.setText(user_status);
        Picasso.with(getApplicationContext()).load(user_profile).placeholder(R.mipmap.icon).into(mprofileimageview);

      mRootref.child("users").child(mchatuser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String lastseenonline=dataSnapshot.child("online").getValue().toString();
                //String image=dataSnapshot.child("image").getValue().toString();
               if (lastseenonline.equals("true")){
                    mLast_seenview.setText("Online");
                }
                else{
                  Get_Time_ago getTimeAgo=new Get_Time_ago();
                   long lastTime=Long.parseLong(lastseenonline);

                   String last_seen_time=getTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                    mLast_seenview.setText(last_seen_time);
                   /*SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                   String time=sfd.format(new Date(lastseenonline));
                   mLast_seenview.setText(time);*/
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


     /*   msend_message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();

            }
        });*/
        setUpEmojiPopup();
    mRefreshlayout.setEnabled(false);

    }
    private void loadMessages() {
        DatabaseReference messageRef=mRootref.child("messages").child(mCurrentuserid).child(mchatuser);
        Query messageQuery=messageRef.limitToLast(100000);
//updated at 16/08/2017========orderbychild was required for ordering data(pushed data)=======================
        messageQuery.orderByChild("time").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message1 = dataSnapshot.getValue(Messages.class);
                String userid = dataSnapshot.getKey();
                messagesList.add(message1);
                mAdapter.notifyDataSetChanged();
                mMessagelist.scrollToPosition(messagesList.size() - 1);
                mRefreshlayout.setRefreshing(false);


            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });


        final DatabaseReference db1=mchatSeen;
        mchatSeen.child(mCurrentuserid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
             if (dataSnapshot.hasChild(mchatuser)){
                 db1.child(mCurrentuserid).child(mchatuser).child("seen").setValue(true);
                 db1.child(mchatuser).child(mCurrentuserid).child("seen").setValue(false);
             }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void sendMessage() {
        String message=editText.getText().toString().trim();
        if (!TextUtils.isEmpty(message)){
            String current_user_ref="messages/" + mCurrentuserid + "/" +mchatuser;
            String chat_user_ref="messages/" + mchatuser + "/" +mCurrentuserid;

            DatabaseReference user_message_push=mRootref.child("messages").child(mCurrentuserid).child(mchatuser)
                    .push();
            String push_id=user_message_push.getKey();
//=============================================================================================================================
            Map messagemap=new HashMap();
            messagemap.put("message",message);
            messagemap.put("seen",false);
            messagemap.put("type","text");
            messagemap.put("time",ServerValue.TIMESTAMP);
            messagemap.put("from",mCurrentuserid);
            Map message_user_map=new HashMap();
            message_user_map.put(current_user_ref +"/" + push_id,messagemap);
            message_user_map.put(chat_user_ref + "/ " + push_id,messagemap);
            editText.setText(" ");

//////////////////////////////////////////////Adding user in Chat database////////////////////////////////////////////////////////
            mchatSeen.child(mCurrentuserid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if (!dataSnapshot.hasChild(mchatuser)){
                     mRootref.child("chat").child(mCurrentuserid).addValueEventListener(new ValueEventListener() {
                         @Override
                         public void onDataChange(DataSnapshot dataSnapshot) {

                             if (!dataSnapshot.hasChild(mchatuser)){
                                 Map chataddmap=new HashMap();
                                 chataddmap.put("seen",false);
                                 chataddmap.put("timestamp", ServerValue.TIMESTAMP);

                                 Map  chatusermap=new HashMap();
                                 chatusermap.put("chat/" + mCurrentuserid + "/" + mchatuser,chataddmap);
                                 chatusermap.put("chat/" + mchatuser + "/" + mCurrentuserid,chataddmap);

                                 mRootref.updateChildren(chatusermap, new DatabaseReference.CompletionListener() {
                                     @Override
                                     public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                         if (databaseError!=null){
                                             Log.d("CHAT_LOG",databaseError.getMessage().toString());
                                         }
                                     }
                                 });
                             }
                         }
                         @Override
                         public void onCancelled(DatabaseError databaseError) { }
                     });
                 }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            DatabaseReference newNotificationref=mRootref.child("messages_notifications").child(mchatuser).push();
            String newNotificationid=newNotificationref.getKey();

            HashMap<String,String> notificationData=new HashMap<String, String>();
            notificationData.put("from",mCurrentuserid);
            notificationData.put("seen","false");
            notificationData.put("message",message);
            Map requestmap=new HashMap();
            requestmap.put("messages_notifications/" +mchatuser +"/" +newNotificationid,notificationData);

            DatabaseReference chatref=FirebaseDatabase.getInstance().getReference().child("chat");
                chatref.child(mCurrentuserid).child(mchatuser).child("timestamp").setValue(ServerValue.TIMESTAMP);
                chatref.child(mchatuser).child(mCurrentuserid).child("timestamp").setValue(ServerValue.TIMESTAMP);
                chatref.child(mCurrentuserid).child(mchatuser).child("seen").setValue(true);
                chatref.child(mchatuser).child(mCurrentuserid).child("seen").setValue(false);

            mRootref.updateChildren(requestmap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                }
            });
            try {

                mMessagelist.smoothScrollToPosition(messagesList.size() - 1);
            } catch (Exception e){
                e.printStackTrace();
            }
            mRootref.updateChildren(message_user_map, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError!=null){
                        Log.d("CHAT_LOG",databaseError.getMessage().toString());
                    }
                }
            });
        }

    }
    @Override public void onBackPressed() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    @Override protected void onStop() {
        if (emojiPopup != null) {
            emojiPopup.dismiss();
        }

        super.onStop();
    }

    private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                .setOnEmojiBackspaceClickListener(ignore -> Log.d(TAG, "Clicked on Backspace"))
                .setOnEmojiClickListener((ignore, ignore2) -> Log.d(TAG, "Clicked on emoji"))
                .setOnEmojiPopupShownListener(() -> emojiButton.setImageResource(R.drawable.ic_keyboard))
                .setOnSoftKeyboardOpenListener(ignore -> Log.d(TAG, "Opened soft keyboard"))
                .setOnEmojiPopupDismissListener(() -> emojiButton.setImageResource(R.drawable.emoji_ios_category_smileysandpeople))
                .setOnSoftKeyboardCloseListener(() -> Log.d(TAG, "Closed soft keyboard"))
                .build(editText);
    }
}
