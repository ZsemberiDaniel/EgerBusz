package com.zsemberidaniel.egerbuszuj.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.zsemberidaniel.egerbuszuj.R
import com.zsemberidaniel.egerbuszuj.adapters.ChooseStopAdapter
import com.zsemberidaniel.egerbuszuj.interfaces.views.IChooseStopView
import com.zsemberidaniel.egerbuszuj.presenters.ChooseStopPresenter

import eu.davidea.flexibleadapter.FlexibleAdapter
import java.util.*

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

class ChooseStopFragment : Fragment(), IChooseStopView {

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var allStopsAdapter: ChooseStopAdapter

    private lateinit var presenter: ChooseStopPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.choose_stop, container, false)

        recyclerView = view.findViewById(R.id.chooseStopRecyclerView) as RecyclerView

        // improve performance because the content does not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true)

        // use a linear layout manager
        layoutManager = LinearLayoutManager(view.context)
        recyclerView.layoutManager = layoutManager

        allStopsAdapter = ChooseStopAdapter(ArrayList<ChooseStopAdapter.ChooseStopItem>())
        allStopsAdapter.setDisplayHeadersAtStartUp(true)
        allStopsAdapter.setStickyHeaders(true)

        presenter = ChooseStopPresenter(this, allStopsAdapter)
        presenter.init()

        allStopsAdapter.addListener(FlexibleAdapter.OnItemClickListener { position ->
            presenter.stopClicked(context, allStopsAdapter.getItem(position))
            true
        })

        recyclerView.adapter = allStopsAdapter

        return view
    }

    override fun onResume() {
        super.onResume()
        allStopsAdapter.clear()
        allStopsAdapter.addItems(0, presenter.getAdapterItemsCopy())
    }

    override fun updateStopFilter(newFilter: String) {
        allStopsAdapter.searchText = newFilter

        // we need this because otherwise the items just disappear if the filter is "" for some reason
        if (newFilter == "") {
            allStopsAdapter.clear()
            allStopsAdapter.addItems(0, presenter.getAdapterItemsCopy())
            allStopsAdapter.showAllHeaders()
            return
        }
        allStopsAdapter.filterItems(presenter.getAdapterItemsCopy())

        // Disable headers if the user is searching because they are not really needed
        // And if enabled they get doubled for some reason
        if (newFilter != "")
            allStopsAdapter.hideAllHeaders()
    }
}
