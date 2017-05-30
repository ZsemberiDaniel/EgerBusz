package com.zsemberidaniel.egerbuszuj.interfaces.views

import com.zsemberidaniel.egerbuszuj.adapters.TimetableAdapter
import com.zsemberidaniel.egerbuszuj.realm.objects.StopTime

/**
 * Created by zsemberi.daniel on 2017. 05. 29..
 */
interface ITimetableView : IBasicView {
    fun showNextUp(nextUpItems: List<StopTime>)
    fun hideNextUp()

    fun expandAll()

    fun showTimes(vararg times: HashMap<String, List<TimetableAdapter.HourMinutesOutput>>?)

    fun setActionbarTitle(title: String)
}