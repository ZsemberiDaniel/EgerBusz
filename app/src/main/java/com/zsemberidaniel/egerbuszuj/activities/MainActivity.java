package com.zsemberidaniel.egerbuszuj.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zsemberidaniel.egerbuszuj.R;
import com.zsemberidaniel.egerbuszuj.TodayType;
import com.zsemberidaniel.egerbuszuj.fragments.ChooseStopFragment;
import com.zsemberidaniel.egerbuszuj.realm.FileToRealm;

import net.danlew.android.joda.JodaTimeAndroid;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JodaTimeAndroid.init(this);
        Realm.init(this);
        // TODO put these in some kind of load stuff
        new FileToRealm().execute(this);
        new TodayType().execute(this);

        viewPager = (ViewPager) findViewById(R.id.mainViewPager);
        viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
    }

    private class MainPagerAdapter extends FragmentPagerAdapter {

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return new ChooseStopFragment();
                default: return null;
            }
        }
    }
}
