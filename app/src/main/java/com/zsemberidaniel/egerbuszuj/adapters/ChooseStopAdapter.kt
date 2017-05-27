package com.zsemberidaniel.egerbuszuj.adapters

import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.zsemberidaniel.egerbuszuj.R
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.HashSet
import java.util.TreeSet

import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractHeaderItem
import eu.davidea.flexibleadapter.items.AbstractSectionableItem
import eu.davidea.flexibleadapter.items.IFilterable
import eu.davidea.viewholders.FlexibleViewHolder
import io.realm.Realm

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

class ChooseStopAdapter(private val items: List<ChooseStopAdapter.ChooseStopItem>) : FlexibleAdapter<ChooseStopAdapter.ChooseStopItem>(ArrayList(items!!)) {

    private var filter: String = ""

    /**
     * Updates the starred attribute of the ChooseStopItems, sorts them, then updates the display as well
     * If it is filtered it will remove the filter and update the whole data set
     */
    private fun updateAndSortDataSet() {
        // remove filter because this function will take the whole list from the constructor into consideration
        if (isFiltered) {
            searchText = ""
        }

        // Update starred
        val realm = Realm.getDefaultInstance()
        for (i in items.indices)
            items[i].isStarred = realm.where<Stop>(Stop::class.java).equalTo(Stop.CN_ID, items[i].stopId)
                    .findFirst().isStarred
        realm.close()

        // sort
        Collections.sort(items)

        // update display
        setNotifyMoveOfFilteredItems(true)
        super.updateDataSet(ArrayList(items), true)
    }

    /**
     * @return Is the FlexibleAdapter filtered in any way?
     */
    val isFiltered: Boolean
        get() = filter != ""

    override fun setSearchText(searchText: String) {
        this.filter = searchText

        super.setSearchText(searchText)
    }

    override fun onPostFilter() {
        super.onPostFilter()

        // update and sort the data set in case the user starred a stop
        if (filter == "")
            updateAndSortDataSet()
    }

