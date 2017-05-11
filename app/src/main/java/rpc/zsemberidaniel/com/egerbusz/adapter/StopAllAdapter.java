package rpc.zsemberidaniel.com.egerbusz.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import rpc.zsemberidaniel.com.egerbusz.R;
import rpc.zsemberidaniel.com.egerbusz.activity.StopAllTimetable;
import rpc.zsemberidaniel.com.egerbusz.data.GTFS;
import rpc.zsemberidaniel.com.egerbusz.data.TodayType;

/**
 * Created by zsemberi.daniel on 2017. 05. 11..
 */

public class StopAllAdapter extends ArrayAdapter<StopAllAdapter.StopAllForAdapter> {

    private Drawable starBorderDrawable;
    private Drawable starFilledDrawable;

    private Context context;

    public StopAllAdapter(@NonNull Context context, @NonNull ArrayList<StopAllForAdapter> stops) {
        super(context, R.layout.station_list_item, stops);
        this.context = context;

        starBorderDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_star_border, null);
        starFilledDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_star, null);
    }

    private class ViewHolder {
        private TextView stopTextView;
        private ImageView starImgView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = null;

        // We have no view to convert
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.station_list_item, null);

            // Store stuff in ViewHolder, so we don't have to search with findViewById every time
            viewHolder = new ViewHolder();
            viewHolder.stopTextView = (TextView) convertView.findViewById(R.id.lineIdText);
            viewHolder.starImgView = (ImageView) convertView.findViewById(R.id.favouriteStarImgView);

            // Store the ViewHolder in the convertView
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // So now we have every view we need stored in ViewHolder and don't have to search for them
        StopAllForAdapter stop = getItem(position);

        viewHolder.stopTextView.setText(stop.getStopName());
        viewHolder.starImgView.setImageDrawable(stop.getStarred() ? starFilledDrawable : starBorderDrawable);

        return convertView;
    }

    /**
     * Click listener for the parent class
     */
    public static class StopAllOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            StopAllForAdapter selected = (StopAllForAdapter) parent.getAdapter().getItem(position);

            // Here we get the StopTimes IJ Trip with the given stop id and the given date
            Cursor stopTimes = GTFS.getInstance().getReadableDatabase().rawQuery(
                "SELECT * FROM " + GTFS.StopTimeTable.TABLE_NAME + " INNER JOIN " + GTFS.TripTable.TABLE_NAME +
                " ON " + GTFS.StopTimeTable.TABLE_NAME + "." + GTFS.StopTimeTable.COLUMN_NAME_TRIP_ID + " = " +
                        GTFS.TripTable.TABLE_NAME + "." + GTFS.TripTable.COLUMN_NAME_ID +
                " WHERE " + GTFS.StopTimeTable.COLUMN_NAME_STOP_ID + " = '" + selected.stopId + "' AND " +
                        GTFS.TripTable.COLUMN_NAME_DAY_TYPE + " = " + TodayType.getTodayType() +
                " ORDER BY " + GTFS.TripTable.COLUMN_NAME_ROUTE_ID + ", " + GTFS.TripTable.COLUMN_NAME_DIRECTION + ", " +
                        GTFS.StopTimeTable.COLUMN_NAME_HOUR + ", " + GTFS.StopTimeTable.COLUMN_NAME_MINUTE
                , null
            );

            ArrayList<String> result = new ArrayList<>();
            while (stopTimes.moveToNext()) {
                String routeID = stopTimes.getString(stopTimes.getColumnIndex(GTFS.TripTable.COLUMN_NAME_ROUTE_ID));
                String headSign = stopTimes.getString(stopTimes.getColumnIndex(GTFS.TripTable.COLUMN_NAME_HEAD_SIGN));
                int direction = stopTimes.getInt(stopTimes.getColumnIndex(GTFS.TripTable.COLUMN_NAME_DIRECTION));
                int hour = stopTimes.getInt(stopTimes.getColumnIndex(GTFS.StopTimeTable.COLUMN_NAME_HOUR));
                int minute = stopTimes.getInt(stopTimes.getColumnIndex(GTFS.StopTimeTable.COLUMN_NAME_MINUTE));

                result.add(routeID + "#" + headSign + "#" + direction + "#" + hour + "#" + minute);
            }

            Intent intent = new Intent(view.getContext(), StopAllTimetable.class);
            intent.putExtra(StopAllTimetable.ARG_TIMETABLE, result);
            view.getContext().startActivity(intent);
        }
    }

    /**
     * A class for storing data for the StopAllAdapter
     */
    public static class StopAllForAdapter {
        private String stopId;
        public String getStopId() { return stopId; }

        private String stopName;
        public String getStopName() { return stopName; }

        private boolean starred;
        public boolean getStarred() { return starred; }

        /**
         * Initializes this class with the starred value defaulted to false
         * @param stopId The three character length id of the stop
         * @param stopName The display name of the bus
         */
        public StopAllForAdapter(String stopId, String stopName) {
            this(stopId, stopName, false);
        }

        /**
         * Initializes this class
         * @param stopId The three character length id of the stop
         * @param stopName The display name of the bus
         * @param starred Whether the user has starred this particular stop
         */
        public StopAllForAdapter(String stopId, String stopName, boolean starred) {
            this.stopId = stopId;
            this.stopName = stopName;
            this.starred = starred;
        }
    }
}
