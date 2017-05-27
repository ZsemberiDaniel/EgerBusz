package com.zsemberidaniel.egerbuszuj.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.IntRange
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import com.zsemberidaniel.egerbuszuj.R
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop
import com.zsemberidaniel.egerbuszuj.realm.objects.StopTime
import com.zsemberidaniel.egerbuszuj.realm.objects.Trip

import java.util.ArrayList
import java.util.HashMap
import java.util.TreeSet

import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractExpandableHeaderItem
import eu.davidea.flexibleadapter.items.AbstractSectionableItem
import eu.davidea.viewholders.ExpandableViewHolder
import eu.davidea.viewholders.FlexibleViewHolder
import io.realm.Realm
import io.realm.Sort

/**
 * Created by zsemberi.daniel on 2017. 05. 14..
 */

class TimetableAdapter(items: List<TimetableAdapter.TimetableHeader>?) : FlexibleAdapter<TimetableAdapter.TimetableHeader>(items) {

    class HourMinutesOutput
    /**
     * @param minutes Does NOT copy it
     */
    @JvmOverloads constructor(var hour: Int, var minutes: MutableList<Int> = ArrayList<Int>())

    class TimetableItem(private val context: Context, header: TimetableHeader, private val hourMinutes: HourMinutesOutput) : AbstractSectionableItem<TimetableItem.TimetableItemViewHolder, TimetableHeader>(header) {

        private val hashString: String = hourMinutes.hour.toString() + header.fullHeadSign

        override fun equals(o: Any?): Boolean {
            if (o is TimetableItem) {

                return o.hashString == hashString
            }

            return false
        }

        override fun hashCode(): Int {
            return hashString.hashCode()
        }

        override fun getLayoutRes(): Int {
            return R.layout.timetable_data
        }

        override fun createViewHolder(adapter: FlexibleAdapter<*>, inflater: LayoutInflater,
                                      parent: ViewGroup): TimetableItemViewHolder {
            return TimetableItemViewHolder(inflater.inflate(layoutRes, parent, false), adapter)
        }

        @SuppressLint("SetTextI18n")
        override fun bindViewHolder(adapter: FlexibleAdapter<*>, holder: TimetableItemViewHolder,
                                    position: Int, payloads: List<*>) {
            holder.minutesLinearLayout.removeAllViews()

            holder.hourTextView.text = formatToTwoDigit(hourMinutes.hour)
            holder.minuteTextViews = Array(hourMinutes.minutes.size, { i ->
                val textView = TextView(context)
                textView.text = "${formatToTwoDigit(hourMinutes.minutes[i])}${if (i != hourMinutes.minutes.size - 1) ", " else ""}"

                textView
            })

            for (minTextView in holder.minuteTextViews!!)
                holder.minutesLinearLayout.addView(minTextView)
        }

        /**
         * Formats a number to two digits. For example 2 -> 02, 5 -> 05, 23 -> 23
         * If number is above two digits it will just be returned as a string
         */
        private fun formatToTwoDigit(t: Int): String {
            return if (t < 10) "0" + t else t.toString()
        }

        class TimetableItemViewHolder(view: View, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(view, adapter) {

            val hourTextView: TextView = view.findViewById(R.id.hourTextView) as TextView
            val minutesLinearLayout: LinearLayout = view.findViewById(R.id.minutesLinearLayout) as LinearLayout
            var minuteTextViews: Array<TextView>? = null

        }

    }

    class TimetableHeader(val route: String, val headSign: String, val fullHeadSign: String)
        : AbstractExpandableHeaderItem<TimetableHeader.TimetableHeaderViewHolder, TimetableItem>() {

        init {
            isExpanded = false
        }

        override fun equals(other: Any?): Boolean {
            if (other is TimetableHeader) {
                return other.fullHeadSign == fullHeadSign
            }

            return false
        }

        override fun hashCode(): Int {
            return fullHeadSign.hashCode()
        }

        override fun getLayoutRes(): Int {
            return R.layout.timetable_header
        }

        override fun createViewHolder(adapter: FlexibleAdapter<*>, inflater: LayoutInflater, parent: ViewGroup): TimetableHeaderViewHolder {
            return TimetableHeaderViewHolder(inflater.inflate(layoutRes, parent, false), adapter)
        }

        override fun bindViewHolder(adapter: FlexibleAdapter<*>, holder: TimetableHeaderViewHolder, position: Int, payloads: List<*>) {
            holder.routeTextView.text = route
            holder.headSignTextView.text = headSign
        }

        class TimetableHeaderViewHolder(view: View, adapter: FlexibleAdapter<*>) : ExpandableViewHolder(view, adapter, true) {
            var routeTextView: TextView = view.findViewById(R.id.routeTextView) as TextView
            var headSignTextView: TextView = view.findViewById(R.id.headsignTextView) as TextView
        }
    }

