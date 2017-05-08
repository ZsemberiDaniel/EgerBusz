package rpc.zsemberidaniel.com.egerbusz.data;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zsemberi.daniel on 2017. 05. 03..
 */

public class Stations {
    private final static String STATIONS_FILE_PATH = "stations.txt";

    private static HashMap<String, String> stations;

    /**
     * @return An ArrayList of the station names in order.
     */
    public static ArrayList<String> getStationNamesInABCOrder() {
        ArrayList<String> values = new ArrayList<>( stations.values() );
        Collections.sort(values);

        return values;
    }

    /**
     * @return All names of the stations in some kind of order
     */
    public static Collection<String> getStationNames() {
        return Collections.unmodifiableCollection(stations.values());
    }

    /**
     * @param id Id of station.
     * @return The name of the station with the given id.
     */
    public static String getName(String id) {
        return stations.get(id);
    }

    /**
     * Initializes the stations from the file. If it has already been read then it just returns.
     * @param context The current context of the app.
     */
    public static void init(Context context) {
        // We have already read the stations
        if (stations != null && !stations.isEmpty()) return;

        BufferedReader reader = null;
        try {
            // Read stations from file
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(STATIONS_FILE_PATH)));

            int count = Integer.valueOf(reader.readLine());
            stations = new HashMap<>(count);

            String line;
            for (int i = 0; i < count; i++) {
                line = reader.readLine();
                stations.put(line.substring(0, 3), line.substring(4));
            }
        } catch (FileNotFoundException e) {
            // TODO File not found -> fetch it from the server
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("IO", "The file probably has a wrong count of the stations.");
            e.printStackTrace();
        } finally {
            // finally close the reader
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // TODO reader could not be closed -> fetch file from server
                    e.printStackTrace();
                }
            }
        }
    }
}
