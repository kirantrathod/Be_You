package com.example.kiran.be_you;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import static android.R.attr.data;
import static android.R.attr.id;



public class MainActivity extends AppCompatActivity
        implements ForceUpdateChecker.OnUpdateNeededListener {
  private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseUser mcurretuser;
   private DatabaseReference muserref;
    private  static final String TAG="MainActivity";
    private SectionsPageAdapter mSectionsPageAdapter;
    private ViewPager mviewPager;
    private TabLayout tabLayout;
    private int[] tabIcons = {
            R.mipmap.ic_group_white,
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth=FirebaseAuth.getInstance();


        // this listener will be called when there is change in firebase user session
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
                else{
                    mcurretuser=user;
                   muserref= FirebaseDatabase.getInstance().getReference().child("users")
                            .child(auth.getCurrentUser().getUid());
                    muserref.child("online").setValue("true");
                   // muserref.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    Log.d(TAG,"onCreate: starting.");
                    //viewpager
                    mSectionsPageAdapter=new SectionsPageAdapter(getSupportFragmentManager());
                    mviewPager=(ViewPager) findViewById(R.id.mainviewpager);
                    setupViewPager(mviewPager);
                    //tabs
                    tabLayout=(TabLayout) findViewById(R.id.main_tab);
                    tabLayout.setupWithViewPager(mviewPager);
                   // setupTabIcons();
                    //----------------------------------------------------------
                }

            }
        };



   /*    Log.d(TAG,"onCreate: starting.");
        //viewpager
        mSectionsPageAdapter=new SectionsPageAdapter(getSupportFragmentManager());
        mviewPager=(ViewPager) findViewById(R.id.mainviewpager);
        setupViewPager(mviewPager);
        //tabs
        TabLayout tabLayout=(TabLayout) findViewById(R.id.main_tab);
        tabLayout.setupWithViewPager(mviewPager);   */

    }

  /*  private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
    }*/

    private void setupViewPager(final ViewPager viewPager){
        SectionsPageAdapter sectionsPageAdapter=new SectionsPageAdapter(getSupportFragmentManager());
        sectionsPageAdapter.addFragment(new RequestFragment(),"Requests");
        sectionsPageAdapter.addFragment(new ChatFragment(),"Chats");
        sectionsPageAdapter.addFragment(new FriendsFragment(),"Friends");
        viewPager.setAdapter(sectionsPageAdapter);
        viewPager.setCurrentItem(1);


        ChatFragment newFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("currentuser",mcurretuser.getUid());
        newFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container2, newFragment);
        transaction.addToBackStack(null);


        // Commit the transaction
        transaction.commit();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_add){
            Intent addintent=new Intent(MainActivity.this,BlogActivity.class);
            muserref.child("online").setValue("true");
            startActivity(addintent);
        }
        if (item.getItemId()==R.id.action_post){
            Intent postintent=new Intent(MainActivity.this,AfterBlogActivity.class);
            muserref.child("online").setValue("true");
            startActivity(postintent);
        }
        if (item.getItemId()==R.id.menu_accountsettings){
            Intent settingintent=new Intent(MainActivity.this,AccountSettingActivity.class);
            muserref.child("online").setValue("true");

            startActivity(settingintent);
           // mprogress.dismiss();


        }
        if (item.getItemId()==R.id.action_alluser){
            startActivity(new Intent(MainActivity.this,UsersActivity.class));
            muserref.child("online").setValue("true");
        }
     if (item.getItemId()==R.id.menu_signout){
         //////

         new AlertDialog.Builder(this)
                 .setMessage("Are you sure you want to Sign out?")
                 .setCancelable(false)
                 .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                     public void onClick(DialogInterface dialog, int id) {
                         FirebaseAuth.getInstance().signOut();
                         finish();
                     }
                 })
                 .setNegativeButton("No", null)
                 .show();
         ///////

         muserref.child("online").setValue(ServerValue.TIMESTAMP);
     }
        return super.onOptionsItemSelected(item);
    }

    //sign out method
    public void signOut() {auth.signOut();
    }
   @Override
    protected void onResume() {
        super.onResume();

    }

   @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }

      //  muserref= FirebaseDatabase.getInstance().getReference().child("users").child(auth.getCurrentUser().getUid());
      /*  mcurretuser=FirebaseAuth.getInstance().getCurrentUser();
        if(mcurretuser !=null) {
            muserref.child("online").setValue(ServerValue.TIMESTAMP);
        }*/


    }
///////////////////////////////////////////////////////
    @Override
    public void onUpdateNeeded(String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New version available")
                .setMessage("Please, update app to new version,You will be amazed to see new features")
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton("No, thanks",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).create();
        dialog.show();
    }
    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
