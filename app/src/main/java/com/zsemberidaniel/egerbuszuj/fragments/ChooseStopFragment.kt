package com.zsemberidaniel.egerbuszuj.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.zsemberidaniel.egerbuszuj.R
import com.zsemberidaniel.egerbuszuj.activities.TimetableActivity
import com.zsemberidaniel.egerbuszuj.adapters.ChooseStopAdapter
import com.zsemberidaniel.egerbuszuj.realm.RealmData
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop

import eu.davidea.flexibleadapter.FlexibleAdapter
import io.realm.Realm
import java.util.*

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

class ChooseStopFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var allStopsAdapter: FlexibleAdapter<ChooseStopAdapter.ChooseStopItem>
    private lateinit var allStopItems: TreeSet<ChooseStopAdapter.ChooseStopItem>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.choose_stop, container, false)

        recyclerView = view.findViewById(R.id.chooseStopRecyclerView) as RecyclerView

        // improve performance because the content does not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)

        // use a linear layout manager
        layoutManager = LinearLayoutManager(view.context)
        recyclerView.layoutManager = layoutManager

        // specify an adapter with all of the stops
        val allStops = RealmData.getAllStops()

        // Get letter headers
        val headers = ChooseStopAdapter.getNewHeaders(allStops)

        // Get the items in the adapter
        allStopItems = ChooseStopAdapter.convertToChooseStopItems(allStops, headers)

        allStopsAdapter = ChooseStopAdapter(ArrayList<ChooseStopAdapter.ChooseStopItem>(allStopItems))
        allStopsAdapter.setDisplayHeadersAtStartUp(true)
        allStopsAdapter.setStickyHeaders(true)

        allStopsAdapter.addListener(FlexibleAdapter.OnItemClickListener { position ->
            val item = allStopsAdapter.getItem(position)

            val intent = Intent(context, TimetableActivity::class.java)
            intent.putExtra(TimetableActivity.ARG_STOP_ID, item.stopId)
            context.startActivity(intent)
            true
        })

        recyclerView.adapter = allStopsAdapter

        return view
    }

    fun updateStopFilter(newFilter: String) {
        allStopsAdapter.searchText = newFilter

        // Disable headers if the user is searching because they are not really needed
        // And if enabled they get doubled for some reason
        if (newFilter != "")
            allStopsAdapter.hideAllHeaders()
        else
            allStopsAdapter.showAllHeaders()

        allStopsAdapter.filterItems(allStopItems.toList())
    }
}
