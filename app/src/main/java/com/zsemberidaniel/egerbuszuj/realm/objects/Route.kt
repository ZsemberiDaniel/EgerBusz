package com.zsemberidaniel.egerbuszuj.realm.objects

import io.realm.RealmObject

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

open class Route : RealmObject() {

    var id: String? = null

    companion object {

        val CN_ID = "id"

        fun getRouteComparator() =
                Comparator<String?> { o1, o2 ->
                    if (o1 == null && o2 == null) return@Comparator 0
                    else if (o1 == null) return@Comparator -1
                    else if (o2 == null) return@Comparator 1

                    val int1: Int = try { o1.toInt() } catch (e: NumberFormatException) { o1.substring(0, o1.length - 1).toInt() }
                    val int2: Int = try { o2.toInt() } catch (e: NumberFormatException) { o2.substring(0, o2.length - 1).toInt() }

                    if (int1 == int2) o1.length.compareTo(o2.length)
                    else int1 - int2
                }
    }
}
