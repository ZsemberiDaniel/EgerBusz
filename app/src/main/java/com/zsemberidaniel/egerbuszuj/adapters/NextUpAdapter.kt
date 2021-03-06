package com.zsemberidaniel.egerbuszuj.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.zsemberidaniel.egerbuszuj.R
import com.zsemberidaniel.egerbuszuj.misc.formatToTwoDigits

import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder

import org.joda.time.DateTime
import org.joda.time.LocalTime
import org.joda.time.Period

/**
 * Created by zsemberi.daniel on 2017. 05. 27..
 */
class NextUpAdapter(items: MutableList<NextUpItem>?) : FlexibleAdapter<NextUpAdapter.NextUpItem>(items) {

    class NextUpItem(val context: Context, val route: String, val headSign: String, val time: LocalTime) : AbstractFlexibleItem<NextUpItem.NextUpItemViewHolder>() {

        val fullHeadSign = "$route - $headSign"

        override fun hashCode(): Int = fullHeadSign.hashCode()
        override fun equals(other: Any?): Boolean =
                if (other is NextUpItem) fullHeadSign == other.fullHeadSign
                else false

        override fun getLayoutRes(): Int = R.layout.timetable_nextup_card
        override fun createViewHolder(adapter: FlexibleAdapter<out IFlexible<*>>, inflater: LayoutInflater,
                                      parent: ViewGroup): NextUpItemViewHolder =
            NextUpItemViewHolder(inflater.inflate(layoutRes, parent, false), adapter)


        @SuppressLint("SetTextI18n")
        override fun bindViewHolder(adapter: FlexibleAdapter<out IFlexible<*>>, holder: NextUpItemViewHolder,
                                    position: Int, payloads: MutableList<Any?>) {
            holder.headSignTextView.text = fullHeadSign

            val now = Period(DateTime.now(), time.toDateTimeToday())

            // How long the user would have to wait for the bus
            val timeFromNow =
            if (now.hours == 0 && now.minutes + 1 == 0) context.resources.getString(R.string.now)
            else if (now.hours == 0) "${now.minutes + 1}${context.resources.getString(R.string.minuteShort)}"
            else "${now.hours}${context.resources.getString(R.string.hourShort)}${now.minutes + 1}${context.resources.getString(R.string.minuteShort)}"

            holder.timeTextView.text = "${time.hourOfDay.formatToTwoDigits()}:${time.minuteOfHour.formatToTwoDigits()} - $timeFromNow"
        }

        class NextUpItemViewHolder(view: View, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(view, adapter) {
            val headSignTextView: TextView = view.findViewById(R.id.headsignTextView) as TextView
            val timeTextView: TextView = view.findViewById(R.id.timeTextView) as TextView
        }

    }

}