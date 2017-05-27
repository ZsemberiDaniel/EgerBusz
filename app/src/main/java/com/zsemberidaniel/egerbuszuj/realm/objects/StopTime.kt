package com.zsemberidaniel.egerbuszuj.realm.objects

import io.realm.RealmObject

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

open class StopTime : RealmObject() {

    var trip: Trip? = null
    var stop: Stop? = null
    var hour: Byte = 0
    var minute: Byte = 0
    var stopSequence: Int = 0

    companion object {

        val CN_TRIP = "trip"
        val CN_STOP = "stop"
        val CN_HOUR = "hour"
        val CN_MINUTE = "minute"
        val CN_SEQUENCE = "stopSequence"
    }
}
