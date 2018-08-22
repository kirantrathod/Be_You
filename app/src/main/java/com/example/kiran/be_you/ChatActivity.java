package com.example.kiran.be_you;

import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.kiran.be_you.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private String mchatuser;
    private Toolbar mchattoolbar;
    private DatabaseReference mRootref,muserref1;
    private FirebaseAuth mAuth;
    private String mCurrentuserid;
    private TextView mTitleview;
    private TextView mLast_seenview;
    private CircleImageView mprofileimageview;
    private EditText mchat_messageview;
    private ImageButton msend_message_btn;
    private ImageButton mAdd_image_btn;
    private RecyclerView mMessagelist;
    private SwipeRefreshLayout mRefreshlayout;
    private final List<Message> messagesList=new ArrayList<>();
    private LinearLayoutManager mlinearlayout;
    private MessageAdapter mAdapter;
    private static final int TOTAL_ITEMS_TO_LOAD=6;
    private int mcurrentPage=1;
    private int itemPos=0;
    private String mLastkey="";
    private  String mPrevkey="";
    private  static final int GALLERY_PICK=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        muserref1= FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        muserref1.child("online").setValue("true");

        mchattoolbar=(Toolbar) findViewById(R.id.toolbarchat);
       // mProfileimage=(CircleImageView) SettingsActivity(R.id.chat_profile_image) ;
       setSupportActionBar(mchattoolbar);
       android.support.v7.app.ActionBar actionBar=getSupportActionBar();
       actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);



        mRootref= FirebaseDatabase.getInstance().getReference();
        //-----------------chat----------------
        //mAuth=FirebaseAuth.getInstance();
        mCurrentuserid=mAuth.getCurrentUser().getUid();

        mchatuser=getIntent().getStringExtra("user_id");
        final String user_name=getIntent().getStringExtra("user_name");
        final String user_status=getIntent().getStringExtra("user_status");
        final String user_profile=getIntent().getStringExtra("user_profileimage");
        final String gender=getIntent().getStringExtra("gender");
//===================toolbar onclicklistner============================
        mchattoolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, ChatUserProfile.class);
                intent.putExtra("user_id",mchatuser);
                intent.putExtra("user_name",user_name);
                intent.putExtra("user_status",user_status);
                intent.putExtra("user_profileimage",user_profile);
                intent.putExtra("gender",gender);
                startActivity(intent);
                finish();
            }
        });
//=========================================================================
       //actionBar.setTitle(user_name);

        LayoutInflater inflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view=inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);
//---------------------custom action bar items
        mTitleview=(TextView)findViewById(R.id.chat_displayname);
        mLast_seenview=(TextView)findViewById(R.id.chat_Lastseen);
        mprofileimageview=(CircleImageView)findViewById(R.id.chat_profile_image);
        mchat_messageview=(EditText)findViewById(R.id.write_message);
        msend_message_btn=(ImageButton)findViewById(R.id.send_message);
        mAdd_image_btn=(ImageButton)findViewById(R.id.addimage);
        TextView  mchat_statusview=(TextView)findViewById(R.id.chat_status);
        mchat_statusview.setSelected(true);
        mAdapter=new MessageAdapter(messagesList);
        mMessagelist=(RecyclerView)findViewById(R.id.messageslist);
        mRefreshlayout=(SwipeRefreshLayout)findViewById(R.id.swiperefresh_layout);
        mlinearlayout=new LinearLayoutManager(this);
        mMessagelist.setHasFixedSize(true);
        mMessagelist.setLayoutManager(mlinearlayout);
        mMessagelist.setAdapter(mAdapter);
        loadMessages();
        


        mAdd_image_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentgallary=new Intent();
                intentgallary.setType("image/*");
                intentgallary.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intentgallary,"select Image"),GALLERY_PICK);
            }
        });

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
                  GetTimeAgo getTimeAgo=new GetTimeAgo();
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
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        msend_message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();

            }
        });
    /*    mRefreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mcurrentPage++;
                 itemPos=0;
                messagesList.clear();
                loadMessages();

            }
        });
*/
    mRefreshlayout.setEnabled(false);

    }

    private void loadMoreMessages() {
        DatabaseReference messageRef=mRootref.child("messages").child(mCurrentuserid).child(mchatuser);
        Query messageQuery1=messageRef.orderByKey().endAt(mLastkey).limitToLast(10);
        messageQuery1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message2=dataSnapshot.getValue(Message.class);
                String messagekey=dataSnapshot.getKey();

                if (!mPrevkey.equals(messagekey))
                {
                    //messagesList.add(itemPos++,message2);
                }
                else
                {
                    mPrevkey=mLastkey;
                }
                if (itemPos==1)
                {
                    mLastkey=messagekey;
                }

                //messagesList.clear();
                Log.d("TOTALKEYS","Last key:" +mLastkey+"|Prev key:"+mPrevkey+"| message key" + messagekey);
                mAdapter.notifyDataSetChanged();
                mMessagelist.scrollToPosition(messagesList.size() -1);
                //messagesList.clear();
                mRefreshlayout.setRefreshing(false);
                mlinearlayout.scrollToPositionWithOffset(10,0);

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

    }

    private void loadMessages() {
        DatabaseReference messageRef=mRootref.child("messages").child(mCurrentuserid).child(mchatuser);
        Query messageQuery=messageRef.limitToLast(1000);

//updated at 16/08/2017========orderbychild was required for ordering data(pushed data)=======================
        messageQuery.orderByChild("time").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message1=dataSnapshot.getValue(Message.class);
               // itemPos++;
                //String messagekey=dataSnapshot.getKey();
                /*if (itemPos==1){

                    mLastkey=messagekey;
                    mPrevkey=messagekey;
                }*/
                messagesList.add(message1);
                mAdapter.notifyDataSetChanged();
                mMessagelist.scrollToPosition(messagesList.size() -1);
                mRefreshlayout.setRefreshing(false);

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



    }

    private void sendMessage() {
        String message=mchat_messageview.getText().toString();
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
            mchat_messageview.setText(" ");

            DatabaseReference newNotificationref=mRootref.child("messages_notifications").child(mchatuser).push();
            String newNotificationid=newNotificationref.getKey();

            HashMap<String,String> notificationData=new HashMap<String, String>();
            notificationData.put("from",mCurrentuserid);
            notificationData.put("seen","false");
            notificationData.put("message",message);
            Map requestmap=new HashMap();
            requestmap.put("messages_notifications/" +mchatuser +"/" +newNotificationid,notificationData);

            DatabaseReference chatref=FirebaseDatabase.getInstance().getReference()
                    .child("chat");
                chatref.child(mCurrentuserid).child(mchatuser).child("timestamp").setValue(ServerValue.TIMESTAMP);
                chatref.child(mchatuser).child(mCurrentuserid).child("timestamp").setValue(ServerValue.TIMESTAMP);

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


}
