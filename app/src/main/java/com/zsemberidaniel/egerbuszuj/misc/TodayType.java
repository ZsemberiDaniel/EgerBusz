package com.zsemberidaniel.egerbuszuj.misc;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by zsemberi.daniel on 2017. 05. 08..
 */

public class TodayType extends AsyncTask<Context, Void, Void> {

    /**
     * The file:
     * year
     * // After this do the special munkaszuneti napok come
     * month day
     * ...
     *
     * // After this do the tanszuneti napok come
     * month day
     * ...
     *
     * // After this do the special days on which we have to work come
     * month day
     * ...
     *
     * // After this do the summer break come
     * month day // from
     * month day // till
     */
    private static final String CALENDAR_FILE_PATH = "calendar.txt";

    public static int ISKOLAI_MUNKANAP = 0;
    public static int TANSZUNETI_MUNKANAP = 1;
    public static int SZABADNAP = 2;
    public static int MUNKASZUNETI_NAP = 3;

    private static ArrayList<DateTime> munkaszunetiNapok;
    private static ArrayList<DateTime> tanszunetiNapok;
    private static ArrayList<DateTime> specialisMunkanap;
    private static DateTime[] summerBreak;

    @Override
    protected Void doInBackground(Context... params) {
        init(params[0]);

        return null;
    }

    /**
     * Initializes the special days. Needs to be called at the start of the app every time.
     * @param context The current context
     */
    private static void init(Context context) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(CALENDAR_FILE_PATH)));

            int year = Integer.valueOf(reader.readLine());
            // Casting the value to int because Android Studio 2.3 gives error that year should be Calendar.Sunday or something...
            if (year != (int) Calendar.getInstance().get(Calendar.YEAR)) {
                // TODO not current year -> fetch from server
            }

            munkaszunetiNapok = new ArrayList<>();
            tanszunetiNapok = new ArrayList<>();
            specialisMunkanap = new ArrayList<>();
            summerBreak = new DateTime[2];

            String line;
            String[] words;
            while (!(line = reader.readLine()).equals("")) {
                words = line.split(" ");
                munkaszunetiNapok.add(new DateTime(year, Integer.valueOf(words[0]), Integer.valueOf(words[1]), 0, 0));
            }

            while (!(line = reader.readLine()).equals("")) {
                words = line.split(" ");
                tanszunetiNapok.add(new DateTime(year, Integer.valueOf(words[0]), Integer.valueOf(words[1]), 0, 0));
            }

            while (!(line = reader.readLine()).equals("")) {
                words = line.split(" ");
                specialisMunkanap.add(new DateTime(year, Integer.valueOf(words[0]), Integer.valueOf(words[1]), 0, 0));
            }

            for (int i = 0; i < summerBreak.length; i++) {
                words = reader.readLine().split(" ");
                summerBreak[i] = new DateTime(year, Integer.valueOf(words[0]), Integer.valueOf(words[1]), 0, 0);
            }
        } catch (IOException exception) {
            // TODO file not found fetch it from the server maybe
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Based on today's date return what type of day it is
     * @return It's based on the ints in this class (ISKOLAI_MUNKANAP,  TANSZUNETI_MUNKANAP ...)
     */
    public static int getTodayType() {
        DateTime today = DateTime.now();

        return getDayType(today);
    }

    /**
     * The day type of the given date
     * @return It's based on the ints in this class (ISKOLAI_MUNKANAP,  TANSZUNETI_MUNKANAP ...)
     */
    public static int getDayType(DateTime date) {
        // first let's go through the days on which we work but we normally wouldn't have to
        for (DateTime workingDay : specialisMunkanap)
            if (workingDay.withTimeAtStartOfDay().equals(date.withTimeAtStartOfDay()))
                return ISKOLAI_MUNKANAP;

        // Then we need to get rid of all the munkaszuneti nap-s and the szabadnap-s becaus
        // munkaszuneti nap-s and szabadnap-s override tanszuneti nap-s

        // This comes first because if something is a tanszuneti nap it doesn't mean that it's a munkaszunet
        for (DateTime munkaSzunetiNap : munkaszunetiNapok)
            if (munkaSzunetiNap.withTimeAtStartOfDay().equals(date.withTimeAtStartOfDay()))
                return MUNKASZUNETI_NAP;

        // The day is saturday -> SZABADNAP
        if (date.getDayOfWeek() == DateTimeConstants.SATURDAY) {
            return SZABADNAP;
        } else if (date.getDayOfWeek() == DateTimeConstants.SUNDAY) {
            return MUNKASZUNETI_NAP;
        }

        // At this point there are no munkaszuneti nap-s
        for (DateTime tanSzunetiNap : tanszunetiNapok)
            if (tanSzunetiNap.withTimeAtStartOfDay().equals(date.withTimeAtStartOfDay()))
                return TANSZUNETI_MUNKANAP;

        // Now comes the summer break
        if (date.isBefore(summerBreak[1]) && date.isAfter(summerBreak[0]))
            return TANSZUNETI_MUNKANAP;

        return ISKOLAI_MUNKANAP;
    }
}