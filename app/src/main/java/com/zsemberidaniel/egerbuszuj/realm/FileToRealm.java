package com.zsemberidaniel.egerbuszuj.realm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.zsemberidaniel.egerbuszuj.StaticStrings;
import com.zsemberidaniel.egerbuszuj.realm.objects.Route;
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop;
import com.zsemberidaniel.egerbuszuj.realm.objects.StopTime;
import com.zsemberidaniel.egerbuszuj.realm.objects.Trip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import io.realm.Realm;

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

public class FileToRealm {

    private static final String PREF_IS_FILLED = "isRealmFilled";

    private static final String STOPS_FILE_PATH = "stops.txt";
    private static final String ROUTE_FILE_PATH = "routes.txt";

    public static void init(final Context context) {
        // if we have already filled the realm don't fill it again
        SharedPreferences preferences = context.getSharedPreferences(StaticStrings.PREFS, 0);
        if (preferences.getBoolean(PREF_IS_FILLED, false)) {
            return;
        }

        Realm.getDefaultInstance().executeTransactionAsync(new Realm.Transaction() {

            @Override
            public void execute(Realm realm) {
                fillRealm(realm, context);
            }
        });

        // indicate that we filled the realm
        preferences.edit().putBoolean(PREF_IS_FILLED, true).apply();
    }

    private static void fillRealm(Realm realm, Context context) {
        // Populate tables from files
        BufferedReader reader  = null;

        // First populate the stops table
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(STOPS_FILE_PATH)));

            String line;
            // A line looks like: IDI Name of stop
            while ((line = reader.readLine()) != null) {
                Stop stop = realm.createObject(Stop.class);
                stop.setId(line.substring(0, 3));
                stop.setName(line.substring(4));
                stop.setStarred(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // TODO couldn't open the stops file so download it or something
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            reader = null;
        }

        // Next populate the routes table
        // Fill this list as well so we can then read from the listItem.txt file as well to populate the other tables
        ArrayList<String> routes = new ArrayList<>();
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(ROUTE_FILE_PATH)));

            String line;
            // A line looks like: Name
            while ((line = reader.readLine()) != null) {
                Route route = realm.createObject(Route.class);
                route.setId(line);

                routes.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // TODO couldn't open the routes file so download it or something
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            reader = null;
        }

        // Next populate both the trip and stop_time tabled
        // Go through all the routes because we can get the filenames that way
        for (String route : routes) {
            try {
                reader = new BufferedReader(new InputStreamReader(context.getAssets().open(route + ".txt")));
                Log.i("IO/I", "Reading from " + route + ".txt");

                for (int direction = 0; direction <= 1; direction++) {
                    String headSign = reader.readLine();
                    // If head sign is - then there is no route here
                    if (headSign.equals("-")) continue;

                    // For each dayType
                    for (int j = 0; j <= 3; j++) {
                        Trip trip = realm.createObject(Trip.class);
                        trip.setId(route + j + direction);
                        trip.setDirection(direction);
                        trip.setDayType(j);
                        trip.setHeadSign(headSign);
                        trip.setRoute(realm.where(Route.class).equalTo(Route.CN_ID, route).findFirst());
                    }

                    // Read and store the data first so we can make the tables with the times
                    int stopCount = Integer.valueOf(reader.readLine());
                    int[][] types = new int[2][stopCount];
                    String[] stops = new String[stopCount];
                    for (int i = 0; i < stopCount; i++) stops[i] = reader.readLine();
                    for (int i = 0; i < stopCount; i++) types[0][i] = Integer.valueOf(reader.readLine());
                    for (int i = 0; i < stopCount; i++) types[1][i] = Integer.valueOf(reader.readLine());

                    // Read the times
                    String line;
                    String[] lineWords;
                    int lineCounter = 0;
                    byte hour = 0, type = 0;

                    while ((line = reader.readLine()) != null && !line.equals("")) {
                        // no buses in this category so don't do anything
                        if (!line.equals("-")) {
                            // The very first line of an hour: hour:type
                            if (lineCounter % 5 == 0) {
                                lineWords = line.split(":");
                                hour = Byte.valueOf(lineWords[0]);
                                type = (byte) (Byte.valueOf(lineWords[1]) - 1);
                            } else { // The other lines: min,min,min,min (...)
                                lineWords = line.split(",");

                                // Go through all stops and add them to the stop_time table with the correct time
                                for (int i = 0; i < stops.length; i++) {
                                    // Also go through all the minutes
                                    for (int k = 0; k < lineWords.length; k++) {
                                        StopTime stopTime = realm.createObject(StopTime.class);
                                        stopTime.setStop(realm.where(Stop.class).equalTo(Stop.CN_ID, stops[i]).findFirst());
                                        stopTime.setTrip(realm.where(Trip.class)
                                                .equalTo(Trip.CN_ID, route + (lineCounter % 5 - 1) + direction).findFirst());
                                        stopTime.setStopSequence(types[type][i]);

                                        // calculate the correct minutes
                                        int minutes = Integer.valueOf(lineWords[k]) + types[type][i];
                                        // if the minute goes above 60 we need to add Math.floor(minutes / 60d) to hour
                                        stopTime.setHour((byte) (Math.floor(minutes / 60d) + hour));
                                        // if minute goes above 60 we need to trim it down to [0;60[
                                        stopTime.setMinute((byte) (minutes % 60));
                                    }
                                }
                            }
                        }

                        lineCounter++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                // TODO couldn't open the stops file so download it or something
            } finally {
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                reader = null;
            }
        }
    }

}
