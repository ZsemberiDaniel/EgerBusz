package com.zsemberidaniel.egerbuszuj.realm.objects

import io.realm.RealmObject

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

open class Trip : RealmObject() {

    var id: String? = null
    var route: Route? = null
    var headSign: String? = null
    var direction: Int = 0
    var dayType: Int = 0

    companion object {

        val CN_ID = "id"
        val CN_ROUTE = "route"
        val CN_DAY_TYPE = "dayType"
        val CN_HEAD_SIGN = "headSign"
        val CN_DIRECTION = "direction"
    }
}