    companion object {

        /**
         * Returns a list with the items from the times HashMap. Also sets up the headers.
         * @param times An array of the times. The keys are route ids plus the head sign.
         * *              The value is a list of the hours and minutes. Because of the nature of the method
         * *              it will modify it so if you want the original back please !!!USE A COPY!!!
         * *
         * @return null if times has 0 elements. Otherwise a list of all the items with the correct headers.
         */
        fun setupItems(context: Context, vararg times: HashMap<String, List<HourMinutesOutput>>?): List<TimetableHeader>? {
            if (times.isEmpty()) return null

            val output = ArrayList<TimetableHeader>()

            // Add every time to the first member of the times array
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
            val sortedKeys = TreeSet<String>()
            while (keyIterator != null && keyIterator.hasNext())
                sortedKeys.add(keyIterator.next())

            val separator = context.resources.getString(R.string.headsignSeparator)
            // Then add the items in order
            if (times[0]?.keys?.size != null)
                for (i in 0..times[0]?.keys?.size!! - 1) {
                    val routeIDHeadSign = sortedKeys.pollFirst()
                    val hours = times[0]?.getValue(routeIDHeadSign)
                    val split = routeIDHeadSign.split(separator)

                    val header = TimetableHeader(split[0], split[1], routeIDHeadSign)
                    if (hours != null)
                        for (k in hours.indices) {
                            header.addSubItem(TimetableItem(context, header, hours[k]))
                        }
                    output.add(header)
                }

            return output
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
        fun getAllTimes(context: Context, stop: String, route: String?,
                        @IntRange(from = 0, to = 4) dayType: Int,
                        @IntRange(from = 0, to = 1) direction: Int): HashMap<String, List<HourMinutesOutput>>? {

            val realm = Realm.getDefaultInstance()
            val times = realm.where<StopTime>(StopTime::class.java)

            // Look for stopTimes with the given route, direction and dayType
            // This can be achieved with the proper trip id because that is made up of all of
            // that information
            if (route != null)
                times.equalTo(StopTime.CN_TRIP + "." + Trip.CN_ID, route + dayType + direction)
            else
            // otherwise we'll still need to apply the direction and the dayType
                times.like(StopTime.CN_TRIP + "." + Trip.CN_ID, "*" + dayType + direction)

            // Look for stopTimes with the given stop
            times.equalTo(StopTime.CN_STOP + "." + Stop.CN_ID, stop)
            realm.close()

            // Order like this because we want to group by routes then the time
            var sortedTimes = times.findAll()
            sortedTimes = sortedTimes.sort(arrayOf(StopTime.CN_TRIP + "." + Trip.CN_ID, StopTime.CN_HOUR, StopTime.CN_MINUTE),
                    arrayOf(Sort.ASCENDING, Sort.ASCENDING, Sort.ASCENDING))

            // if we have no StopTimes with the given attributes return null
            if (sortedTimes.size == 0) return null

            val output = HashMap<String, List<HourMinutesOutput>>()
            val hours = ArrayList<HourMinutesOutput>()

            // what are the current groups
            var currentRoute = sortedTimes[0].trip?.route?.id
            var currentHeadSign = sortedTimes[0].trip?.headSign
            var currentHourMinutes = HourMinutesOutput(sortedTimes[0].hour.toInt())

            var routeId: String
            var headSign = ""
            var hour: Int
            var min: Int
            var headsignSeparator: String = context.resources.getString(R.string.headsignSeparator)
            for (i in sortedTimes.indices) {
                // examined StopTime attributes
                routeId = sortedTimes[i].trip?.route?.id!!
                headSign = sortedTimes[i].trip?.headSign!!
                hour = sortedTimes[i].hour.toInt()
                min = sortedTimes[i].minute.toInt()

                // Start a new hour group
                if (hour != currentHourMinutes.hour || routeId != currentRoute) {
                    // store stuff
                    hours.add(currentHourMinutes)

                    currentHourMinutes = HourMinutesOutput(hour)
                }

                // Start a new route group
                if (routeId != currentRoute) {
                    // of course first store the current stuff
                    output.put(currentRoute + headsignSeparator + currentHeadSign, ArrayList(hours))

                    hours.clear()
                    currentRoute = routeId
                    currentHeadSign = headSign
                }

                currentHourMinutes.minutes.add(min)
            }

            // Add the very last ones
            hours.add(currentHourMinutes)
            output.put(currentRoute + headsignSeparator + headSign, ArrayList(hours))

            return output
        }
    }

}
