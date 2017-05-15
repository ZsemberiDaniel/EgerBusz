package com.zsemberidaniel.egerbuszuj.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.zsemberidaniel.egerbuszuj.R;
import com.zsemberidaniel.egerbuszuj.adapters.TimetableAdapter;
import com.zsemberidaniel.egerbuszuj.misc.TodayType;
import com.zsemberidaniel.egerbuszuj.realm.objects.Route;
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;

/**
 * Created by zsemberi.daniel on 2017. 05. 14..
 */

public class TimetableActivity extends AppCompatActivity {

    /**
     * Can be passed to this activity as an argument (String).
     * It is a filter for the timetable: what route(s) to show.
     */
    public static final String ARG_ROUTE_ID = "routeID";
    /**
     * Can be passed to this activity as an argument (String).
     * It is a filter for the timetable: what stop(s) to show.
     */
    public static final String ARG_STOP_ID = "stopID";

    private String filterStopName;

    private String filterRouteID;
    private String filterStopID;

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private TimetableAdapter timetableAdapter;

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

        // Get arguments
        Bundle extras;
        if (savedInstanceState == null) { extras = getIntent().getExtras(); }
        else { extras = savedInstanceState; }

        filterRouteID = extras.getString(ARG_ROUTE_ID);
        filterStopID = extras.getString(ARG_STOP_ID);

        if (filterStopID != null) {
            filterStopName = Realm.getDefaultInstance().where(Stop.class)
                    .equalTo(Stop.CN_ID, filterStopID).findFirst().getName();
        }

        recyclerView = (RecyclerView) findViewById(R.id.timetableRecycleView);

        // improve performance because the content does not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        // Get times in both direction
        HashMap<String, List<TimetableAdapter.HourMinutesOutput>> timesDir1 =
                TimetableAdapter.getAllTimes(filterStopID, filterRouteID, TodayType.getTodayType(), 0);
        HashMap<String, List<TimetableAdapter.HourMinutesOutput>> timesDir2 =
                TimetableAdapter.getAllTimes(filterStopID, filterRouteID, TodayType.getTodayType(), 1);

        // Get UI items
        List<TimetableAdapter.TimetableHeader> items = TimetableAdapter.setupItems(this, timesDir1, timesDir2);

        // make adapter
        timetableAdapter = new TimetableAdapter(items);
        timetableAdapter.setDisplayHeadersAtStartUp(true);
        timetableAdapter.setStickyHeaders(true);
        timetableAdapter.expandItemsAtStartUp();

        // set adapter
        recyclerView.setAdapter(timetableAdapter);


        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.timetableToolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        ActionBar actionBar;
        if ((actionBar = getSupportActionBar()) != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);

            // also set title
            if (filterRouteID != null && filterStopID == null) {
                actionBar.setTitle(filterRouteID);
            } else if (filterStopID != null && filterRouteID == null) {
                actionBar.setTitle(filterStopName);
            } else {
                actionBar.setTitle(filterStopName + " - " + filterRouteID);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timetable, menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
