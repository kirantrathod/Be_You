package com.example.kiran.be_you;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ChatUserProfile extends AppCompatActivity {
    private ImageView mprofileimage;
    private TextView mdisplayname,mdisplaystatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_user_profile);
        mprofileimage=(ImageView)findViewById(R.id.displayimageuser);
        mdisplayname=(TextView)findViewById(R.id.chatuser_displayname);
        mdisplaystatus=(TextView)findViewById(R.id.chatuserstatus);
        final String user_name=getIntent().getStringExtra("user_name");
        final String user_status=getIntent().getStringExtra("user_status");
        final String user_profile=getIntent().getStringExtra("user_profileimage");
        final String gender=getIntent().getStringExtra("gender");
        if (gender.equals("female"))
        {
            Picasso.with(ChatUserProfile.this).load(user_profile).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.female_avatar).into(mprofileimage);
        }
        else
        {
            Picasso.with(ChatUserProfile.this).load(user_profile).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.male).into(mprofileimage);
        }

        mdisplayname.setText(user_name);
        mdisplaystatus.setText(user_status);

    }
}

