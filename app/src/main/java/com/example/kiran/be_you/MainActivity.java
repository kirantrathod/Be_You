package com.example.kiran.be_you;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivity";

    @BindView(R.id.main_viewpager) ViewPager mViewPager;
    @BindView(R.id.main_tab) TabLayout mTabLayout;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserOnlineRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        // this listener will be called when there is change in firebase user session
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mCurrentUser = firebaseAuth.getCurrentUser();
                if (mCurrentUser == null) {
                    // user auth state is changed - user is null: launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else {
                    mUserOnlineRef = FirebaseDatabase.getInstance().getReference("users/" + mCurrentUser.getUid() + "/online");
                    mUserOnlineRef.setValue("true");
                    setupViewPager();
                }
            }
        };
    }

    private void setupViewPager() {
        SectionsPagerAdapter sectionsPageAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(sectionsPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        /*
         * ChatFragment is my default fragment. Which should open first when I open my application.
         * That's why I'm creating instance of ChatFragment changing ChatFragments container with
         * currentUid, so that when user changes frangments then he/she can get current user easily.
         */
        ChatFragment newFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("currentUid", mCurrentUser.getUid());
        newFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container2, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        mUserOnlineRef.setValue("true");

        switch (item.getItemId()) {
            case R.id.action_add:
                startActivity(new Intent(MainActivity.this, BlogActivity.class));
                break;
            case R.id.action_post:
                startActivity(new Intent(MainActivity.this, AfterBlogActivity.class));
                break;
            case R.id.menu_accountsettings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_alluser:
                startActivity(new Intent(MainActivity.this, UsersActivity.class));
                break;
            case R.id.menu_signout:
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to sign out?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                FirebaseAuth.getInstance().signOut();
                                finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                mUserOnlineRef.setValue(ServerValue.TIMESTAMP);
                break;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private static class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new RequestFragment();
                case 1:
                    return new ChatFragment();
                case 2:
                    return new FriendsFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            super.getPageTitle(position);
            switch (position) {
                case 0:
                    return "Requests";
                case 1:
                    return "Chats";
                case 2:
                    return "Friends";
                default:
                    return null;
            }
        }
    }
}
