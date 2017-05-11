package rpc.zsemberidaniel.com.egerbusz.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.joda.time.DateTime;

import java.util.ArrayList;

import rpc.zsemberidaniel.com.egerbusz.R;
import rpc.zsemberidaniel.com.egerbusz.adapter.StopAllAdapter;
import rpc.zsemberidaniel.com.egerbusz.data.GTFS;
import rpc.zsemberidaniel.com.egerbusz.adapter.StopAdapter;
import rpc.zsemberidaniel.com.egerbusz.data.TodayType;

/**
 * Created by zsemberi.daniel on 2017. 05. 04..
 */

public class ChooseBusFragment extends Fragment {

    public static final String ARG_DIRECTION = "direction";

    private int direction;
    private String headSignText = "";
    public String getHeadSignText() { return headSignText; }

    private ListView stationListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_bus, container, false);

        // Get the direction from the argument bundle
        direction = getArguments().getInt(ARG_DIRECTION);

        // Get views
        stationListView = (ListView) view.findViewById(R.id.stationListView);

        return view;
    }

    public void updateListView(String selected) {
        Cursor stopsCursor;

        // ___________________ Nothing is selected so show everything in alphabetical order __________________________
        if (selected.equals(getResources().getString(R.string.NoneText))) {
            ArrayList<StopAllAdapter.StopAllForAdapter> stationNames = new ArrayList<>();

            // Get data from the Stop table
            stopsCursor = GTFS.getInstance().getReadableDatabase().query(
                    GTFS.StopTable.TABLE_NAME,
                    new String[] { "*" },
                    null, null, null, null,
                    GTFS.StopTable.COLUMN_NAME_NAME
            );

            while (stopsCursor.moveToNext()) {
                stationNames.add(new StopAllAdapter.StopAllForAdapter(
                        stopsCursor.getString(stopsCursor.getColumnIndex(GTFS.StopTable.COLUMN_NAME_ID)),
                        stopsCursor.getString(stopsCursor.getColumnIndex(GTFS.StopTable.COLUMN_NAME_NAME)),
                        stopsCursor.getInt(stopsCursor.getColumnIndex(GTFS.StopTable.COLUMN_NAME_STARRED)) > 0
                ));
            }
            stopsCursor.close();

            stationListView.setAdapter(new StopAllAdapter(getActivity(), stationNames));
            stationListView.setOnItemClickListener(new StopAllAdapter.StopAllOnItemClickListener());


        } else {// __________________ A particular route is selected so show that in the correct order _____________
            // Set the correct head sign text
            Cursor headSignCursor = GTFS.getInstance().getReadableDatabase().query(
                    true,
                    GTFS.TripTable.TABLE_NAME,
                    new String[] { GTFS.TripTable.COLUMN_NAME_HEAD_SIGN },
                    GTFS.TripTable.COLUMN_NAME_ROUTE_ID + " = ? AND " + GTFS.TripTable.COLUMN_NAME_DIRECTION + " = ?",
                    new String[] { selected, String.valueOf(direction) },
                    null, null, null, null
            );
            // We have a head sign so we have a route in this direction
            if (headSignCursor.moveToNext()) {
                headSignText = headSignCursor.getString(headSignCursor.getColumnIndex(GTFS.TripTable.COLUMN_NAME_HEAD_SIGN));
            } else {
                // if we don't have a head sign we have no route in this direction
                headSignText = "";
                headSignCursor.close();

                stationListView.setAdapter(null);
                return;
            }
            headSignCursor.close();

            // Select all stops from the given route
            stopsCursor =  GTFS.getInstance().getReadableDatabase().rawQuery(
                "SELECT DISTINCT " + GTFS.StopTable.COLUMN_NAME_NAME + ", " + GTFS.StopTable.COLUMN_NAME_ID +
                " FROM " + GTFS.StopTimeTable.TABLE_NAME + " INNER JOIN " + GTFS.StopTable.TABLE_NAME +
                " ON " + GTFS.StopTimeTable.TABLE_NAME + "." + GTFS.StopTimeTable.COLUMN_NAME_STOP_ID +
                        "=" + GTFS.StopTable.TABLE_NAME + "." + GTFS.StopTable.COLUMN_NAME_ID +
                " WHERE " + GTFS.StopTimeTable.COLUMN_NAME_TRIP_ID + " = '" + selected + "0" + direction + "' OR " +
                        GTFS.StopTimeTable.COLUMN_NAME_TRIP_ID + " = '" + selected + "1" + direction + "' OR " +
                        GTFS.StopTimeTable.COLUMN_NAME_TRIP_ID + " = '" + selected + "2" + direction + "' OR " +
                        GTFS.StopTimeTable.COLUMN_NAME_TRIP_ID + " = '" + selected + "3" + direction + "' " +
                " ORDER BY " + GTFS.StopTimeTable.COLUMN_NAME_STOP_SEQUENCE + ";"
                ,null
            );

            // Here we'll store the stops in a ListView friendly ArrayList
            ArrayList<StopAdapter.StopForAdapter> stations = new ArrayList<>();

            // All the variables we'll need in the while loop
            Cursor timeCursor;
            DateTime today = DateTime.now();
            int currentDayType = TodayType.getTodayType();

            // Go through each stop and get with sql when the next bus stops there
            while (stopsCursor.moveToNext()) {
                String stopId = stopsCursor.getString(stopsCursor.getColumnIndex(GTFS.StopTable.COLUMN_NAME_ID));

                // Get the list of times for the given stop, direction and dayType and only after the now time
                timeCursor = GTFS.getInstance().getReadableDatabase().query(
                        GTFS.StopTimeTable.TABLE_NAME,
                        new String[] { GTFS.StopTimeTable.COLUMN_NAME_HOUR, GTFS.StopTimeTable.COLUMN_NAME_MINUTE },
                        GTFS.StopTimeTable.COLUMN_NAME_TRIP_ID + " = ? AND " + GTFS.StopTimeTable.COLUMN_NAME_STOP_ID + " = ? AND " + GTFS.StopTimeTable.COLUMN_NAME_HOUR + " >= ? AND " + GTFS.StopTimeTable.COLUMN_NAME_MINUTE + " >= ?",
                        new String[] { selected + currentDayType + direction, stopId, String.valueOf(today.getHourOfDay()), String.valueOf(today.getMinuteOfHour()) },
                        null, null, null
                );

                StopAdapter.StopForAdapter stopForAdapter;
                // Get only the very first time and display that
                if (timeCursor.moveToNext()) {
                    stopForAdapter = new StopAdapter.StopForAdapter(
                            stopsCursor.getString(stopsCursor.getColumnIndex(GTFS.StopTable.COLUMN_NAME_NAME)),
                            timeCursor.getInt(timeCursor.getColumnIndex(GTFS.StopTimeTable.COLUMN_NAME_HOUR)),
                            timeCursor.getInt(timeCursor.getColumnIndex(GTFS.StopTimeTable.COLUMN_NAME_MINUTE))
                    );
                } else { // if there are no more buses today then don't use the time
                    stopForAdapter = new StopAdapter.StopForAdapter(
                            stopsCursor.getString(stopsCursor.getColumnIndex(GTFS.StopTable.COLUMN_NAME_NAME)));
                }
                timeCursor.close();

                stations.add(stopForAdapter);
            }
            stopsCursor.close();

            stationListView.setAdapter(new StopAdapter(getActivity(), stations));
            stationListView.setOnItemClickListener(new StopAdapter.StopOnItemClickListener());
        }
    }
}