package com.zsemberidaniel.egerbuszuj.activities

import android.os.Bundle
import android.support.v4.app.NotificationCompatSideChannelService
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.view.Menu
import android.view.MenuItem
import android.view.View

import com.zsemberidaniel.egerbuszuj.R
import com.zsemberidaniel.egerbuszuj.adapters.NextUpAdapter
import com.zsemberidaniel.egerbuszuj.adapters.TimetableAdapter
import com.zsemberidaniel.egerbuszuj.interfaces.presenters.ITimetablePresenter
import com.zsemberidaniel.egerbuszuj.interfaces.views.ITimetableView
import com.zsemberidaniel.egerbuszuj.misc.StaticStrings
import com.zsemberidaniel.egerbuszuj.misc.TodayType
import com.zsemberidaniel.egerbuszuj.presenters.TimetablePresenter
import com.zsemberidaniel.egerbuszuj.realm.objects.Route
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop
import com.zsemberidaniel.egerbuszuj.realm.objects.StopTime

import io.realm.Realm
import io.realm.Sort
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

/**
 * Created by zsemberi.daniel on 2017. 05. 14..
 */

class TimetableActivity : AppCompatActivity(), ITimetableView {

    private lateinit var timetablePresenter: ITimetablePresenter

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var timetableAdapter: TimetableAdapter

    private lateinit var nextUpRecyclerView: RecyclerView
    private lateinit var nextUpLayoutManager: LinearLayoutManager
    private lateinit var nextUpAdapter: NextUpAdapter

    private lateinit var toolbar: Toolbar

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

        timetablePresenter = TimetablePresenter(this, extras.getString(ARG_ROUTE_ID),
                extras.getString(ARG_STOP_ID), extras.getInt(ARG_DIRECTION, -1))


        recyclerView = findViewById(R.id.timetableRecycleView) as RecyclerView
        nextUpRecyclerView = findViewById(R.id.nextUpRecyclerView) as RecyclerView
        toolbar = findViewById(R.id.timetableToolbar) as Toolbar


        // improve performance because the content does not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)

        // use a linear layout manager
        layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = layoutManager

        // make adapter
        timetableAdapter = TimetableAdapter(null)
        timetableAdapter.setDisplayHeadersAtStartUp(true)
        timetableAdapter.setStickyHeaders(true)
        timetableAdapter.expandItemsAtStartUp()
        // set adapter
        recyclerView.adapter = timetableAdapter


        // Next Up Recycler view
        nextUpRecyclerView.setHasFixedSize(true)
        nextUpLayoutManager = LinearLayoutManager(this, OrientationHelper.HORIZONTAL, false)
        nextUpRecyclerView.layoutManager = nextUpLayoutManager

        nextUpAdapter = NextUpAdapter(null)
        nextUpRecyclerView.adapter = nextUpAdapter


        // Toolbar
        setSupportActionBar(toolbar)

        // add back arrow to toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        timetablePresenter.init()
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

        timetablePresenter.destroy()
    }

    override fun showNextUp(nextUpItems: List<StopTime>) {
        // make a list of max 10 NextUpItems
        val items: MutableList<NextUpAdapter.NextUpItem> = MutableList(nextUpItems.size, {
            NextUpAdapter.NextUpItem(this,
                    nextUpItems[it].trip?.route?.id ?: "ERR",
                    nextUpItems[it].trip?.headSign?.substring(0, 3)?.toUpperCase() ?: "ERR",
                    LocalTime(nextUpItems[it].hour.toInt(), nextUpItems[it].minute.toInt(), 0))
        })

        // add the items to the adapter
        nextUpAdapter.clear()
        nextUpAdapter.addItems(0, items)
    }

    override fun hideNextUp() {
        nextUpRecyclerView.visibility = View.GONE
    }

    /**
     * Shows the times from the times HashMaps
     */
    override fun showTimes(vararg times: HashMap<String, List<TimetableAdapter.HourMinutesOutput>>?) {
        if (times.isEmpty()) return

        timetableAdapter.clear()
        timetableAdapter.addItems(0, getTimesHeader(*times)?.toMutableList() ?: List(0, { TimetableAdapter.TimetableHeader("", "", "") }))
    }

    override fun expandAll() {
        timetableAdapter.expandAll()
    }

    /**
     * Returns a list with the items from the times HashMap. Also sets up the headers.
     * @param times An array of the times. The keys are route ids plus the head sign.
     * *              The value is a list of the hours and minutes. Because of the nature of the method
     * *              it will modify it so if you want the original back please !!!USE A COPY!!!
     * *
     * @return null if times has 0 elements. Otherwise a list of all the items with the correct headers.
     */
    private fun getTimesHeader(vararg times: HashMap<String, List<TimetableAdapter.HourMinutesOutput>>?):
            List<TimetableAdapter.TimetableHeader>? {
        if (times.isEmpty()) return null

        val output = ArrayList<TimetableAdapter.TimetableHeader>()

        // Move every HashMap to the very first item of the varargs
        var keyIterator: MutableIterator<String>?
        var key: String
        for (i in 1..times.size - 1) {
            keyIterator = times[i]?.keys?.iterator()

            while (keyIterator != null && keyIterator.hasNext()) {
                key = keyIterator.next()

                if (times[i]?.getValue(key) != null)
                    times[0]?.put(key, times[i]?.getValue(key)!!)
            }
        }

        // Now we can treat the first one as a collection of all the others
        keyIterator = times[0]?.keys?.iterator()

        // First sort the routeIDs so it is easier to find them
        val sortedKeys: MutableList<String> = MutableList(0, { "" })
        while (keyIterator != null && keyIterator.hasNext())
            sortedKeys.add(keyIterator.next())

        // sort by route ids, we need to split the keys because they are id - headSign
        val routeComp = Route.getRouteComparator()
        sortedKeys.sortWith(Comparator<String> { o1, o2 ->
            routeComp.compare(o1.split(StaticStrings.SEPARATOR)[0], o2.split(StaticStrings.SEPARATOR)[0])
        })

        val separator = StaticStrings.SEPARATOR
        // Then add the items in order
        if (times[0]?.keys?.size != null)
            for (i in 0 until sortedKeys.size) {
                val routeIDHeadSign = sortedKeys[i]
                val hours = times[0]?.getValue(routeIDHeadSign)
                val split = routeIDHeadSign.split(separator)

                val header = TimetableAdapter.TimetableHeader(split[0], split[1], routeIDHeadSign)
                if (hours != null)
                    for (k in hours.indices) {
                        header.addSubItem(TimetableAdapter.TimetableItem(baseContext, header, hours[k]))
                    }
                output.add(header)
            }

        return output
    }

    override fun setActionbarTitle(title: String) {
        supportActionBar?.title = title
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
        /**
         * Can be passed to this activity as an argument (Int).
         * It is a filter for the timetable: which direction to show
         */
        val ARG_DIRECTION = "direction"
    }
}
