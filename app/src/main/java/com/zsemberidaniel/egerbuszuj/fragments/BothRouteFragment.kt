package com.zsemberidaniel.egerbuszuj.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.*
import com.zsemberidaniel.egerbuszuj.R
import com.zsemberidaniel.egerbuszuj.activities.TimetableActivity
import com.zsemberidaniel.egerbuszuj.adapters.RouteAdapter
import com.zsemberidaniel.egerbuszuj.realm.RealmData
import eu.davidea.flexibleadapter.FlexibleAdapter
import android.view.animation.LayoutAnimationController




/**
 * Created by zsemberi.daniel on 2017. 05. 27..
 */
class BothRouteFragment : Fragment() {

    private lateinit var routeSpinner: Spinner
    private lateinit var headSignTextView: TextView
    private lateinit var headSignSwapButton: Button

    private lateinit var routeRecyclerView: RecyclerView
    private lateinit var routeAdapter: RouteAdapter

    private var direction: Int = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_both_route, container, false)

        headSignTextView = view.findViewById(R.id.headsignTextView) as TextView
        headSignSwapButton = view.findViewById(R.id.swapHeadSignButton) as Button
        routeSpinner = view.findViewById(R.id.chooseRouteSpinner) as Spinner
        routeRecyclerView = view.findViewById(R.id.routeRecyclerView) as RecyclerView

        // spinner
        val spinnerAdapter = ArrayAdapter<String>(view.context, R.layout.support_simple_spinner_dropdown_item,
                RealmData.getAllRoutes().map { it.id }.sortedBy { it })
        routeSpinner.adapter = spinnerAdapter

        routeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) { }

            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                reflectRouteOrDirectionChangeOnGUI(getChosenRoute(), direction)
            }

        }

        // setup recycler view
        routeRecyclerView.setHasFixedSize(true)
        routeRecyclerView.layoutManager = LinearLayoutManager(context)

        // add swap button click listener
        headSignSwapButton.setOnClickListener {
            direction = 1 - direction
            reflectRouteOrDirectionChangeOnGUI(getChosenRoute(), direction)

            val anim = AnimationUtils.loadAnimation(context, R.anim.image_view_rotation_180)
            // val animController = LayoutAnimationController(anim, 0f)

            headSignSwapButton.startAnimation(anim)
        }

        return view
    }

    fun getChosenRoute() = routeSpinner.selectedItem.toString()

    /**
     * Updates the recyclerView and the head part.
     * Also adds new clickListeners
     */
    fun reflectRouteOrDirectionChangeOnGUI(route: String, direction: Int) {
        val items = RealmData.getAllStopsInOrder(route, direction).map { RouteAdapter.RouteAdapterItem(it!!) }.toMutableList()

        headSignTextView.text = RealmData.getHeadSignOf(route, direction)

        if (routeRecyclerView.adapter == null) {
            routeAdapter = RouteAdapter(items)
            routeRecyclerView.adapter = routeAdapter
        } else {
            routeAdapter.clear()
            routeAdapter.addItems(0, items)
        }

        routeAdapter.mItemClickListener = FlexibleAdapter.OnItemClickListener { position ->
            val item = routeAdapter.getItem(position)

            val intent = Intent(context, TimetableActivity::class.java)
            intent.putExtra(TimetableActivity.ARG_STOP_ID, item.stop.id)
            intent.putExtra(TimetableActivity.ARG_ROUTE_ID, route)
            intent.putExtra(TimetableActivity.ARG_DIRECTION, 0)
            context.startActivity(intent)

            true
        }
    }
}