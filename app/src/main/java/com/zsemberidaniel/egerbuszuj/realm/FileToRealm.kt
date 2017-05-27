package com.zsemberidaniel.egerbuszuj.realm

import android.content.Context
import android.os.AsyncTask
import android.util.Log

import com.zsemberidaniel.egerbuszuj.misc.StaticStrings
import com.zsemberidaniel.egerbuszuj.realm.objects.Route
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop
import com.zsemberidaniel.egerbuszuj.realm.objects.StopTime
import com.zsemberidaniel.egerbuszuj.realm.objects.Trip

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList

import io.realm.Realm

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

class FileToRealm {
    companion object {

        val PREF_IS_FILLED = "isRealmFilled"

        private val STOPS_FILE_PATH = "stops.txt"
        private val ROUTE_FILE_PATH = "routes.txt"

        fun init(context: Context) {
            // if we have already filled the realm don't fill it again
            val preferences = context.getSharedPreferences(StaticStrings.PREFS, 0)
            // preferences.edit().putBoolean(PREF_IS_FILLED, false).apply()
            if (!preferences.getBoolean(PREF_IS_FILLED, false)) {
                Realm.getDefaultInstance().executeTransaction { realm -> realm.deleteAll() }
                Realm.getDefaultInstance().executeTransaction { realm -> fillRealm(realm, context) }

                // indicate that we filled the realm
                preferences.edit().putBoolean(PREF_IS_FILLED, true).apply()
            }
        }

        private fun fillRealm(realm: Realm, context: Context) {
            // Populate tables from files
            var reader: BufferedReader
            // First populate the stops table
            try {
                reader = BufferedReader(InputStreamReader(context.assets.open(STOPS_FILE_PATH)))

                BufferedReader(reader).use { reader ->
                    reader.lineSequence().forEach {
                        // A line looks like: IDI Name of stop
                        val stop = realm.createObject<Stop>(Stop::class.java)
                        stop.id = it.substring(0, 3)
                        stop.name = it.substring(4)
                        stop.isStarred = false
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
                // TODO couldn't open the stops file so download it or something
            }

            // Next populate the routes table
            // Fill this list as well so we can then read from the listItem.txt file as well to populate the other tables
            val routes = ArrayList<String>()
            try {
                reader = BufferedReader(InputStreamReader(context.assets.open(ROUTE_FILE_PATH)))

                // A line looks like: Name
                BufferedReader(reader).use { reader ->
                    reader.lineSequence().forEach {
                        val route = realm.createObject<Route>(Route::class.java)
                        route.id = it

                        routes.add(it)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                // TODO couldn't open the routes file so download it or something
            }

            // Next populate both the trip and stop_time tabled
            // Go through all the routes because we can get the filenames that way
            for (route in routes) {
                try {
                    reader = BufferedReader(InputStreamReader(context.assets.open(route + ".txt")))
                    Log.i("IO/I", "Reading from $route.txt")

                    BufferedReader(reader).use { reader ->
                        for (direction in 0..1) {
                            val headSign = reader.readLine()
                            // If head sign is - then there is no route here
                            if (headSign == "-") continue

                            // For each dayType
                            for (j in 0..3) {
                                val trip = realm.createObject<Trip>(Trip::class.java)
                                trip.id = route + j + direction
                                trip.direction = direction
                                trip.dayType = j
                                trip.headSign = headSign
                                trip.route = realm.where<Route>(Route::class.java).equalTo(Route.CN_ID, route).findFirst()
                            }

                            // Read and store the data first so we can make the tables with the times
                            val stopCount = Integer.valueOf(reader.readLine())!!
                            val types = Array(2) { IntArray(stopCount) }
                            val stops = arrayOfNulls<String>(stopCount)
                            for (i in 0..stopCount - 1) stops[i] = reader.readLine()
                            for (i in 0..stopCount - 1) types[0][i] = Integer.valueOf(reader.readLine())!!
                            for (i in 0..stopCount - 1) types[1][i] = Integer.valueOf(reader.readLine())!!

                            // Read the times
                            var line: String? = reader.readLine()
                            var lineWords: Array<String>
                            var lineCounter = 0
                            var hour: Byte = 0
                            var type: Byte = 0


                            while (line != "" && line != null) {
                                // no buses in this category so don't do anything
                                if (line != "-") {
                                    // The very first line of an hour: hour:type
                                    if (lineCounter % 5 == 0) {
                                        lineWords = line.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                                        hour = java.lang.Byte.valueOf(lineWords[0])!!
                                        type = (java.lang.Byte.valueOf(lineWords[1])!! - 1).toByte()
                                    } else { // The other lines: min,min,min,min (...)
                                        lineWords = line.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

                                        // Go through all stops and add them to the stop_time table with the correct time
                                        for (i in stops.indices) {
                                            // Also go through all the minutes
                                            for (k in lineWords.indices) {
                                                val stopTime = realm.createObject<StopTime>(StopTime::class.java)
                                                stopTime.stop = realm.where<Stop>(Stop::class.java).equalTo(Stop.CN_ID, stops[i]).findFirst()
                                                stopTime.trip = realm.where<Trip>(Trip::class.java)
                                                        .equalTo(Trip.CN_ID, route + (lineCounter % 5 - 1) + direction).findFirst()
                                                stopTime.stopSequence = types[type.toInt()][i]

                                                // calculate the correct minutes
                                                val minutes = Integer.valueOf(lineWords[k])!! + types[type.toInt()][i]
                                                // if the minute goes above 60 we need to add Math.floor(minutes / 60d) to hour
                                                stopTime.hour = (Math.floor(minutes / 60.0) + hour).toByte()
                                                // if minute goes above 60 we need to trim it down to [0;60[
                                                stopTime.minute = (minutes % 60).toByte()
                                            }
                                        }
                                    }
                                }

                                lineCounter++
                                line = reader.readLine()
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    // TODO couldn't open the stops file so download it or something
                }
            }
        }
    }
}
