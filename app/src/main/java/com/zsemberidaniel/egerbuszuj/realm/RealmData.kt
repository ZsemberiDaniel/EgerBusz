package com.zsemberidaniel.egerbuszuj.realm

import com.zsemberidaniel.egerbuszuj.realm.objects.Route
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop
import com.zsemberidaniel.egerbuszuj.realm.objects.StopTime
import com.zsemberidaniel.egerbuszuj.realm.objects.Trip
import io.realm.Realm

/**
 * Created by zsemberi.daniel on 2017. 05. 27..
 */
class RealmData {
    companion object {
        fun getAllRoutes(): List<Route> =
                Realm.getDefaultInstance().where(Route::class.java).distinct(Route.CN_ID).toList()

        fun getAllStops(): List<Stop> =
                Realm.getDefaultInstance().where(Stop::class.java).distinct(Stop.CN_ID)

        fun getAllStopsInOrder(route: String, direction: Int): List<Stop?> =
                Realm.getDefaultInstance().where(StopTime::class.java)
                        .equalTo("${StopTime.CN_TRIP}.${Trip.CN_ID}", "${route}0$direction")
                        .findAllSorted(StopTime.CN_SEQUENCE)
                        .map { it.stop }
                        .distinctBy { it?.id }

        fun getHeadSignOf(route: String, direction: Int): String =
                Realm.getDefaultInstance().where(Trip::class.java)
                        .equalTo(Trip.CN_ID, "${route}0$direction")
                        .findFirst()
                        .headSign!!
    }
}