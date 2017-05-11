package rpc.zsemberidaniel.com.egerbusz.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

import rpc.zsemberidaniel.com.egerbusz.R;
import rpc.zsemberidaniel.com.egerbusz.adapter.StopTimeTableAdapter;

/**
 * Created by zsemberi.daniel on 2017. 05. 11..
 */

public class StopAllTimetable extends AppCompatActivity {

    public static final String ARG_TIMETABLE = "timetable";

    private ListView timetableListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stop_timetable);

        timetableListView = (ListView) findViewById(R.id.timeTableListView);

        // Get the timetable either from the intent or the savedInstanceState
        ArrayList<String> timetable = null;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();

            if (extras != null) timetable = extras.getStringArrayList(ARG_TIMETABLE);
        } else {
            timetable = savedInstanceState.getStringArrayList(ARG_TIMETABLE);
        }

        StopTimeTableAdapter stopTimeTableAdapter = new StopTimeTableAdapter(this);
        // Go through each result and group them first by the route id then by the direction then by the hour
        // It's going to be easy because the list is already ordered
        String currentRouteID = "";
        int currentDirection = -1;
        int currentHour = Integer.valueOf(timetable.get(0).split("#")[3]); // set tit to the first one so the first time the condition won't trigger
        ArrayList<Integer> minutes = new ArrayList<>();

        String[] words;
        for (String data : timetable) {
            words = data.split("#");

            String routeID = words[0];
            String headSign = words[1];
            int direction = Integer.valueOf(words[2]);
            int hour = Integer.valueOf(words[3]);
            int minute = Integer.valueOf(words[4]);

            // New hour should be started
            if (hour != currentHour) {
                // Also store the current one
                stopTimeTableAdapter.addItem(currentHour, minutes);

                currentHour = hour;
                minutes.clear();
            }

            // new separator needed in the adapter
            if (currentDirection != direction || !currentRouteID.equals(routeID)) {
                stopTimeTableAdapter.addItemSeperator(routeID, headSign);

                currentDirection = direction;
                currentRouteID = routeID;
            }

            minutes.add(minute);
        }

        timetableListView.setAdapter(stopTimeTableAdapter);
    }
}
