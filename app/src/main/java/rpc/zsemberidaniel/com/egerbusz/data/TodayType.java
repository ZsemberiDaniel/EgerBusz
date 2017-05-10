package rpc.zsemberidaniel.com.egerbusz.data;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by zsemberi.daniel on 2017. 05. 08..
 */

public class TodayType {

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

    private static ArrayList<SimpleCalendar> munkaszunetiNapok;
    private static ArrayList<SimpleCalendar> tanszunetiNapok;
    private static ArrayList<SimpleCalendar> specialisMunkanap;
    private static SimpleCalendar[] summerBreak;

    /**
     * Initializes the special days. Needs to be called at the start of the app every time.
     * @param context The current context
     */
    public static void init(Context context) {
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
            summerBreak = new SimpleCalendar[2];

            String line;
            String[] words;
            while (!(line = reader.readLine()).equals("")) {
                words = line.split(" ");
                munkaszunetiNapok.add(new SimpleCalendar(Integer.valueOf(words[0]), Integer.valueOf(words[1])));
            }

            while (!(line = reader.readLine()).equals("")) {
                words = line.split(" ");
                tanszunetiNapok.add(new SimpleCalendar(Integer.valueOf(words[0]), Integer.valueOf(words[1])));
            }

            while (!(line = reader.readLine()).equals("")) {
                words = line.split(" ");
                specialisMunkanap.add(new SimpleCalendar(Integer.valueOf(words[0]), Integer.valueOf(words[1])));
            }

            for (int i = 0; i < summerBreak.length; i++) {
                words = reader.readLine().split(" ");
                summerBreak[i] = new SimpleCalendar(Integer.valueOf(words[0]), Integer.valueOf(words[1]));
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
        Calendar today = Calendar.getInstance();

        return getDayType(today);
    }

    /**
     * The day type of the given date
     * @return It's based on the ints in this class (ISKOLAI_MUNKANAP,  TANSZUNETI_MUNKANAP ...)
     */
    public static int getDayType(Calendar date) {
        // first let's go through the days on which we work but we normally wouldn't have to
        for (SimpleCalendar workingDay : specialisMunkanap)
            if (workingDay.equals(date))
                return ISKOLAI_MUNKANAP;

        // Then we need to get rid of all the munkaszuneti nap-s and the szabadnap-s becaus
        // munkaszuneti nap-s and szabadnap-s override tanszuneti nap-s

        // This comes first because if something is a tanszuneti nap it doesn't mean that it's a munkaszunet
        for (SimpleCalendar munkaSzunetiNap : munkaszunetiNapok)
            if (munkaSzunetiNap.equals(date))
                return MUNKASZUNETI_NAP;

        // The day is saturday -> SZABADNAP
        if (date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
            return SZABADNAP;
        } else if (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return MUNKASZUNETI_NAP;
        }

        // At this point there are no munkaszuneti nap-s
        for (SimpleCalendar tanSzunetiNap : tanszunetiNapok)
            if (tanSzunetiNap.equals(date))
                return TANSZUNETI_MUNKANAP;

        // Now comes the summer break
        if (summerBreak[1].isTheCalendarSmallerOrEqual(date) && !summerBreak[0].isTheCalendarSmaller(date))
            return TANSZUNETI_MUNKANAP;

        return ISKOLAI_MUNKANAP;
    }

    /**
     * Simple calendar for providing month and date storage
     */
    private static class SimpleCalendar {
        private int month;
        public int getMonth() { return month; }
        private int day;
        public int getDay() { return day; }

        public SimpleCalendar(int month, int day) {
            this.month = month;
            this.day = day;
        }

        public boolean isTheCalendarSmallerOrEqual(Calendar calendar) {
            int month = calendar.get(Calendar.MONTH);

            if (this.month == month) {
                return calendar.get(Calendar.DAY_OF_MONTH) <= day;
            } else {
                return month <= this.month;
            }
        }

        public boolean isTheCalendarSmaller(Calendar calendar) {
            int month = calendar.get(Calendar.MONTH);

            if (this.month == month) {
                return calendar.get(Calendar.DAY_OF_MONTH) < day;
            } else {
                return month < this.month;
            }
        }

        public boolean equals(Calendar calendar) {
            return (int) calendar.get(Calendar.MONTH) == (month - 1) && calendar.get(Calendar.DAY_OF_MONTH) == day;
        }
    }
}