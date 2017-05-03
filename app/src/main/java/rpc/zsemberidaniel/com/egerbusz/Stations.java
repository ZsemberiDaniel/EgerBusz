package rpc.zsemberidaniel.com.egerbusz;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by zsemberi.daniel on 2017. 05. 03..
 */

public class Stations {
    private static String stationsFilePath = "stations.txt";

    private static HashMap<String, String> stations;

    public static void initStations() {
        BufferedReader reader = null;
        try {
            // Read stations from file
            reader = new BufferedReader(new FileReader("stations.txt"));

            String line = reader.readLine();
            do {


                line = reader.readLine();
            } while (!line.equals(""));
        } catch (FileNotFoundException e) {
            // TODO File not found -> fetch it from the server
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("IO", "The file probably has no empty line at the end otherwise some kind of other file format error.");
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
