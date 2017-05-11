package rpc.zsemberidaniel.com.egerbusz.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import rpc.zsemberidaniel.com.egerbusz.R;

/**
 * Created by zsemberi.daniel on 2017. 05. 11..
 */

public class StopTimeTableAdapter extends BaseAdapter {

    private static final int ID_DATA = 0;
    private static final int ID_SEPARATOR_DATA = 1;

    private ArrayList<Object> data;
    private TreeSet<Integer> separatorPositions;

    private Context context;
    private LayoutInflater layoutInflater;

    public StopTimeTableAdapter(Context context) {
        this.context = context;

        data = new ArrayList<>();
        separatorPositions = new TreeSet<>();
        layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Add item to adapter. The formatting will be done here
     */
    public void addItem(int hour, List<Integer> list) {
        Integer[] array = new Integer[list.size()];
        list.toArray(array);
        addItem(hour, array);
    }

    /**
     * Add item to adapter. The formatting will be done here, so if not sure about formatting use this
     */
    public void addItem(int hour, Integer... minutes) {
        String sHour = (hour < 10 ? "0" : "") + hour;
        StringBuilder sMinutes = new StringBuilder();

        // Build it like this: 10, 25, 30
        for (int i = 0; i < minutes.length; i++) {
            sMinutes.append(minutes[i]);
            if (i != minutes.length - 1) sMinutes.append(", ");
        }

        addItem(sHour, sMinutes.toString());
    }

    /**
     * Add item to adapter. The formatting won't change in this case.
     * @param hour The formatted hour String that will be displayed
     * @param minutes The formatted minuteS String that will be displayed
     */
    public void addItem(String hour, String minutes) {
        data.add(new StopTimeTableDataForAdapter(hour, minutes));
    }

    public void addItemSeperator(String routeId, String headSign) {
        data.add(new StopTimeTableSeparatorForAdapter(routeId, headSign));

        // save the separator position
        separatorPositions.add(data.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return separatorPositions.contains(position) ? ID_SEPARATOR_DATA : ID_DATA;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        int type = getItemViewType(position);

        // We have no view to recycle
        if (convertView == null) {
            viewHolder = new ViewHolder();

            // Based on the type create the layout and store it to the ViewHolder
            switch (type) {
                case ID_DATA:
                    convertView = layoutInflater.inflate(R.layout.stop_timetable_data, null);
                    viewHolder.hourTextView = (TextView) convertView.findViewById(R.id.hourTextView);
                    viewHolder.minutesTextView = (TextView) convertView.findViewById(R.id.minutesTextView);
                    break;
                case ID_SEPARATOR_DATA:
                    convertView = layoutInflater.inflate(R.layout.stop_timetable_header, null);
                    viewHolder.routeTextView = (TextView) convertView.findViewById(R.id.routeTextView);
                    break;
            }

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        switch (type) {
            case ID_DATA:
                StopTimeTableDataForAdapter data = (StopTimeTableDataForAdapter) getItem(position);

                viewHolder.hourTextView.setText(data.getHour());
                viewHolder.minutesTextView.setText(data.getMinutes());
                break;
            case ID_SEPARATOR_DATA:
                StopTimeTableSeparatorForAdapter header = (StopTimeTableSeparatorForAdapter) getItem(position);

                viewHolder.routeTextView.setText(header.getFormattedHeader());
                break;
        }

        return convertView;
    }

    private class ViewHolder {
        public TextView hourTextView;
        public TextView minutesTextView;
        public TextView routeTextView;
    }

    /**
     * Data class for the simple data row
     */
    private class StopTimeTableDataForAdapter {
        private String hour;
        public String getHour() { return hour; }
        private String minutes;
        public String getMinutes() { return minutes; }

        public StopTimeTableDataForAdapter(String hour, String minutes) {
            this.hour = hour;
            this.minutes = minutes;
        }
    }

    /**
     * Data class for the header row
     */
    private class StopTimeTableSeparatorForAdapter {
        private String routeId;
        public String getRouteId() { return routeId; }
        private String headSign;
        public String getHeadSign() { return headSign; }

        private String formattedHeader;
        public String getFormattedHeader() { return formattedHeader; }

        public StopTimeTableSeparatorForAdapter(String routeId, String headSign) {
            this.routeId = routeId;
            this.headSign = headSign;

            formattedHeader = routeId + " - " + headSign;
        }
    }
}
