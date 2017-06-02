package com.zsemberidaniel.egerbuszuj.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.*
import com.zsemberidaniel.egerbuszuj.R
import com.zsemberidaniel.egerbuszuj.activities.TimetableActivity
import com.zsemberidaniel.egerbuszuj.adapters.RouteAdapter
import com.zsemberidaniel.egerbuszuj.interfaces.views.IBothRouteView
import com.zsemberidaniel.egerbuszuj.presenters.BothRoutePresenter
import com.zsemberidaniel.egerbuszuj.realm.RealmData
import eu.davidea.flexibleadapter.FlexibleAdapter

/**
 * Created by zsemberi.daniel on 2017. 05. 27..
 */
class BothRouteFragment : Fragment(), IBothRouteView {

    private lateinit var routeSpinner: Spinner
    private lateinit var headSignTextView: TextView
    private lateinit var headSignSwapButton: Button

    private lateinit var routeRecyclerView: RecyclerView
    private lateinit var routeAdapter: RouteAdapter

    private lateinit var presenter: BothRoutePresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_both_route, container, false)

        headSignTextView = view.findViewById(R.id.headsignTextView) as TextView
        headSignSwapButton = view.findViewById(R.id.swapHeadSignButton) as Button
        routeSpinner = view.findViewById(R.id.chooseRouteSpinner) as Spinner
        routeRecyclerView = view.findViewById(R.id.routeRecyclerView) as RecyclerView

        presenter = BothRoutePresenter(this)
        presenter.init()

        // spinner
        val spinnerAdapter = ArrayAdapter<String>(view.context, R.layout.support_simple_spinner_dropdown_item,
                RealmData.getAllRouteIdsSorted())
        routeSpinner.adapter = spinnerAdapter

        routeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>) { }

            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                presenter.routeChanged(routeSpinner.selectedItem.toString())
            }

        }

        // setup recycler view
        routeRecyclerView.setHasFixedSize(true)
        routeRecyclerView.layoutManager = LinearLayoutManager(context)

        // add swap button click listener
        headSignSwapButton.setOnClickListener {
            presenter.flipDirection()

            val from = headSignSwapButton.rotation % 360
            val till = from + 180f
            Log.i("TERT", "$from -> $till")
            val anim = RotateAnimation(from, till, headSignSwapButton.pivotX, headSignSwapButton.pivotY)
            anim.duration = 300
            anim.fillAfter = true
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) { }

                override fun onAnimationEnd(animation: Animation?) {
                    headSignSwapButton.rotation = till
                    Log.i("TERT", "Anim ended rot is ${headSignSwapButton.rotation}")
                }
                override fun onAnimationStart(animation: Animation?) {
                    headSignSwapButton.rotation = from
                    Log.i("TERT", "Anim started rot is ${headSignSwapButton.rotation}")
                }

            })
            // val animController = LayoutAnimationController(anim, 0f)

            headSignSwapButton.startAnimation(anim)
        }

        return view
    }

    fun getChosenRoute() = presenter.route

    /**
     * Updates the recyclerView and the head part.
     * Also adds new clickListeners
     */
    override fun updateAdapterItems(items: MutableList<RouteAdapter.RouteAdapterItem>, headSignText: String) {
        headSignTextView.text = headSignText

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
            intent.putExtra(TimetableActivity.ARG_ROUTE_ID, getChosenRoute())
            intent.putExtra(TimetableActivity.ARG_DIRECTION, 0)
            context.startActivity(intent)

            true
        }
    }
}