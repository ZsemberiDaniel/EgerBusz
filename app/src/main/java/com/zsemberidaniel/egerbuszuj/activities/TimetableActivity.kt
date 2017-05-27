package com.zsemberidaniel.egerbuszuj.activities

import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout

import com.zsemberidaniel.egerbuszuj.R
import com.zsemberidaniel.egerbuszuj.adapters.TimetableAdapter
import com.zsemberidaniel.egerbuszuj.misc.TodayType
import com.zsemberidaniel.egerbuszuj.realm.objects.Route
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop

import java.util.ArrayList
import java.util.HashMap

import io.realm.Realm

/**
 * Created by zsemberi.daniel on 2017. 05. 14..
 */

class TimetableActivity : AppCompatActivity() {

    private var filterStopName: String? = null

    private var filterRouteID: String? = null
    private var filterStopID: String? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var timetableAdapter: TimetableAdapter

    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timetable)

        // Get arguments
        val extras: Bundle
        if (savedInstanceState == null) {
            extras = intent.extras
        } else {
            extras = savedInstanceState
        }

        filterRouteID = extras.getString(ARG_ROUTE_ID)
        filterStopID = extras.getString(ARG_STOP_ID)

        if (filterStopID != null) {

            recyclerView = findViewById(R.id.timetableRecycleView) as RecyclerView

            // improve performance because the content does not change the layout size of the RecyclerView
            recyclerView.setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = LinearLayoutManager(applicationContext)
            recyclerView.layoutManager = layoutManager

            filterStopName = Realm.getDefaultInstance().where<Stop>(Stop::class.java)
                    .equalTo(Stop.CN_ID, filterStopID).findFirst().name

            // Get times in both direction
            val timesDir1  = TimetableAdapter.getAllTimes(this, filterStopID.toString(), filterRouteID, TodayType.todayType, 0)
            val timesDir2 = TimetableAdapter.getAllTimes(this, filterStopID.toString(), filterRouteID, TodayType.todayType, 1)

            // Get UI items
            val items = TimetableAdapter.setupItems(this, timesDir1, timesDir2)

            // make adapter
            timetableAdapter = TimetableAdapter(items)
            timetableAdapter.setDisplayHeadersAtStartUp(true)
            timetableAdapter.setStickyHeaders(true)
            timetableAdapter.expandItemsAtStartUp()

            // set adapter
            recyclerView.adapter = timetableAdapter
        }


        // Toolbar
        toolbar = findViewById(R.id.timetableToolbar) as Toolbar
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // also set title
        if (filterRouteID != null && filterStopID == null) {
            supportActionBar?.setTitle(filterRouteID)
        } else if (filterStopID != null && filterRouteID == null) {
            supportActionBar?.setTitle(filterStopName)
        } else {
            supportActionBar?.setTitle(filterStopName + " - " + filterRouteID)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_timetable, menu)


        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {

        /**
         * Can be passed to this activity as an argument (String).
         * It is a filter for the timetable: what route(s) to show.
         */
        val ARG_ROUTE_ID = "routeID"
        /**
         * Can be passed to this activity as an argument (String).
         * It is a filter for the timetable: what stop(s) to show.
         */
        val ARG_STOP_ID = "stopID"
    }
}
