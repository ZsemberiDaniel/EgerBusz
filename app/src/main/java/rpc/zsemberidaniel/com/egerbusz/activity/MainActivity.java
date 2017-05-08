package rpc.zsemberidaniel.com.egerbusz.activity;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import rpc.zsemberidaniel.com.egerbusz.fragment.ChooseBusFragment;
import rpc.zsemberidaniel.com.egerbusz.R;
import rpc.zsemberidaniel.com.egerbusz.data.Lines;
import rpc.zsemberidaniel.com.egerbusz.data.Stations;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Stations.init(this);
        Lines.init(this);

        ViewPager viewPager = (ViewPager) findViewById(R.id.mainViewPager);
        FragmentPagerAdapter fragmentPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentPagerAdapter);
    }
}

class MainPagerAdapter extends FragmentPagerAdapter {

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return new ChooseBusFragment();
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return 1;
    }
}