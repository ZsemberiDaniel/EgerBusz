package rpc.zsemberidaniel.com.egerbusz.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zsemberi.daniel on 2017. 05. 05..
 * Defines one line. It contains it's id, it's list of stations in order.
 */
public class Line {
    /**
     * The id of this line. For example: 12, 2i (...)
     */
    private String id;
    public String getId() { return id; }

    private LineOneWay[] lineBothWays;
    public LineOneWay getLineForward() { return lineBothWays[0]; }
    public LineOneWay getLineBackward() { return lineBothWays[1]; }
    public LineOneWay[] getLineBothWays() { return lineBothWays; }

    /**
     * Init a line
     * @param id The id of the line
     * @param file From where we can read the data for the line
     */
    public Line(String id, InputStreamReader file) throws IOException {
        this.id = id;

        lineBothWays = new LineOneWay[2];
        BufferedReader reader = new BufferedReader(file);
        lineBothWays[0] = new LineOneWay(reader.readLine(), reader);
        lineBothWays[1] = new LineOneWay(reader.readLine(), reader);
    }

    /**
     * Describes only one way of the line
     */
    public class LineOneWay {

        /**
         * The name of this line which describes which way it goes
         */
        private String name;
        public String getName() { return name; }

        /**
         * The ids of the stations on this line in order.
         */
        private String[] stationIds;

        /**
         * The name of all stations in the order of the ids.
         */
        private String[] stationNames;
        public String[] getStationNames() { return stationNames; }

        /**
         * How the time goes from station to station in the first kind of timetable.
         * Every value represents at the ith station how much time passed from the start.
         */
        private int[] timeOne;
        /**
         * How the time goes from station to station in the second kind of timetable.
         * Every value represents at the ith station how much time passed from the start.
         */
        private int[] timeTwo;

        /**
         * Populates the stationsIds list from a file! Does not close the reader
         * @param name The name of this line which describes which way it goes
         * @param reader The reader from which we can read the data of the line
         */
        public LineOneWay(String name, BufferedReader reader) throws IOException {
            this.name = name;
            int countOfStations = Integer.valueOf(reader.readLine());

            stationIds = new String[countOfStations];
            stationNames = new String[countOfStations];
            timeOne = new int[countOfStations];
            timeTwo = new int[countOfStations];

            // Read the ids
            for (int i = 0; i < countOfStations; i++) {
                stationIds[i] = reader.readLine();
                stationNames[i] = Stations.getName(stationIds[i]);
            }
            // Read the timeones
            for (int i = 0; i < countOfStations; i++)
                timeOne[i] = Integer.valueOf(reader.readLine());
            // Read the timetwos
            for (int i = 0; i < countOfStations; i++)
                timeTwo[i] = Integer.valueOf(reader.readLine());

            // read the timetable
            String line;
            int lineCounter = 0;
            while ((line = reader.readLine()) != null) {
                lineCounter++;

                // Started a new hour in the file
                if (lineCounter % 5 == 0) {

                }
            }
        }
    }

    public class LineDBHelper extends SQLiteOpenHelper {

        // Database data
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "lines.db";

        // Table data
        private LineEntry lineEntry;

        public LineDBHelper(Context context, String lineId) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);

            lineEntry = new LineEntry(lineId + ".db");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(lineEntry.CMD_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        private class LineEntry {
            public String tableName;
            public final String columnNameStartHour = "hour";
            public final String columnNameStartMinute = "minute";
            public final String columnNameDayType = "day_type";

            public String CMD_CREATE_TABLE;
            public String CMD_DELETE_TABLE;

            public LineEntry(String tableName) {
                this.tableName = tableName;

                CMD_CREATE_TABLE = "CREATE TABLE " + tableName + " (" +
                        columnNameStartHour + " INTEGER," +
                        columnNameStartMinute + " INTEGER," +
                        columnNameDayType + " INTEGER)";
                CMD_DELETE_TABLE = "DROP TABLE IF EXISTS " + tableName;
            }
        }
    }
}
