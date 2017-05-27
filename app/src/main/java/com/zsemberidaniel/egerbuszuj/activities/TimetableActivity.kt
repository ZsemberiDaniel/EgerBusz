package com.zsemberidaniel.egerbuszuj.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import com.zsemberidaniel.egerbuszuj.R
import com.zsemberidaniel.egerbuszuj.adapters.NextUpAdapter
import com.zsemberidaniel.egerbuszuj.adapters.TimetableAdapter
import com.zsemberidaniel.egerbuszuj.misc.TodayType
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop
import com.zsemberidaniel.egerbuszuj.realm.objects.StopTime

import io.realm.Realm
import io.realm.Sort
import org.joda.time.DateTime
import org.joda.time.LocalTime
import java.util.*

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

    private lateinit var nextUpRecyclerView: RecyclerView
    private lateinit var nextUpLayoutManager: LinearLayoutManager
    private var nextUpAdapter: NextUpAdapter? = null

    private var toolbar: Toolbar? = null

    private val updateTimer: Timer = Timer()

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


        recyclerView = findViewById(R.id.timetableRecycleView) as RecyclerView

        // improve performance because the content does not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)

        // use a linear layout manager
        layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = layoutManager

        if (filterStopID != null) {
            filterStopName = Realm.getDefaultInstance().where<Stop>(Stop::class.java)
                    .equalTo(Stop.CN_ID, filterStopID).findFirst().name

            // Get times in both direction
            val timesDir1 = TimetableAdapter.getAllTimes(this, filterStopID.toString(), filterRouteID, TodayType.todayType, 0)
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


        // Next Up Recycler view
        nextUpRecyclerView = findViewById(R.id.nextUpRecyclerView) as RecyclerView
        nextUpRecyclerView.setHasFixedSize(true)

        nextUpLayoutManager = LinearLayoutManager(this, OrientationHelper.HORIZONTAL, false)
        nextUpRecyclerView.layoutManager = nextUpLayoutManager

        updateNextUp(DateTime.now())
        updateTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateNextUp(DateTime.now())
            }

        }, (60 - DateTime.now().secondOfMinute + 3) * 1000L, 60000L)


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

    override fun onDestroy() {
        super.onDestroy()

        updateTimer.cancel()
    }

    /**
     * Updates the next up cards
     */
    fun updateNextUp(currTime: DateTime) {
        if (filterStopID == null) return

        // Get all times in this stop on this route, today, in both directions
        // After the currTime hour, sorted by time
        val times = TimetableAdapter.getRealmQueryFor(filterStopID!!, filterRouteID, TodayType.todayType)
                .greaterThanOrEqualTo(StopTime.CN_HOUR, currTime.hourOfDay)
                .greaterThanOrEqualTo(StopTime.CN_MINUTE, currTime.minuteOfHour)
                .findAll().sort(arrayOf(StopTime.CN_HOUR, StopTime.CN_MINUTE), arrayOf(Sort.ASCENDING, Sort.ASCENDING))

        if (times.size > 0) {
            val items: MutableList<NextUpAdapter.NextUpItem> = MutableList(Math.min(times.size, 10), {
                NextUpAdapter.NextUpItem(this, times[it].trip?.route?.id ?: "ERR",
                        times[it].trip?.headSign?.substring(0, 3)?.toUpperCase() ?: "ERR",
                        LocalTime(times[it].hour.toInt(), times[it].minute.toInt(), 0))
            })

            if (nextUpAdapter == null) {
                nextUpAdapter = NextUpAdapter(items)
                nextUpRecyclerView.adapter = nextUpAdapter
            } else {
                nextUpAdapter?.clear()
                nextUpAdapter?.addItems(0, items)
            }
        }
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
