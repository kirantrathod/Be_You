package com.example.kiran.be_you;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kiran on 6/12/2017.
 */

class SectionsPageAdapter extends FragmentPagerAdapter{
    private final List<Fragment> mFragmantList=new ArrayList<>();
    private final List<String> mFragmentTitleList=new ArrayList<>();
    public void addFragment(Fragment fragment,String title){
        mFragmantList.add(fragment);
        mFragmentTitleList.add(title);

    }
    public SectionsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmantList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmantList.size();
    }
}
