package com.example.kiran.be_you;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeNameActivity extends AppCompatActivity {
    private Button msavechanges;
    private EditText mdiplayname;
    //firebase
    private DatabaseReference mDatabasereference;
    private FirebaseUser mcurrentuser;

    //progressbar
    private ProgressDialog mprogress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_displayname);
        //firebase
        mcurrentuser= FirebaseAuth.getInstance().getCurrentUser();
        String currentuid=mcurrentuser.getUid();
        mDatabasereference= FirebaseDatabase.getInstance().getReference().child("users").child(currentuid);

        msavechanges=(Button) findViewById(R.id.savechangbtn1);
        mdiplayname=(EditText)findViewById(R.id.changedisplayname_plaintext);
        msavechanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //progressbar
                mprogress=new ProgressDialog(ChangeNameActivity.this);
                mprogress.setTitle("Saving Changes!");
                mprogress.setMessage("Please wait while loading.");
                mprogress.setCanceledOnTouchOutside(false);
                mprogress.show();

                String status=mdiplayname.getText().toString();
                mDatabasereference.child("name").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mprogress.dismiss();
                            Intent reverseintent=new Intent(ChangeNameActivity.this,SettingsActivity.class);
                            startActivity(reverseintent);

                        }else{
                            Toast.makeText(getApplicationContext(),"There was some error in saving changes",Toast.LENGTH_LONG).show();
                        }

                    }
                });

            }
        });


    }
}
