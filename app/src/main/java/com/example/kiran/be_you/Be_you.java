package com.example.kiran.be_you;

import android.app.Application;
import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import static android.support.v7.app.AppCompatDelegate.MODE_NIGHT_AUTO;

/**
 * Created by Kiran on 7/2/2017.
 */

public class Be_you extends MultiDexApplication {
    private DatabaseReference muserdatabase;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Picasso.Builder builder=new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built=builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
       mAuth=FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser()!=null) {
            muserdatabase = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(mAuth.getCurrentUser().getUid());
            muserdatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        muserdatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        EmojiManager.install(new IosEmojiProvider());





    }
}
