package com.zsemberidaniel.egerbuszuj.presenters

import android.annotation.SuppressLint
import android.support.annotation.IntRange
import com.zsemberidaniel.egerbuszuj.adapters.TimetableAdapter
import com.zsemberidaniel.egerbuszuj.interfaces.presenters.ITimetablePresenter
import com.zsemberidaniel.egerbuszuj.interfaces.views.ITimetableView
import com.zsemberidaniel.egerbuszuj.misc.StaticStrings
import com.zsemberidaniel.egerbuszuj.misc.TodayType
import com.zsemberidaniel.egerbuszuj.realm.objects.Route
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop
import com.zsemberidaniel.egerbuszuj.realm.objects.StopTime
import com.zsemberidaniel.egerbuszuj.realm.objects.Trip
import io.realm.Realm
import org.joda.time.DateTime
import java.util.*

/**
 * Created by zsemberi.daniel on 2017. 05. 29..
 */
class TimetablePresenter(val view: ITimetableView, val filterRouteID: String?, val filterStopID: String?, val filterDirection: Int?)
    : ITimetablePresenter {

    private val filterStopName: String? =
            Realm.getDefaultInstance().where(Stop::class.java).equalTo(Stop.CN_ID, filterStopID ?: "")?.findFirst()?.name

    private val updateTimer = Timer()

    override fun init() {
        setToolbarTitle()
        loadTimes()

        updateNextUp()
        updateTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateNextUp()
            }
        }, (60 - DateTime.now().secondOfMinute + 3) * 1000L, 60000L)
    }

    override fun destroy() {
        updateTimer.cancel()
    }

    override fun loadTimes() {
        if (filterStopID != null) {
            // Get times in both direction
            val timesDir1: HashMap<String, List<TimetableAdapter.HourMinutesOutput>>? =
                    if (filterDirection == -1 || filterDirection == 0)
                        getAllTimes(filterStopID.toString(), filterRouteID, TodayType.todayType, 0)
                    else null

            val timesDir2: HashMap<String, List<TimetableAdapter.HourMinutesOutput>>? =
                    if (filterDirection == -1 || filterDirection == 1)
                        getAllTimes(filterStopID.toString(), filterRouteID, TodayType.todayType, 1)
                    else null

            // Get UI items
            if (timesDir1 == null) view.showTimes(timesDir2)
            else if (timesDir2 == null) view.showTimes(timesDir1)
            else view.showTimes(timesDir1, timesDir2)

            // we have a route so we only gonna have one route -> expand it
            if (filterRouteID != null) {
                view.expandAll()
            }
        }
    }

    /**
     * Returns the times a bus is at a stop. If we want we can even specify which bus (route).
     * @param stop The stop for which this function returns the hours and minutes
     * *
     * @param route Optional. If we only want the hours for a given route for the stop
     * *
     * @param dayType What type of day we want it for
     * *
     * @param direction Which direction the bus goes
     * *
     * @return The keys are route ids plus the head sign (so if route is given there will only be one). The value is a list of the hours and minutes.
     */
    @SuppressLint("Range")
    override fun getAllTimes(stop: String, route: String?,
                    @IntRange(from = 0, to = 4) dayType: Int,
                    @IntRange(from = 0, to = 1) direction: Int): HashMap<String, List<TimetableAdapter.HourMinutesOutput>>? {
        // Order like this because we want to group by routes then the time
        val sortedTimes = getRealmQueryFor(stop, route, dayType, direction).sortedWith(compareBy({ it.trip?.id }, { it.hour }, { it.minute }))

        // if we have no StopTimes with the given attributes return null
        if (sortedTimes.isEmpty()) return null

        val output = java.util.HashMap<String, List<TimetableAdapter.HourMinutesOutput>>()
        val hours = ArrayList<TimetableAdapter.HourMinutesOutput>()

        // what are the current groups
        var currentRoute = sortedTimes[0].trip?.route?.id
        var currentHeadSign = sortedTimes[0].trip?.headSign
        var currentHourMinutes = TimetableAdapter.HourMinutesOutput(sortedTimes[0].hour.toInt())

        var routeId: String
        var hour: Int
        var headSign: String = ""
        for (i in sortedTimes.indices) {
            // examined StopTime attributes
            routeId = sortedTimes[i].trip?.route?.id!!
            hour = sortedTimes[i].hour.toInt()
            headSign = sortedTimes[i].trip?.headSign!!

            // Start a new hour group
            if (hour != currentHourMinutes.hour || routeId != currentRoute) {
                // store stuff
                hours.add(currentHourMinutes)

                currentHourMinutes = TimetableAdapter.HourMinutesOutput(hour)
            }

            // Start a new route group
            if (routeId != currentRoute) {
                // of course first store the current stuff
                output.put(currentRoute + StaticStrings.SEPARATOR + currentHeadSign, ArrayList(hours))

                hours.clear()
                currentRoute = routeId
                currentHeadSign = headSign
            }

            currentHourMinutes.minutes.add(sortedTimes[i].minute.toInt())
        }

        // Add the very last ones
        hours.add(currentHourMinutes)
        output.put(currentRoute + StaticStrings.SEPARATOR + headSign, ArrayList(hours))

        return output
    }

    override fun setToolbarTitle() {
        // also set title
        if (filterRouteID != null && filterStopID == null) {
            view.setActionbarTitle(filterRouteID)
        } else if (filterStopID != null && filterRouteID == null) {
            view.setActionbarTitle(filterStopName ?: "")
        } else {
            view.setActionbarTitle("$filterStopName-$filterRouteID")
        }
    }

    override fun updateNextUp() {
        if (filterStopID == null) return

        val currTime = DateTime.now()

        // Get all times in this stop on this route, today, in both directions
        // After the currTime hour, sorted by time
        val times = getRealmQueryFor(filterStopID, filterRouteID, TodayType.todayType, filterDirection)
                .filter {
                    (it.hour.toInt() == currTime.hourOfDay && it.minute >= currTime.minuteOfHour) ||
                     it.hour.toInt() > currTime.hourOfDay
                }.sortedWith(compareBy({ it.hour }, { it.minute }))

        // only show the first ten or if there are less, only that amount
        view.showNextUp(times.subList(0, Math.min(times.size, 10)))
    }

    /**
     * Return a query with all of the StopTimes in [stop] on [route], on [dayType], in [direction]
     * If direction is null then both directions will be returned
     */
    private fun getRealmQueryFor(stop: String, route: String?,
                         @IntRange(from = 0, to = 4) dayType: Int,
                         @IntRange(from = 0, to = 4) direction: Int? = null): List<StopTime> {
        val realm = Realm.getDefaultInstance()
        val times = realm.where<StopTime>(StopTime::class.java).beginGroup()

        // Look for stopTimes with the given stop
        times.equalTo(StopTime.CN_STOP + "." + Stop.CN_ID, stop)

        // Look for stopTimes with the given route, direction and dayType
        // This can be achieved with the proper trip id because that is made up of all of
        // that information
        if (direction != null && direction != -1) times.equalTo(StopTime.CN_TRIP + "." + Trip.CN_DIRECTION, direction)
        times.equalTo(StopTime.CN_TRIP + "." + Trip.CN_DAY_TYPE, dayType)
        if (route != null) times.equalTo(StopTime.CN_TRIP + "." + Trip.CN_ROUTE + "." + Route.CN_ID, route)
        times.endGroup()

        return times.findAll()
    }
}