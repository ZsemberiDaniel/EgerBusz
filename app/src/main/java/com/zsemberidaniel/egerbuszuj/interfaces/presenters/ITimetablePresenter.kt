package com.zsemberidaniel.egerbuszuj.interfaces.presenters

import android.support.annotation.IntRange
import com.zsemberidaniel.egerbuszuj.adapters.TimetableAdapter
import java.util.HashMap

/**
 * Created by zsemberi.daniel on 2017. 05. 29..
 */
interface ITimetablePresenter : IBasicPresenter {
    fun loadTimes()
    fun getAllTimes(stop: String, route: String?,
                    @IntRange(from = 0, to = 4) dayType: Int,
                    @IntRange(from = 0, to = 1) direction: Int): HashMap<String, List<TimetableAdapter.HourMinutesOutput>>?
    fun setToolbarTitle()
    fun updateNextUp()
}