package com.example.kiran.be_you;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.R.attr.bitmap;

public class AccountSettingActivity extends AppCompatActivity {
    private DatabaseReference mUserDatabase;
    private FirebaseUser mcurrentuser;
    private Button mchangestatus,mchangeimage;
    private ImageButton mchangestatus2;
    private ImageButton mchange_displayname;
    private static final int GALLARY_PICK=1;
    private StorageReference mimagestorage;
    private ProgressDialog mprogress;
    private CircleImageView mDisplayImage;
    private TextView mname,mstatus;
    private DatabaseReference Muserref;
    private  FirebaseAuth Mauth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);
        //-----------------------------------online status--------------------------

        Mauth=FirebaseAuth.getInstance();
        Muserref= FirebaseDatabase.getInstance().getReference().child("users").child(Mauth.getCurrentUser().getUid());
        Muserref.child("online").setValue("true");

      // mchangestatus=(Button)findViewById(R.id.setting_statusbtn);
        //storage
        mimagestorage= FirebaseStorage.getInstance().getReference();

        mchangeimage=(Button)findViewById(R.id.setting_changeimage_btn);
        mchangeimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallaryintent=new Intent();
                gallaryintent.setType("image/*");
                gallaryintent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallaryintent,"select image"),GALLARY_PICK);
            }
        });

        mchange_displayname=(ImageButton) findViewById(R.id.changedisplayname_btn2);
        mchange_displayname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chngDisplaynameintent=new Intent(AccountSettingActivity.this,change_displayname.class);
                startActivity(chngDisplaynameintent);
            }
        });


       mchangestatus2=(ImageButton)findViewById(R.id.changestatusbtn2);
        mchangestatus2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent changestatusintent=new Intent(AccountSettingActivity.this,ChangeStatusActivity.class);
                startActivity(changestatusintent);
            }
        });
        /////////////////////////////////////////////////
        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView_accountsetting);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
        ////////////////////////////////////////////////////////////


        mDisplayImage=(CircleImageView)findViewById(R.id.settingcircleimage);
        mname=(TextView)findViewById(R.id.settings_displaynames);
        mstatus=(TextView)findViewById(R.id.setting_status);

        mcurrentuser= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid=mcurrentuser.getUid();

        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(current_uid);
        mUserDatabase.keepSynced(true);




        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String thumbnail=dataSnapshot.child("thumb_image").getValue().toString();
                String gender=dataSnapshot.child("gender").getValue().toString();
                mname.setText(name);
                mstatus.setText(status);
               // Picasso.with(AccountSettingActivity.this).load(image).placeholder(R.drawable.icon).into(mDisplayImage);
                if (gender.equals("female"))
                {
                    Picasso.with(AccountSettingActivity.this).load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.female_avatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(AccountSettingActivity.this).load(image).placeholder(R.mipmap.female_avatar).into(mDisplayImage);
                        }
                    });
                }
                else
                {
                    Picasso.with(AccountSettingActivity.this).load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.male).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(AccountSettingActivity.this).load(image).placeholder(R.mipmap.male).into(mDisplayImage);
                        }
                    });
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GALLARY_PICK&& resultCode==RESULT_OK){
            Uri imageuri=data.getData();
            CropImage.activity(imageuri)
                    .setAspectRatio(3,3)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                mprogress=new ProgressDialog(AccountSettingActivity.this);
                mprogress.setTitle("Uploading Image!");
                mprogress.setMessage("Please wait while uploading image!");
                mprogress.setCanceledOnTouchOutside(false);
                mprogress.show();
                Uri resultUri = result.getUri();
                File thumb_filepath=new File(resultUri.getPath());
                String currentuserid=mcurrentuser.getUid();

                Bitmap thumb_bitmap= new Compressor(this)
                        .setMaxWidth(250)
                        .setMaxHeight(250)
                        .setQuality(50)
                        .compressToBitmap(thumb_filepath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();
                StorageReference filepath=mimagestorage.child("profile_images").child(currentuserid +".jpg");
                final StorageReference thumbPath=mimagestorage.child("profile_images").child("thumb_image").child(currentuserid+".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            @SuppressWarnings("VisibleForTests") final
                            String downloadurl= task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumbPath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbtask) {
                                    @SuppressWarnings("VisibleForTests")
                                    String thumb_downloadurl=thumbtask.getResult().getDownloadUrl().toString();

                                    if(thumbtask.isSuccessful()){
                                        Map map=new HashMap();
                                        map.put("image",downloadurl);
                                        map.put("thumb_image",thumb_downloadurl);
                                        mUserDatabase.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    mprogress.dismiss();
                                                    Toast.makeText(AccountSettingActivity.this,"successfully Uploaded Profile Image!",Toast.LENGTH_LONG).show();
                                                }
                                                else {
                                                    Toast.makeText(AccountSettingActivity.this,"failed to upload thumb image!",Toast.LENGTH_LONG).show();
                                                    mprogress.dismiss();
                                                }
                                            }
                                        });

                                    }


                                }
                            });


                        }
                        else {
                            Toast.makeText(AccountSettingActivity.this,"failed to upload!",Toast.LENGTH_LONG).show();
                            mprogress.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==R.id.menu_requests){
            Intent addintent=new Intent(AccountSettingActivity.this,Sent_Requests.class);
            startActivity(addintent);
        }
        if (item.getItemId()==R.id.demo){
            Intent demointent=new Intent(AccountSettingActivity.this,demo.class);
            startActivity(demointent);
        }

        return super.onOptionsItemSelected(item);
    }


    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
