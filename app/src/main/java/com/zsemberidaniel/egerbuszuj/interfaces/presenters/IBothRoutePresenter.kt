package com.zsemberidaniel.egerbuszuj.interfaces.presenters

/**
 * Created by zsemberi.daniel on 2017. 05. 31..
 */
interface IBothRoutePresenter : IBasicPresenter {
    fun directionChanged(newDirection: Int)
    fun routeChanged(newRoute: String)
    fun flipDirection()
}