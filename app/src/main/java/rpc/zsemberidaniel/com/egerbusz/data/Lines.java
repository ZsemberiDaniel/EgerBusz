package rpc.zsemberidaniel.com.egerbusz.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by zsemberi.daniel on 2017. 05. 05..
 */

public class Lines {
    private static final String LINES_FILE_PATH = "lines.txt";

    private static HashMap<String, Line> lines;

    /**
     * @return An unmodifiable version of the lines list.
     */
    public static Collection<Line> getLines() {
        return Collections.unmodifiableCollection(lines.values());
    }

    public static Line getLine(String id) {
        return lines.get(id);
    }

    /**
     * Initializes the line list from the given file. If already had been done it returns.
     * REQUIRES the stations to be loaded.
     * @param context The current context
     */
    public static void init(Context context) {
        // We have already read the lines
        if (lines != null && !lines.isEmpty()) return;

        lines = new HashMap<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(LINES_FILE_PATH)));

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    lines.put(line, new Line(line, new InputStreamReader(context.getAssets().open(line + ".txt"))));
                } catch (IOException exception) {
                    // No file for this line.... for now do nothing
                    // TODO fetch the file for this line from server
                }
            }
        } catch (IOException e) {
            // Couldn't read the file
            // TODO maybe fetch it from the server here
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                // Couldn't close reader, don't know what happened
                e.printStackTrace();
            }
        }
    }

}
