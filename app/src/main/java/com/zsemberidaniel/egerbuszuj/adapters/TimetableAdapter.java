package com.zsemberidaniel.egerbuszuj.adapters;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zsemberidaniel.egerbuszuj.R;
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop;
import com.zsemberidaniel.egerbuszuj.realm.objects.StopTime;
import com.zsemberidaniel.egerbuszuj.realm.objects.Trip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractExpandableHeaderItem;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.viewholders.ExpandableViewHolder;
import eu.davidea.viewholders.FlexibleViewHolder;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by zsemberi.daniel on 2017. 05. 14..
 */

public class TimetableAdapter extends FlexibleAdapter<TimetableAdapter.TimetableHeader> {

    private static final String routeIDHeadSignSeparator = " - ";

    public TimetableAdapter(@Nullable List<TimetableHeader> items) {
        super(items);
    }

    /**
     * Returns a list with the items from the times HashMap. Also sets up the headers.
     * @param times An array of the times. The keys are route ids plus the head sign.
     *              The value is a list of the hours and minutes. Because of the nature of the method
     *              it will modify it so if you want the original back please !!!USE A COPY!!!
     * @return null if times has 0 elements. Otherwise a list of all the items with the correct headers.
     */
    public static List<TimetableHeader> setupItems(Context context, @NonNull HashMap<String, List<HourMinutesOutput>>... times) {
        if (times.length == 0) return null;

        List<TimetableHeader> output = new ArrayList<>();

        // Add every time to the first member of the times array
        Iterator<String> keyIterator;
        String key;
        for (int i = 1; i < times.length; i++) {
            keyIterator = times[i].keySet().iterator();

            while (keyIterator.hasNext()) {
                key = keyIterator.next();

                times[0].put(key, times[i].get(key));
            }
        }

        // Now we can treat the first one as a collection of all the others
        keyIterator = times[0].keySet().iterator();

        // First sort the routeIDs so it is easier to find them
        TreeSet<String> sortedKeys = new TreeSet<>();
        while (keyIterator.hasNext())
            sortedKeys.add(keyIterator.next());

        // Then add the items in order
        for (int i = 0; i < times[0].keySet().size(); i++) {
            String routeIDHeadSign = sortedKeys.pollFirst();
            List<HourMinutesOutput> hours = times[0].get(routeIDHeadSign);

            TimetableHeader header = new TimetableHeader(routeIDHeadSign);
            for (int k = 0; k < hours.size(); k++) {
                header.addSubItem(new TimetableItem(context, header, hours.get(k)));
            }
            output.add(header);
        }

        return output;
    }

    /**
     * Returns the times a bus is at a stop. If we want we can even specify which bus (route).
     * @param stop The stop for which this function returns the hours and minutes
     * @param route Optional. If we only want the hours for a given route for the stop
     * @param dayType What type of day we want it for
     * @param direction Which direction the bus goes
     * @return The keys are route ids plus the head sign (so if route is given there will only be one). The value is a list of the hours and minutes.
     */
    public static HashMap<String, List<HourMinutesOutput>> getAllTimes(@NonNull String stop, @Nullable String route,
                                                                       @IntRange(from = 0, to = 4) int dayType,
                                                                       @IntRange(from = 0, to = 1) int direction) {

        Realm realm = Realm.getDefaultInstance();
        RealmQuery<StopTime> times = realm.where(StopTime.class);

        // Look for stopTimes with the given route, direction and dayType
        // This can be achieved with the proper trip id because that is made up of all of
        // that information
        if (route != null)
            times.equalTo(StopTime.CN_TRIP + "." + Trip.CN_ID, route + dayType + direction);
        else // otherwise we'll still need to apply the direction and the dayType
            times.like(StopTime.CN_TRIP + "." + Trip.CN_ID, "*" + dayType + direction);

        // Look for stopTimes with the given stop
        times.equalTo(StopTime.CN_STOP + "." + Stop.CN_ID, stop);
        realm.close();

        // Order like this because we want to group by routes then the time
        RealmResults<StopTime> sortedTimes = times.findAll();
        sortedTimes = sortedTimes.sort(new String[] { StopTime.CN_TRIP + "." + Trip.CN_ID, StopTime.CN_HOUR, StopTime.CN_MINUTE},
                new Sort[] { Sort.ASCENDING, Sort.ASCENDING, Sort.ASCENDING });

        // if we have noe StopTimes with the given attributes return null
        if (sortedTimes.size() == 0) return null;

        HashMap<String, List<HourMinutesOutput>> output = new HashMap<>();
        List<HourMinutesOutput> hours = new ArrayList<>();

        // what are the current groups
        String currentRoute = sortedTimes.get(0).getTrip().getRoute().getId();
        String currentHeadSign = sortedTimes.get(0).getTrip().getHeadSign();
        HourMinutesOutput currentHourMinutes = new HourMinutesOutput(sortedTimes.get(0).getHour());

        String routeId, headSign = "";
        int hour, min;
        for (int i = 0; i < sortedTimes.size(); i++) {
            // examined StopTime attributes
            routeId = sortedTimes.get(i).getTrip().getRoute().getId();
            headSign = sortedTimes.get(i).getTrip().getHeadSign();
            hour = sortedTimes.get(i).getHour();
            min = sortedTimes.get(i).getMinute();
            Log.i("DATA", routeId + " " + headSign + " " + hour + " " + min);

            // Start a new hour group
            if (hour != currentHourMinutes.hour || !routeId.equals(currentRoute)) {
                // store stuff
                hours.add(currentHourMinutes);

                currentHourMinutes = new HourMinutesOutput(hour);
            }

            // Start a new route group
            if (!routeId.equals(currentRoute)) {
                // of course first store the current stuff
                output.put(currentRoute + routeIDHeadSignSeparator + currentHeadSign, new ArrayList<>(hours));

                hours.clear();
                currentRoute = routeId;
                currentHeadSign = headSign;
            }

            currentHourMinutes.minutes.add(min);
        }

        // Add the very last ones
        hours.add(currentHourMinutes);
        output.put(currentRoute + routeIDHeadSignSeparator + headSign, new ArrayList<>(hours));

        return output;
    }

