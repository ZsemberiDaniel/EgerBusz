package com.zsemberidaniel.egerbuszuj.misc

import android.content.Context
import android.os.AsyncTask
import android.util.Log

import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.LocalDate

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.Calendar

/**
 * Created by zsemberi.daniel on 2017. 05. 08..
 */

class TodayType {

    companion object {

        /**
         * The file:
         * year
         * // After this do the special munkaszuneti napok come
         * month day
         * ...

         * // After this do the tanszuneti napok come
         * month day
         * ...

         * // After this do the special days on which we have to work come
         * month day
         * ...

         * // After this do the summer break come
         * month day // from
         * month day // till
         */
        private val CALENDAR_FILE_PATH = "calendar.txt"

        var ISKOLAI_MUNKANAP = 0
        var TANSZUNETI_MUNKANAP = 1
        var SZABADNAP = 2
        var MUNKASZUNETI_NAP = 3

        private lateinit var munkaszunetiNapok: ArrayList<DateTime>
        private lateinit var tanszunetiNapok: ArrayList<DateTime>
        private lateinit var specialisMunkanap: ArrayList<DateTime>
        private lateinit var summerBreak: Array<DateTime>

        /**
         * Initializes the special days. Needs to be called at the start of the app every time.
         * @param context The current context
         */
        fun init(context: Context) {
            var reader: BufferedReader
            try {
                reader = BufferedReader(InputStreamReader(context.assets.open(CALENDAR_FILE_PATH)))

                val year = Integer.valueOf(reader.readLine())!!
                // Casting the value to int because Android Studio 2.3 gives error that year should be Calendar.Sunday or something...
                if (year != Calendar.getInstance().get(Calendar.YEAR)) {
                    // TODO not current year -> fetch from server
                }

                munkaszunetiNapok = ArrayList<DateTime>()
                tanszunetiNapok = ArrayList<DateTime>()
                specialisMunkanap = ArrayList<DateTime>()
                summerBreak = Array(2, { DateTime() })

                var words: Array<String>
                var at: Int = 0
                val splitRegex = " ".toRegex()
                BufferedReader(reader).use { reader ->
                    reader.lineSequence().forEach {
                        if (it == "")
                            at++
                        else when (at) {
                            // munkaszuneti nap
                            0 -> {
                                words = it.split(splitRegex).dropLastWhile({ it.isEmpty() }).toTypedArray()
                                munkaszunetiNapok.add(DateTime(year, Integer.valueOf(words[0])!!, Integer.valueOf(words[1])!!, 0, 0))
                            }
                            // tanszuneti nao
                            1 -> {
                                words = it.split(splitRegex).dropLastWhile({ it.isEmpty() }).toTypedArray()
                                tanszunetiNapok.add(DateTime(year, Integer.valueOf(words[0])!!, Integer.valueOf(words[1])!!, 0, 0))
                            }
                            // specialis munkanap
                            2 -> {
                                words = it.split(splitRegex).dropLastWhile({ it.isEmpty() }).toTypedArray()
                                specialisMunkanap.add(DateTime(year, Integer.valueOf(words[0])!!, Integer.valueOf(words[1])!!, 0, 0))
                            }
                            // nyari szunet
                            3, 4 -> {
                                words = it.split(splitRegex).dropLastWhile({ it.isEmpty() }).toTypedArray()
                                summerBreak[at - 3] = DateTime(year, Integer.valueOf(words[0])!!, Integer.valueOf(words[1])!!, 0, 0)

                                at++
                            }
                        }
                    }
                }

            } catch (exception: IOException) {
                // TODO file not found fetch it from the server maybe
            }
        }

        /**
         * Based on today's date return what type of day it is
         * @return It's based on the ints in this class (ISKOLAI_MUNKANAP,  TANSZUNETI_MUNKANAP ...)
         */
        val todayType: Int
            get() {
                val today = DateTime.now()

                return getDayType(today)
            }

        /**
         * The day type of the given date
         * @return It's based on the ints in this class (ISKOLAI_MUNKANAP,  TANSZUNETI_MUNKANAP ...)
         */
        fun getDayType(date: DateTime): Int {
            // first let's go through the days on which we work but we normally wouldn't have to
            specialisMunkanap
                    .filter { it.withTimeAtStartOfDay() == date.withTimeAtStartOfDay() }
                    .forEach { return ISKOLAI_MUNKANAP }

            // Then we need to get rid of all the munkaszuneti nap-s and the szabadnap-s becaus
            // munkaszuneti nap-s and szabadnap-s override tanszuneti nap-s

            // This comes first because if something is a tanszuneti nap it doesn't mean that it's a munkaszunet
            munkaszunetiNapok
                    .filter { it.withTimeAtStartOfDay() == date.withTimeAtStartOfDay() }
                    .forEach { return MUNKASZUNETI_NAP }

            // The day is saturday -> SZABADNAP
            if (date.dayOfWeek == DateTimeConstants.SATURDAY) {
                return SZABADNAP
            } else if (date.dayOfWeek == DateTimeConstants.SUNDAY) {
                return MUNKASZUNETI_NAP
            }

            // At this point there are no munkaszuneti nap-s
            tanszunetiNapok
                    .filter { it.withTimeAtStartOfDay() == date.withTimeAtStartOfDay() }
                    .forEach { return TANSZUNETI_MUNKANAP }

            // Now comes the summer break
            if (date.isBefore(summerBreak[1]) && date.isAfter(summerBreak[0]))
                return TANSZUNETI_MUNKANAP

            return ISKOLAI_MUNKANAP
        }
    }
}