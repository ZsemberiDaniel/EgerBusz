package com.zsemberidaniel.egerbuszuj.realm.objects

import io.realm.RealmObject

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

open class Stop : RealmObject() {

    var id: String? = null
    var name: String? = null
    var isStarred: Boolean = false

    companion object {

        val CN_ID = "id"
        val CN_NAME = "name"
        val CN_STARRED = "starred"
    }
}
