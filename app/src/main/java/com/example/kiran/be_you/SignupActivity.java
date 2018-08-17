package com.example.kiran.be_you;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {
    private EditText display_name,inputEmail, inputPassword;
    private Button  btnSignUp, btnResetPassword;
    private ProgressDialog mprogress;
    private FirebaseAuth auth;
    private TextInputEditText genderEdittext;
    private DatabaseReference mDatabase,db1;
    private DatabaseReference mdevicetokendatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        // btnSignIn = (Button) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        display_name = (EditText) findViewById(R.id.displayname_textview);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);

//=========================================
        // Get a reference to the AutoCompleteTextView in the layout
        final AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_gender);
// Get the string array
        String[] countries = getResources().getStringArray(R.array.gender_array);
// Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, countries);
        textView.setAdapter(adapter);
  //====================================================

        mprogress=new ProgressDialog(this);
        mdevicetokendatabase= FirebaseDatabase.getInstance().getReference().child("users");

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayname = display_name.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
               // String gender=genderEdittext.getText().toString().trim();
                String gender=textView.getText().toString().trim();
                // reg_user(displayname,email,password);
                if (TextUtils.isEmpty(displayname)) {
                    Toast.makeText(getApplicationContext(), "Enter Name To Display Name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!gender.equals("male") ||gender.equals("female")){
                    Toast.makeText(getApplicationContext(), "Please Enter gender value in lowercase or make sure spelling is right", Toast.LENGTH_SHORT).show();
                    return;
                }
                mprogress.setTitle("Signing Up...");
                mprogress.setMessage("Please wait..You will be logged in after signing up");
                mprogress.setCanceledOnTouchOutside(false);
                mprogress.show();
               // progressBar.setVisibility(View.VISIBLE);
                reg_user(displayname, email, password,gender);

               /* //-------------------device token-------------------------------------
                String currentuser_id=auth.getCurrentUser().getUid();
                String DeviceToken= FirebaseInstanceId.getInstance().getToken();
                mdevicetokendatabase.child(currentuser_id).child("device_token").setValue(DeviceToken); */
            }

            private void reg_user(final String displayname, String email, final String password, final String gender) {
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(SignupActivity.this, "Your account has created.", Toast.LENGTH_SHORT).show();
                                //progressBar.setVisibility(View.GONE);
                                mprogress.dismiss();
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {

                                   // String currentuser_id=auth.getCurrentUser().getUid();
                                    String DeviceToken= FirebaseInstanceId.getInstance().getToken();
                                   // mdevicetokendatabase.child(currentuser_id).child("device_token").setValue(DeviceToken);
                                    //putting uid and data in realtime database
                                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                    String uid = currentUser.getUid();

                                    mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

                                    HashMap<String, String> usermap = new HashMap<>();
                                    usermap.put("name", displayname);
                                    usermap.put("status", "Hey,There I am using Be_You!");
                                    usermap.put("device_token",DeviceToken);
                                    usermap.put("image", "https://firebasestorage.googleapis.com/v0/b/beyou-a7151.appspot.com/o/profile_images%2FkTZxthCkQebTzdM7o5KC0miNxPw2.jpg?alt=media&token=8ba6019e-9f12-4714-bb4f-015e4b963b23");
                                   // usermap.put("thumb_image", "default");
                                    usermap.put("gender",gender);
                                    if (gender.equals("female"))
                                    {
                                        usermap.put("thumb_image", "https://firebasestorage.googleapis.com/v0/b/beyou-a7151.appspot.com/o/profile_images%2FFemale-Side-comb-O-neck-512.png?alt=media&token=31685e1a-be2e-4a73-a949-744af252a3b9");
                                    }
                                    else
                                    {
                                        usermap.put("thumb_image", "https://firebasestorage.googleapis.com/v0/b/beyou-a7151.appspot.com/o/profile_images%2Fmale.png?alt=media&token=c430b68f-0874-43c3-85c0-4b4418ec0b3e");
                                    }
                                    
                                   // usermap.put("you","You");
                                    //blog creating
                                  // usermap.put("title","post!");
                                   // usermap.put("desc","Hey its post");
                                    //usermap.put("blog_image","https://firebasestorage.googleapis.com/v0/b/beyou-a7151.appspot.com/o/profile_images%2FZbqQPAbUPIeAKPkMEXaIA8WU7rn2.jpg?alt=media&token=1bcc9ef7-519c-4707-8341-b7fec546d159");
                                    mDatabase.setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                                finish();
                                            }
                                        }
                                    });

                                }
                            }
                        });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mprogress.dismiss();
       // progressBar.setVisibility(View.GONE);
    }
}


