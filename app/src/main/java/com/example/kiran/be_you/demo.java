package com.example.kiran.be_you;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class demo extends AppCompatActivity {
    private DatabaseReference d1;
    private EditText display_text;
    private FirebaseAuth auth;
    private ProgressDialog mprogress;
    private Button submit,ad,ad1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        mprogress=new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        display_text = (EditText) findViewById(R.id.demo_text);

        submit=(Button) findViewById(R.id.demo_submitbtn);
        ad=(Button)findViewById(R.id.adbutton);
        ad1=(Button)findViewById(R.id.adbutton1);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mprogress.setTitle("Loading..");
                mprogress.setMessage("Please wait");
                mprogress.show();
                String demotext = display_text.getText().toString().trim();
                d1= FirebaseDatabase.getInstance().getReference().child("demo");
                d1.child("text").setValue(demotext).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mprogress.dismiss();
                        Intent intent=new Intent(demo.this,show_demo.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
        ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(demo.this,AdShowing_Activity.class);
                startActivity(intent);
            }
        });
        ad1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent int1=new Intent(demo.this,First_Interstitial_Ad.class);
                startActivity(int1);
            }
        });

    }
}