    public static class HourMinutesOutput {
        public int hour;
        public List<Integer> minutes;

        public HourMinutesOutput(int hour) {
            this(hour, new ArrayList<Integer>());
        }

        /**
         * @param minutes Does NOT copy it
         */
        public HourMinutesOutput(int hour, List<Integer> minutes) {
            this.hour = hour;
            this.minutes = minutes;
        }
    }

    public static class TimetableItem extends AbstractSectionableItem<TimetableItem.TimetableItemViewHolder, TimetableHeader> {

        private HourMinutesOutput hourMinutes;
        private Context context;

        private String hashString;

        public TimetableItem(Context context, TimetableHeader header, int hour, List<Integer> minutes) {
            this(context, header, new HourMinutesOutput(hour, minutes));
        }

        public TimetableItem(Context context, TimetableHeader header, HourMinutesOutput hourMinutes) {
            super(header);
            this.context = context;
            this.hourMinutes = hourMinutes;

            hashString = hourMinutes.hour + header.getRouteIDHeadSign();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof TimetableItem) {
                TimetableItem item = (TimetableItem) o;

                return item.hashString.equals(hashString);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return hashString.hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.timetable_data;
        }

        @Override
        public TimetableItemViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater,
                                                        ViewGroup parent) {
            return new TimetableItemViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter adapter, TimetableItemViewHolder holder,
                                   int position, List payloads) {
            holder.minutesLinearLayout.removeAllViews();

            holder.hourTextView.setText(formatToTwoDigit(hourMinutes.hour));
            holder.minuteTextViews = new TextView[hourMinutes.minutes.size()];

            // add children to linear layout
            for (int i = 0; i < hourMinutes.minutes.size(); i++) {
                holder.minuteTextViews[i] = new TextView(context);
                holder.minuteTextViews[i].setText(formatToTwoDigit(hourMinutes.minutes.get(i)) +
                        (i != hourMinutes.minutes.size() - 1 ? ", " : ""));
                holder.minutesLinearLayout.addView(holder.minuteTextViews[i]);
            }
        }

        /**
         * Formats a number to two digits. For example 2 -> 02, 5 -> 05, 23 -> 23
         * If number is above two digits it will just be returned as a string
         */
        private String formatToTwoDigit(int t) {
            return t < 10 ? "0" + t : String.valueOf(t);
        }

        public static class TimetableItemViewHolder extends FlexibleViewHolder {

            private TextView hourTextView;
            private LinearLayout minutesLinearLayout;
            private TextView[] minuteTextViews;

            public TimetableItemViewHolder(View view, FlexibleAdapter adapter) {
                super(view, adapter);

                hourTextView = (TextView) view.findViewById(R.id.hourTextView);
                minutesLinearLayout = (LinearLayout) view.findViewById(R.id.minutesLinearLayout);
            }
        }

    }

    public static class TimetableHeader extends AbstractExpandableHeaderItem<TimetableHeader.TimetableHeaderViewHolder, TimetableItem> {

        private String routeIDHeadSign;
        public String getRouteIDHeadSign() { return routeIDHeadSign; }

        public TimetableHeader(String routeIDHeadSign) {
            super();
            setExpanded(false);
            this.routeIDHeadSign = routeIDHeadSign;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof TimetableHeader) {
                TimetableHeader header = (TimetableHeader) o;
                return header.routeIDHeadSign.equals(routeIDHeadSign);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return routeIDHeadSign.hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.timetable_header;
        }

        @Override
        public TimetableHeaderViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
            return new TimetableHeaderViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter adapter, TimetableHeaderViewHolder holder, int position, List payloads) {
            holder.titleTextView.setText(routeIDHeadSign);
        }

        public static class TimetableHeaderViewHolder extends ExpandableViewHolder {

            public TextView titleTextView;

            public TimetableHeaderViewHolder(View view, FlexibleAdapter adapter) {
                super(view, adapter, true);

                titleTextView = (TextView) view.findViewById(R.id.routeTextView);
            }
        }
    }

}
