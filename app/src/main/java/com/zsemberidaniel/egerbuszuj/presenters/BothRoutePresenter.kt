package com.zsemberidaniel.egerbuszuj.presenters

import com.zsemberidaniel.egerbuszuj.adapters.RouteAdapter
import com.zsemberidaniel.egerbuszuj.interfaces.presenters.IBothRoutePresenter
import com.zsemberidaniel.egerbuszuj.interfaces.views.IBothRouteView
import com.zsemberidaniel.egerbuszuj.realm.RealmData

/**
 * Created by zsemberi.daniel on 2017. 05. 31..
 */
class BothRoutePresenter(val view: IBothRouteView) : IBothRoutePresenter {

    var direction: Int = 1
        get
        private set
    var route: String? = null
        get
        private set

    override fun init() {

    }

    override fun destroy() {

    }

    /**
     * Flips direction then notifies itself about the change
     */
    override fun flipDirection() {
        directionChanged(1 - direction)
    }

    override fun directionChanged(newDirection: Int) {
        direction = newDirection

        dirOrRouteChanged()
    }

    override fun routeChanged(newRoute: String) {
        route = newRoute

        dirOrRouteChanged()
    }

    fun dirOrRouteChanged() {
        if (route == null) return

        val items = RealmData
                .getAllStopsInOrder(route!!, direction)
                .filterNotNull()
                .map { RouteAdapter.RouteAdapterItem(it) }
                .toMutableList()

        val headSignText = RealmData.getHeadSignOf(route!!, direction)

        view.updateAdapterItems(items, headSignText)
    }
}