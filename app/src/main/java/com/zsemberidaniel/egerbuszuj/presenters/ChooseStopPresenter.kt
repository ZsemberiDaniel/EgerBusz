package com.zsemberidaniel.egerbuszuj.presenters

import android.content.Context
import android.content.Intent
import com.zsemberidaniel.egerbuszuj.activities.TimetableActivity
import com.zsemberidaniel.egerbuszuj.adapters.ChooseStopAdapter
import com.zsemberidaniel.egerbuszuj.interfaces.presenters.IChooseStopPresenter
import com.zsemberidaniel.egerbuszuj.interfaces.views.IChooseStopView
import com.zsemberidaniel.egerbuszuj.realm.RealmData
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop
import java.util.HashMap
import java.util.HashSet
import java.util.TreeSet

/**
 * Created by zsemberi.daniel on 2017. 05. 31..
 */
class ChooseStopPresenter(val view: IChooseStopView, val adapter: ChooseStopAdapter) : IChooseStopPresenter {

    private lateinit var allStopItems: TreeSet<ChooseStopAdapter.ChooseStopItem>
    private lateinit var allStops: List<Stop>

    override fun getAdapterItemsCopy() = ArrayList(allStopItems.toMutableList())

    override fun init() {
        allStops = RealmData.getAllStops()
        allStopItems = convertToChooseStopItems(getNewHeaders())
    }

    override fun destroy() {

    }

    override fun stopClicked(context: Context, item: ChooseStopAdapter.ChooseStopItem) {
        val intent = Intent(context, TimetableActivity::class.java)
        intent.putExtra(TimetableActivity.ARG_STOP_ID, item.stopId)
        context.startActivity(intent)
    }

    /**
     * Generates a header HashMap for the given stops in presenter class. (Only includes the letter which the stops start
     * with). It is a method to be used with the convertToChooseStopItems.
     * @return A HashMap with uppercase Character keys and the headers.
     */
    override fun getNewHeaders(): HashMap<Char, ChooseStopAdapter.ChooseStopHeader> {
        val characters = HashSet<Char>()
        characters.add('*')
        val headers = HashMap<Char, ChooseStopAdapter.ChooseStopHeader>()

        allStops.mapTo(characters) { it.name?.toUpperCase()?.get(0)!! }

        for (letter in characters) headers.put(letter, ChooseStopAdapter.ChooseStopHeader(letter))

        return headers
    }

    /**
     * Converts the given items to the ChoseStopItem GUI class so it can be added to FlexibleAdapter.
     * It needs the letter headers which will be added to the FlexibleAdapter as well. They need to be
     * in a HashMap. The keys are the characters (uppercase) and the values are the headers themselves.
     * Keeps the order of the items.
     * @param headers The letter headers to be added
     * *
     * @return The GUI items
     */
    override fun convertToChooseStopItems(headers: HashMap<Char, ChooseStopAdapter.ChooseStopHeader>): TreeSet<ChooseStopAdapter.ChooseStopItem> {
        val items = TreeSet<ChooseStopAdapter.ChooseStopItem>()
        val starHeader = headers['*']

        allStops.mapTo(items) {
            ChooseStopAdapter.ChooseStopItem(headers[it.name?.toUpperCase()?.get(0)]!!,
                    starHeader!!, it.id!!, it.name!!, it.isStarred)
        }

        return items
    }
}