package com.zsemberidaniel.egerbuszuj.realm.objects

import io.realm.RealmObject

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

open class Route : RealmObject() {

    var id: String? = null

    companion object {

        val CN_ID = "id"
    }
}
