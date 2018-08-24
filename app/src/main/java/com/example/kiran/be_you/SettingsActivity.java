package com.example.kiran.be_you;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private static final int GALLERY_PICK = 1;

    @BindView(R.id.settings_image) CircleImageView mDisplayImage;
    @BindView(R.id.settings_name) TextView mName;
    @BindView(R.id.settings_status) TextView mStatus;

    private String mCurrentUid;
    private StorageReference mImageStorage;
    private DatabaseReference mUserDatabase;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        mImageStorage = FirebaseStorage.getInstance().getReference("profile_images");
        mCurrentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUserDatabase= FirebaseDatabase.getInstance().getReference("users/" + mCurrentUid);
        mUserDatabase.keepSynced(true);
        mUserDatabase.child("online").setValue("true");

        /////////////////////////////////////////////////
        // Load an ad into the AdMob banner view.
        /* AdView adView = (AdView) SettingsActivity(R.id.adView_accountsetting);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);*/
        ////////////////////////////////////////////////////////////

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String gender = dataSnapshot.child("gender").getValue().toString();
                mName.setText(name);
                mStatus.setText(status);

                if (gender.equals("female")) {
                    Picasso.with(SettingsActivity.this).load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.female_avatar).into(mDisplayImage, new Callback() {
                        @Override public void onSuccess() {}

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.mipmap.female_avatar).into(mDisplayImage);
                        }
                    });
                }
                else {
                    Picasso.with(SettingsActivity.this).load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.mipmap.male).into(mDisplayImage, new Callback() {
                        @Override public void onSuccess() {}

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.mipmap.male).into(mDisplayImage);
                        }
                    });
                }
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @OnClick(R.id.settings_image) void onImageClick() {
        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent,"select image"), GALLERY_PICK);
    }

    @OnClick(R.id.change_name_btn) void onChangeNameClick() {
        startActivity(new Intent(SettingsActivity.this, ChangeNameActivity.class));
    }

    @OnClick(R.id.change_status_btn) void onChangeStatusClick() {
        startActivity(new Intent(SettingsActivity.this,ChangeStatusActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK  &&  resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(3,3)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                mProgressDialog = new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image!");
                mProgressDialog.setMessage("Please wait while uploading image!");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                uploadImage(resultUri);
                uploadThumbImage(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                // Exception error = result.getError();
                Log.e(TAG, "onActivityResult: ", result.getError());
            }
        }
    }

    private void uploadImage(Uri resultUri) {
        final StorageReference filepath = mImageStorage.child(mCurrentUid + ".jpg");

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
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Map map = new HashMap();
                    map.put("image", downloadUri.toString());
                    mUserDatabase.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(SettingsActivity.this,"Successfully uploaded profile image!", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(SettingsActivity.this,"Failed to upload image!", Toast.LENGTH_LONG).show();
                            }
                            mProgressDialog.dismiss();
                        }
                    });

                } else {
                    Toast.makeText(SettingsActivity.this, "Upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadThumbImage(Uri resultUri) {
        final StorageReference thumbPath = mImageStorage.child("thumb_image/" + mCurrentUid + ".jpg");

        // compress image to thumbnail
        File thumbFilepath = new File(resultUri.getPath());
        Bitmap thumbBitmap= new Compressor(this)
                .setMaxWidth(250)
                .setMaxHeight(250)
                .setQuality(50)
                .compressToBitmap(thumbFilepath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] thumbByte = baos.toByteArray();

        // upload thumbnail image
        thumbPath.putBytes(thumbByte).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return thumbPath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Map map = new HashMap();
                    map.put("thumb_image", downloadUri.toString());
                    mUserDatabase.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(SettingsActivity.this,"Successfully uploaded profile image as thumbnail!", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(SettingsActivity.this,"Failed to upload image as thumbnail!", Toast.LENGTH_LONG).show();
                            }
                            mProgressDialog.dismiss();
                        }
                    });
                } else {
                    Toast.makeText(SettingsActivity.this, "Upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.profile_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.menu_requests:
                startActivity(new Intent(SettingsActivity.this,Sent_Requests.class));
                break;
            default:
                return false;
        }

        return true;
    }
}
