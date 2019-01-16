package com.example.kiran.be_you;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

public class change_displayname extends AppCompatActivity {

    final String TAG = "change_displayname";

    private Button msavechanges;
    //private EditText mdiplayname;
    //firebase
    private DatabaseReference mDatabasereference;
    private FirebaseUser mcurrentuser;

    EmojiPopup emojiPopup;
    EmojiEditText editText;
    ImageView emojiButton;
    ViewGroup rootView;
    //progressbar
    private ProgressDialog mprogress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_displayname);


        rootView = findViewById(R.id.changeDisplayNameLayout);
        editText = findViewById(R.id.main_activity_chat_bottom_message_edittext);
        emojiButton = findViewById(R.id.main_activity_emoji);

        emojiButton.setColorFilter(ContextCompat.getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);

        emojiButton.setOnClickListener(ignore -> emojiPopup.toggle());

        //firebase
        mcurrentuser= FirebaseAuth.getInstance().getCurrentUser();
        String currentuid=mcurrentuser.getUid();
        mDatabasereference= FirebaseDatabase.getInstance().getReference().child("users").child(currentuid);

        msavechanges=(Button) findViewById(R.id.savechangbtn1);
        //mdiplayname=(EditText)findViewById(R.id.changedisplayname_plaintext);
        msavechanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //progressbar
                mprogress=new ProgressDialog(change_displayname.this);
                mprogress.setTitle("Saving Changes!");
                mprogress.setMessage("Please wait while loading.");
                mprogress.setCanceledOnTouchOutside(false);
                mprogress.show();

                String status=editText.getText().toString().trim();
                mDatabasereference.child("name").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mprogress.dismiss();
                            Intent reverseintent=new Intent(change_displayname.this,AccountSettingActivity.class);
                            startActivity(reverseintent);

                        }else{
                            Toast.makeText(getApplicationContext(),"There was some error in saving changes",Toast.LENGTH_LONG).show();
                        }

                    }
                });

            }
        });

        setUpEmojiPopup();


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
