package com.example.kiran.be_you;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class BlogActivity extends AppCompatActivity {
    private ImageButton mselectimage;
    private EditText mposttitle;
    private EditText mpostdescription;
    private Button msubmitbtn;
    private static final int GALLARY_REQUEST=1;
    private Uri mImageuri=null;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
   // private FirebaseUser mcurrentuser;
    private ProgressDialog mprogress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);
        mStorage= FirebaseStorage.getInstance().getReference();
      //  FirebaseUser currentuser= FirebaseAuth.getInstance().getCurrentUser();
       //String Currentuseruid=currentuser.getUid();
        FirebaseUser currentuser= FirebaseAuth.getInstance().getCurrentUser();
        String Currentuseruid=currentuser.getUid();
      mDatabase= FirebaseDatabase.getInstance().getReference().child("blogs").child(Currentuseruid).push();
        //String pushid=mDatabase.getKey();

        mposttitle=(EditText) findViewById(R.id.posttitle);
        mpostdescription=(EditText)findViewById(R.id.postdescription);
        msubmitbtn=(Button) findViewById(R.id.submit_post);
        mselectimage=(ImageButton) findViewById(R.id.add_imagebtn);
        mprogress=new ProgressDialog(this);
        mselectimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallaryIntent=new Intent(Intent.ACTION_GET_CONTENT);
                gallaryIntent.setType("image/*");
                startActivityForResult(gallaryIntent,GALLARY_REQUEST);
            }
        });
        msubmitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });
    }

    private void startPosting() {
        final String title_val=mposttitle.getText().toString().trim();
        final String description_val=mpostdescription.getText().toString().trim();
        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(description_val) && mImageuri!=null){
            mprogress.setTitle("Posting Your post...");
            mprogress.setMessage("Please wait while loading!");
            mprogress.show();

            StorageReference filepath=mStorage.child("Blog_images").child(mImageuri.getLastPathSegment());

            filepath.putFile(mImageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                 // Uri downloadUrl= taskSnapshot.getDownloadUrl();

                    @SuppressWarnings("VisibleForTests")
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();


                    String pushid=mDatabase.getKey();

                DatabaseReference newPost=mDatabase;
                    newPost.child("title").setValue(title_val);
                    newPost.child("desc").setValue(description_val);
                    newPost.child("timestamp").setValue(ServerValue.TIMESTAMP);
                    newPost.child("blog_image").setValue(downloadUrl.toString());
                    mprogress.dismiss();
                    Intent postintent=new Intent(BlogActivity.this,AfterBlogActivity.class);
                    startActivity(postintent);
                    finish();
                  /*  HashMap<String, String> usermap = new HashMap<>();
                    usermap.put("title",title_val );
                    usermap.put("desc",description_val);
                    usermap.put("image",downloadUrl.toString());
                    mDatabase.setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mprogress.dismiss();
                                Toast.makeText(BlogActivity.this,"Your post has posted",Toast.LENGTH_LONG).show();
                            }
                        }
                    });*/



                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GALLARY_REQUEST && resultCode==RESULT_OK){
            mImageuri=data.getData();
            mselectimage.setImageURI(mImageuri);
        }
    }
}
