package com.zsemberidaniel.egerbuszuj.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.zsemberidaniel.egerbuszuj.R;
import com.zsemberidaniel.egerbuszuj.misc.TodayType;
import com.zsemberidaniel.egerbuszuj.fragments.ChooseStopFragment;
import com.zsemberidaniel.egerbuszuj.realm.FileToRealm;

import net.danlew.android.joda.JodaTimeAndroid;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private Toolbar toolbar;
    private MaterialSearchView searchView;

    /**
     * The search menu item, which we need to be disabled when a route is selected
     */
    private MenuItem searchMenuItem;

    /**
     * Adapter for the viewPager
     */
    private MainPagerAdapter mainPagerAdapter;

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
        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mainPagerAdapter);

        toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mainPagerAdapter.getChooseStopFragment().updateStopFilter(newText);

                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_all_stops, menu);

        searchMenuItem = menu.findItem(R.id.actionSearch);
        searchView.setMenuItem(searchMenuItem);

        return super.onCreateOptionsMenu(menu);
    }

    private class MainPagerAdapter extends FragmentPagerAdapter {

        private ChooseStopFragment chooseStopFragment;
        public ChooseStopFragment getChooseStopFragment() { return chooseStopFragment; }

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
                case 0:
                    chooseStopFragment = new ChooseStopFragment();

                    return chooseStopFragment;
                default: return null;
            }
        }
    }
}
