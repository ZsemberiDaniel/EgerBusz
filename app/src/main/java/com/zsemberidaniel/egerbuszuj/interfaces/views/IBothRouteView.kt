package com.zsemberidaniel.egerbuszuj.interfaces.views

import com.zsemberidaniel.egerbuszuj.adapters.RouteAdapter

/**
 * Created by zsemberi.daniel on 2017. 05. 31..
 */
interface IBothRouteView {
    fun updateAdapterItems(items: MutableList<RouteAdapter.RouteAdapterItem>, headSignText: String)
}