package com.example.kiran.be_you;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class show_demo extends AppCompatActivity {
    private DatabaseReference db1;
    private TextView mtext;
    private Button mupdate,mdel;
    private ProgressDialog mprogress2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_demo);
        db1= FirebaseDatabase.getInstance().getReference().child("demo");
        mtext=(TextView) findViewById(R.id.textis_here);
        mupdate=(Button)findViewById(R.id.update);
        mdel=(Button)findViewById(R.id.delete);


        mprogress2=new ProgressDialog(this);
       db1.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               String text=dataSnapshot.child("text").getValue().toString();
               mtext.setText(text);
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });
        mupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1=new Intent(show_demo.this,demo.class);
                startActivity(intent1);
                finish();
            }
        });
        mdel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mprogress2.setTitle("deletig text..");
                mprogress2.setMessage("please wait!");
                mprogress2.show();
                db1.child("text").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mprogress2.dismiss();
                        mtext.setEnabled(false);
                        Intent inte=new Intent(show_demo.this,finale.class);
                        startActivity(inte);
                        Toast.makeText(show_demo.this,"Text deleted successfully! ",Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });
    }
}
