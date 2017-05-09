package rpc.zsemberidaniel.com.egerbusz.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import rpc.zsemberidaniel.com.egerbusz.R;
import rpc.zsemberidaniel.com.egerbusz.data.GTFS;
import rpc.zsemberidaniel.com.egerbusz.data.StringAdapter;

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
        List<String> stationNames = new ArrayList<>();
        boolean withPics;

        // Nothing is selected so show everything in alphabetical order
        if (selected.equals(getResources().getString(R.string.NoneText))) {
            withPics = false;

            // Get data from the Stop table
            stopsCursor = GTFS.getInstance().getReadableDatabase().query(
                    GTFS.StopTable.TABLE_NAME,
                    new String[] { GTFS.StopTable.COLUMN_NAME_NAME },
                    null, null, null, null,
                    GTFS.StopTable.COLUMN_NAME_NAME
            );
        } else { // A particular route is selected so show that in the correct order
            withPics = true;

            // Set the correct head sign text
            Cursor headSignCursor = GTFS.getInstance().getReadableDatabase().query(
                    true,
                    GTFS.TripTable.TABLE_NAME,
                    new String[] { GTFS.TripTable.COLUMN_NAME_HEAD_SIGN },
                    GTFS.TripTable.COLUMN_NAME_ROUTE_ID + " = ? AND " + GTFS.TripTable.COLUMN_NAME_DIRECTION + " = ?",
                    new String[] { selected, String.valueOf(direction) },
                    null, null, null, null
            );
            headSignCursor.moveToNext();
            headSignText = headSignCursor.getString(headSignCursor.getColumnIndex(GTFS.TripTable.COLUMN_NAME_HEAD_SIGN));

            stopsCursor =  GTFS.getInstance().getReadableDatabase().rawQuery(
                "SELECT DISTINCT " + GTFS.StopTable.COLUMN_NAME_NAME +
                " FROM " + GTFS.StopTimeTable.TABLE_NAME + " INNER JOIN " + GTFS.StopTable.TABLE_NAME +
                " ON " + GTFS.StopTimeTable.TABLE_NAME + "." + GTFS.StopTimeTable.COLUMN_NAME_STOP_ID +
                        "=" + GTFS.StopTable.TABLE_NAME + "." + GTFS.StopTable.COLUMN_NAME_ID +
                " WHERE " + GTFS.StopTimeTable.COLUMN_NAME_TRIP_ID + " = '" + selected + "0" + direction + "'" +
                " ORDER BY " + GTFS.StopTimeTable.COLUMN_NAME_STOP_SEQUENCE + ";"
                ,null
            );
        }

        while (stopsCursor.moveToNext())
            stationNames.add(stopsCursor.getString(stopsCursor.getColumnIndex(GTFS.StopTable.COLUMN_NAME_NAME)));
        stopsCursor.close();
        stationListView.setAdapter(new StringAdapter(getActivity(), stationNames, withPics));
    }
}