    class ChooseStopItem(private val letterHeader: ChooseStopHeader, private val starredHeader: ChooseStopHeader,
                         val stopId: String, val stopName: String, starred: Boolean) : AbstractSectionableItem<ChooseStopItem.ChooseStopItemViewHolder, ChooseStopHeader>(if (starred) starredHeader else letterHeader), IFilterable, Comparable<ChooseStopItem> {
        var isStarred: Boolean = false
            internal set

        init {
            this.isStarred = starred
        }

        override fun isSelectable(): Boolean {
            return true
        }

        override fun equals(`object`: Any?): Boolean {
            if (`object` is ChooseStopItem) {
                return `object`.stopId == stopId
            }

            return false
        }

        override fun hashCode(): Int {
            return stopId.hashCode()
        }

        override fun getLayoutRes(): Int {
            return R.layout.stop_list_item
        }

        override fun createViewHolder(adapter: FlexibleAdapter<*>, inflater: LayoutInflater?,
                                      parent: ViewGroup?): ChooseStopItemViewHolder {
            val viewHolder = ChooseStopItemViewHolder(inflater!!.inflate(layoutRes, parent, false), adapter)

            return viewHolder
        }

        override fun bindViewHolder(adapter: FlexibleAdapter<*>?, holder: ChooseStopItemViewHolder?, position: Int,
                                    payloads: List<*>?) {
            holder!!.stopNameTextView.text = stopName
            setStarredImageCorrectly(holder)

            // setup on click listener for the starred image
            holder.starredImageView.setOnClickListener {
                // Toggle starred in database
                Realm.getDefaultInstance().executeTransaction { realm ->
                    val stop = realm.where<Stop>(Stop::class.java).equalTo(Stop.CN_ID, stopId).findFirst()

                    stop.isStarred = !stop.isStarred
                }

                // update the starred boolean here
                isStarred = Realm.getDefaultInstance().where<Stop>(Stop::class.java).equalTo(Stop.CN_ID, stopId)
                        .findFirst().isStarred

                // update display
                setStarredImageCorrectly(holder)
                if (isStarred)
                    setHeader(starredHeader)
                else
                    setHeader(letterHeader)

                val chooseStopAdapter = adapter as ChooseStopAdapter?

                // only update and sort the data set if it is not filtered because if it is filtered
                // it updates it in a way which displays all of the data
                if (!chooseStopAdapter!!.isFiltered)
                    chooseStopAdapter.updateAndSortDataSet()
            }
        }

        override fun filter(constraint: String): Boolean {
            return stopName.toLowerCase().contains(constraint.toLowerCase())
        }

        /**
         * Sets the starred image of the given viewHolder correctly based on this class' starred
         * boolean. It gets the drawable from ResourceCompat
         * @param viewHolder
         */
        private fun setStarredImageCorrectly(viewHolder: ChooseStopItemViewHolder) {
            if (isStarred) {
                viewHolder.starredImageView!!.setImageDrawable(
                        ResourcesCompat.getDrawable(viewHolder.starredImageView!!.resources,
                                R.drawable.ic_star, null)
                )
            } else {
                viewHolder.starredImageView!!.setImageDrawable(
                        ResourcesCompat.getDrawable(viewHolder.starredImageView!!.resources,
                                R.drawable.ic_star_border, null)
                )
            }
        }

        override fun compareTo(o: ChooseStopItem): Int {
            if (o.isStarred && !isStarred) return 1
            if (isStarred && !o.isStarred) return -1

            return -o.stopName.compareTo(stopName)
        }

        class ChooseStopItemViewHolder(view: View, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(view, adapter, false) {
            var stopNameTextView: TextView = view.findViewById(R.id.stopNameText) as TextView
            var starredImageView: ImageView = view.findViewById(R.id.starredImgView) as ImageView
        }
    }

    class ChooseStopHeader(private val letter: Char) : AbstractHeaderItem<ChooseStopHeader.ChooseStopHeaderViewHolder>() {

        override fun equals(o: Any?): Boolean {
            if (o is ChooseStopHeader) {
                return o.letter == letter
            }

            return false
        }

        override fun hashCode(): Int {
            return letter.toString().hashCode()
        }

        override fun getLayoutRes(): Int {
            return R.layout.letter_item_header
        }

        override fun createViewHolder(adapter: FlexibleAdapter<*>, inflater: LayoutInflater, parent: ViewGroup): ChooseStopHeaderViewHolder {
            return ChooseStopHeaderViewHolder(inflater.inflate(layoutRes, parent, false), adapter)
        }

        override fun bindViewHolder(adapter: FlexibleAdapter<*>?, holder: ChooseStopHeaderViewHolder, position: Int, payloads: List<*>) {
            holder.letterTextView.text = letter.toString()
        }

        class ChooseStopHeaderViewHolder(view: View, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(view, adapter, true) {

            val letterTextView: TextView = view.findViewById(R.id.letterTextView) as TextView
        }
    }

    companion object {

        /**
         * Generates a header HashMap for the given stops. (Only includes the letter which the stops start
         * with). It is a method to be used with the convertToChooseStopItems.
         * @param stops The stops from which to get the starting characters.
         * *
         * @return A HashMap with uppercase Character keys and the headers.
         */
        fun getNewHeaders(stops: List<Stop>): HashMap<Char, ChooseStopHeader> {
            val characters = HashSet<Char>()
            characters.add('*')
            val headers = HashMap<Char, ChooseStopHeader>()

            for (stop in stops) characters.add(stop.name?.toUpperCase()?.get(0)!!)

            for (letter in characters) headers.put(letter, ChooseStopHeader(letter))

            return headers
        }

        /**
         * Converts the given items to the ChoseStopItem GUI class so it can be added to FlexibleAdapter.
         * It needs the letter headers which will be added to the FlexibleAdapter as well. They need to be
         * in a HashMap. The keys are the characters (uppercase) and the values are the headers themselves.
         * Keeps the order of the items.
         * @param stops The stops to be converted
         * *
         * @param headers The letter headers to be added
         * *
         * @return The GUI items
         */
        fun convertToChooseStopItems(stops: List<Stop>,
                                     headers: HashMap<Char, ChooseStopHeader>): TreeSet<ChooseStopItem> {
            val items = TreeSet<ChooseStopItem>()
            val starHeader = headers['*']

            stops.mapTo(items) {
                ChooseStopItem(headers[it.name?.toUpperCase()?.get(0)]!!,
                        starHeader!!, it.id!!, it.name!!, it.isStarred)
            }

            return items
        }
    }
}