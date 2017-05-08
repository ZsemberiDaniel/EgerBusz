package rpc.zsemberidaniel.com.egerbusz.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
     * Populates the stationsIds list from a file!
     * @param id The id of the line
     * @param file Where the line's resource file can be reached. The constructor closes the stream.
     */
    public Line(String id, InputStreamReader file) throws IOException {
        this.id = id;

        BufferedReader reader = new BufferedReader(file);
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

        reader.close();
    }

}
