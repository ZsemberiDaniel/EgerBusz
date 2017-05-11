package rpc.zsemberidaniel.com.egerbusz.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by zsemberi.daniel on 2017. 05. 08..
 */

public class GTFS extends SQLiteOpenHelper {

    private static GTFS instance;
    public static GTFS getInstance() {
        if (instance == null) throw new NullPointerException("Instance not initialized yet!");

        return instance;
    }
    public static void initialize(Context context) {
        if (instance == null)
            instance = new GTFS(context);
    }

    private static final String STOPS_FILE_PATH = "stops.txt";
    private static final String ROUTE_FILE_PATH = "routes.txt";

    private static final String DATABASE_NAME = "BusTimetable.db";
    private static final int DATABASE_VERSION = 16;

    private Context context;

    public GTFS(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    private void createAndPopulateTables(Context context, SQLiteDatabase database) {
        // Create tables
        database.execSQL(StopTable.CMD_CREATE_TABLE);
        database.execSQL(RouteTable.CMD_CREATE_TABLE);
        database.execSQL(StopTimeTable.CMD_CREATE_TABLE);
        database.execSQL(TripTable.CMD_CREATE_TABLE);

        // Populate tables from files
        BufferedReader reader  = null;
        ContentValues contentValues = new ContentValues();

        // First populate the stops table
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(STOPS_FILE_PATH)));

            String line;
            // A line looks like: IDI Name of stop
            while ((line = reader.readLine()) != null) {
                contentValues.clear();
                contentValues.put(StopTable.COLUMN_NAME_ID, line.substring(0, 3));
                contentValues.put(StopTable.COLUMN_NAME_NAME, line.substring(4));

                database.insert(StopTable.TABLE_NAME, null, contentValues);
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
                contentValues.clear();
                contentValues.put(RouteTable.COLUMN_NAME_ID, line);

                database.insert(RouteTable.TABLE_NAME, null, contentValues);

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
                        contentValues.clear();
                        contentValues.put(TripTable.COLUMN_NAME_DAY_TYPE, j);
                        contentValues.put(TripTable.COLUMN_NAME_DIRECTION, direction);
                        contentValues.put(TripTable.COLUMN_NAME_HEAD_SIGN, headSign);
                        contentValues.put(TripTable.COLUMN_NAME_ROUTE_ID, route);
                        contentValues.put(TripTable.COLUMN_NAME_ID, route + j + direction);
                        database.insert(TripTable.TABLE_NAME, null, contentValues);
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
                    int lineCounter = 0, hour = 0, type = 0;

                    while ((line = reader.readLine()) != null && !line.equals("")) {
                        // no buses in this category so don't do anything
                        if (!line.equals("-")) {
                            // The very first line of an hour: hour:type
                            if (lineCounter % 5 == 0) {
                                lineWords = line.split(":");
                                hour = Integer.valueOf(lineWords[0]);
                                type = Integer.valueOf(lineWords[1]) - 1;
                            } else { // The other lines: min,min,min,min (...)
                                lineWords = line.split(",");

                                // Go through all stops and add them to the stop_time table with the correct time
                                for (int i = 0; i < stops.length; i++) {
                                    contentValues.clear();
                                    contentValues.put(StopTimeTable.COLUMN_NAME_STOP_ID, stops[i]);
                                    contentValues.put(StopTimeTable.COLUMN_NAME_TRIP_ID, route + (lineCounter % 5 - 1) + direction);
                                    contentValues.put(StopTimeTable.COLUMN_NAME_STOP_SEQUENCE, i);

                                    // Also go through all the minutes
                                    for (int k = 0; k < lineWords.length; k++) {
                                        // remove minutes and hours cause they are the only ones that change
                                        contentValues.remove(StopTimeTable.COLUMN_NAME_HOUR);
                                        contentValues.remove(StopTimeTable.COLUMN_NAME_MINUTE);

                                        // So change them
                                        int minutes = Integer.valueOf(lineWords[k]) + types[type][i];
                                        // if the minute goes above 60 we need to add Math.floor(minutes / 60d) to hour
                                        contentValues.put(StopTimeTable.COLUMN_NAME_HOUR, Math.floor(minutes / 60d) + hour);
                                        // if minute goes above 60 we need to trim it down to [0;60[
                                        contentValues.put(StopTimeTable.COLUMN_NAME_MINUTE, minutes % 60);

                                        database.insert(StopTimeTable.TABLE_NAME, null, contentValues);
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

    /**
     * All the data for the Stop table in the database
     */
    public static class StopTable implements BaseColumns {
        public static final String TABLE_NAME = "stops";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_NAME = "name";

        public static final String CMD_CREATE_TABLE =
                        "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
                        COLUMN_NAME_NAME + " TEXT)";
    }

    /**
     * All the data for the Route table in the database
     */
    public static class RouteTable implements BaseColumns {
        public static final String TABLE_NAME = "routes";
        public static final String COLUMN_NAME_ID = "id";

        public static final String CMD_CREATE_TABLE =
                        "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_NAME_ID + " TEXT PRIMARY KEY)";
    }

    /**
     * All the data for the StopTime table in the database
     */
    public static class StopTimeTable implements BaseColumns {
        public static final String TABLE_NAME = "stop_times";
        public static final String COLUMN_NAME_TRIP_ID = "trip_id";
        public static final String COLUMN_NAME_STOP_ID = "stop_id";
        public static final String COLUMN_NAME_HOUR = "hour";
        public static final String COLUMN_NAME_MINUTE = "minute";
        public static final String COLUMN_NAME_STOP_SEQUENCE = "stop_sequence";

        public static final String CMD_CREATE_TABLE =
                        "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_NAME_TRIP_ID + " TEXT," +
                        COLUMN_NAME_STOP_ID + " TEXT," +
                        COLUMN_NAME_HOUR + " INTEGER," +
                        COLUMN_NAME_MINUTE + " INTEGER," +
                        COLUMN_NAME_STOP_SEQUENCE + " INTEGER)";
    }

    /**
     * All the data for the Trip table in the database
     */
    public static class TripTable implements BaseColumns {
        public static final String TABLE_NAME = "trips";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_ROUTE_ID = "route_id";
        public static final String COLUMN_NAME_DAY_TYPE = "day_type";
        public static final String COLUMN_NAME_HEAD_SIGN = "head_sign_text";
        public static final String COLUMN_NAME_DIRECTION = "direction";

        public static final String CMD_CREATE_TABLE =
                        "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
                        COLUMN_NAME_ROUTE_ID + " TEXT," +
                        COLUMN_NAME_DAY_TYPE + " INTEGER," +
                        COLUMN_NAME_HEAD_SIGN + " TEXT," +
                        COLUMN_NAME_DIRECTION + " INTEGER)";
    }

    // ____________________ SQLITE METHODS _____________________
    @Override
    public void onCreate(SQLiteDatabase db) {
        createAndPopulateTables(context, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO For debugging purposes just drop all tables
        db.execSQL("DROP TABLE IF EXISTS " + StopTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RouteTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + StopTimeTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TripTable.TABLE_NAME);
        onCreate(db);
    }
}
