package rpc.zsemberidaniel.com.egerbusz.activity;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Calendar;
import java.util.TimeZone;

import rpc.zsemberidaniel.com.egerbusz.data.GTFS;
import rpc.zsemberidaniel.com.egerbusz.data.TodayType;
import rpc.zsemberidaniel.com.egerbusz.fragment.ChooseBusBothWaysFragment;
import rpc.zsemberidaniel.com.egerbusz.fragment.ChooseBusFragment;
import rpc.zsemberidaniel.com.egerbusz.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JodaTimeAndroid.init(this);
        TodayType.init(this);
        GTFS.initialize(this);

        Cursor debugCursor = GTFS.getInstance().getReadableDatabase().rawQuery(
            "SELECT * FROM " + GTFS.StopTimeTable.TABLE_NAME +
                " WHERE " + GTFS.StopTimeTable.COLUMN_NAME_TRIP_ID + " = '14C00';",
            null
        );
        while (debugCursor.moveToNext())
            Log.i("ASDASDASD", debugCursor.getString(debugCursor.getColumnIndex(GTFS.StopTimeTable.COLUMN_NAME_STOP_ID)));

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
            case 0: return new ChooseBusBothWaysFragment();
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return 1;
    }
}