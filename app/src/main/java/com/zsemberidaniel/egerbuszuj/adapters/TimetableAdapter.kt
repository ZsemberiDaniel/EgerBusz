package com.zsemberidaniel.egerbuszuj.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.IntRange
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import com.zsemberidaniel.egerbuszuj.R
import com.zsemberidaniel.egerbuszuj.misc.formatToTwoDigits
import com.zsemberidaniel.egerbuszuj.realm.objects.Route
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
import io.realm.RealmQuery
import io.realm.Sort
import org.joda.time.DateTime

/**
 * Created by zsemberi.daniel on 2017. 05. 14..
 */

class TimetableAdapter(items: List<TimetableAdapter.TimetableHeader>?) : FlexibleAdapter<TimetableAdapter.TimetableHeader>(items) {

    data class HourMinutesOutput
    /**
     * @param minutes Does NOT copy it
     */
    @JvmOverloads constructor(var hour: Int, var minutes: MutableList<Int> = ArrayList<Int>())

    class TimetableItem(private val context: Context, header: TimetableHeader, private val hourMinutes: HourMinutesOutput)
        : AbstractSectionableItem<TimetableItem.TimetableItemViewHolder, TimetableHeader>(header) {

        private val hashString: String = hourMinutes.hour.toString() + header.fullHeadSign
        private var viewHolder: TimetableItemViewHolder? = null

        /**
         * Updates all the textViews based on the currTime
         */
        fun updateTextColor(currTime: DateTime) {
            val normalColor = ContextCompat.getColor(context, R.color.timetableNormalText)
            val fadedColor = ContextCompat.getColor(context, R.color.timetableFadedText)

            // This hour has been passed in currTime so set everything to faded
            if (hourMinutes.hour < currTime.hourOfDay) {
                viewHolder?.hourTextView?.setTextColor(fadedColor)

                if (viewHolder?.minuteTextViews != null)
                    for (view in viewHolder?.minuteTextViews!!)
                        view.setTextColor(fadedColor)

                return
            }

            // Current hour -> we need to check every minute whether it has passed
            else if (hourMinutes.hour == currTime.hourOfDay) {
                for (i in 0..hourMinutes.minutes.size - 1) {
                    if (hourMinutes.minutes[i] <= currTime.minuteOfHour) {
                        viewHolder?.minuteTextViews?.get(i)?.setTextColor(fadedColor)

                        // We are at the last minute and it has been converted to faded -> the hour can be converted
                        // because there are no more minutes in it
                        if (i == hourMinutes.minutes.size - 1) viewHolder?.hourTextView?.setTextColor(fadedColor)
                    } else { // set all the others to normal color
                        viewHolder?.minuteTextViews?.get(i)?.setTextColor(normalColor)
                    }
                }
            }

            // if this hour has not passed set it's color to normal
            else if (hourMinutes.hour > currTime.hourOfDay) {
                viewHolder?.hourTextView?.setTextColor(normalColor)
                for (i in 0..hourMinutes.minutes.size - 1)
                    viewHolder?.minuteTextViews?.get(i)?.setTextColor(normalColor)
            }
        }

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
            viewHolder = holder
            holder.minutesLinearLayout.removeAllViews()

            holder.hourTextView.text = hourMinutes.hour.formatToTwoDigits()
            holder.minuteTextViews = Array(hourMinutes.minutes.size, { i ->
                val textView = TextView(context)
                textView.text = "${hourMinutes.minutes[i].formatToTwoDigits()}${if (i != hourMinutes.minutes.size - 1) ", " else ""}"

                textView
            })

            holder.minuteTextViews?.forEach { minTextView -> holder.minutesLinearLayout.addView(minTextView) }

            updateTextColor(DateTime.now())
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
}
