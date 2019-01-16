package com.example.kiran.be_you;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.google.android.gms.tasks.Continuation;
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
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class BlogActivity extends AppCompatActivity {
    private ImageButton mselectimage;
    private EditText mposttitle;
    private EditText mpostdescription;
    private Button msubmitbtn;
    private static final int GALLARY_REQUEST = 1;
   private Uri mImageuri = null;
   private Uri resultUri;
    private byte[] thumb_byte;
    String Currentuseruid;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private ProgressDialog mprogress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);
        mStorage = FirebaseStorage.getInstance().getReference();
        //  FirebaseUser currentuser= FirebaseAuth.getInstance().getCurrentUser();
        //String Currentuseruid=currentuser.getUid();
        FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
        Currentuseruid = currentuser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("blogs").child(Currentuseruid).push();
        //String pushid=mDatabase.getKey();

        mposttitle = (EditText) findViewById(R.id.posttitle);
        mpostdescription = (EditText) findViewById(R.id.postdescription);
        msubmitbtn = (Button) findViewById(R.id.submit_post);
        mselectimage = (ImageButton) findViewById(R.id.add_imagebtn);
        mprogress = new ProgressDialog(this);
        mselectimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallaryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                gallaryIntent.setType("image/*");
                startActivityForResult(gallaryIntent, GALLARY_REQUEST);
            }
        });
        msubmitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLARY_REQUEST && resultCode == RESULT_OK) {
           /* mImageuri = data.getData();
            mselectimage.setImageURI(mImageuri);*/
            mImageuri=data.getData();

            CropImage.activity(mImageuri)
                    .setAspectRatio(3,3)
                    .start(this);

        }
///////////////////////////////////////////////////////////////////////////////////////////////////
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

        if (resultCode == RESULT_OK) {
            /*mprogress = new ProgressDialog(AccountSettingActivity.this);
            mprogress.setTitle("Uploading Image!");
            mprogress.setMessage("Please wait while uploading image!");
            mprogress.setCanceledOnTouchOutside(false);
            mprogress.show();*/
             resultUri = result.getUri();
            File thumb_filepath = new File(resultUri.getPath());
            //// Seting image to show when selected image on screen as selected
            mselectimage.setImageURI(resultUri);
            //String currentuserid = mcurrentuser.getUid();

            Bitmap thumb_bitmap = null;
            try {
                thumb_bitmap = new Compressor(this)
                        .setMaxWidth(250)
                        .setMaxHeight(250)
                        .setQuality(50)
                        .compressToBitmap(thumb_filepath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
             thumb_byte = baos.toByteArray();



        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Exception error = result.getError();
        }
    }
}
     //////////////////////////////////////////////////////////////////////////////////////////////////


    private void startPosting() {
        final String title_val = mposttitle.getText().toString().trim();
        final String description_val = mpostdescription.getText().toString().trim();
        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(description_val) && resultUri != null) {
            mprogress.setTitle("Posting Your post...");
            mprogress.setMessage("Please wait while loading!");
            mprogress.show();

            final StorageReference filepath = mStorage.child("Blog_images").child(resultUri.getLastPathSegment());
            final StorageReference thumbpath=mStorage.child("Blog_images").child("thumb_image").child(Currentuseruid+".jpg");

            filepath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task1) {
                    if (task1.isSuccessful()) {
                      /* @SuppressWarnings("VisibleForTests")
                    Task<Uri> downloadUrl = filepath.getDownloadUrl();*/
                        Uri downloadUri=task1.getResult();

                        final DatabaseReference newPost = mDatabase;
                        newPost.child("title").setValue(title_val);
                        newPost.child("desc").setValue(description_val);
                        newPost.child("timestamp").setValue(ServerValue.TIMESTAMP);
                        newPost.child("blog_image").setValue(downloadUri.toString());
                      //  mprogress.dismiss();
                        //////////////////////////////////////////////////////=========================/////////////////////////

                        thumbpath.putBytes(thumb_byte).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return thumbpath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    Map map = new HashMap();
                                    map.put("thumb_image", downloadUri.toString());
                                    newPost.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // mprogress.dismiss();
                                                Toast.makeText(BlogActivity.this,"successfully Uploaded Blog Image to thumbimage!",Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(BlogActivity.this,"failed to upload image to thumb!",Toast.LENGTH_LONG).show();
                                                // mprogress.dismiss();
                                            }
                                        }
                                    });

                                } else {
                                    Toast.makeText(BlogActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        mprogress.dismiss();
                        /////////////////////////////////////////////////////===========================//////////////////////////
                        Intent postintent = new Intent(BlogActivity.this, AfterBlogActivity.class);
                        startActivity(postintent);
                        finish();
                    }
                }
            });
}
    }
}


