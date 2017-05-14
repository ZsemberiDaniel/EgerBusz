package com.zsemberidaniel.egerbuszuj.adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zsemberidaniel.egerbuszuj.R;
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.viewholders.FlexibleViewHolder;
import io.realm.Realm;

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

public class ChooseStopAdapter extends FlexibleAdapter<ChooseStopAdapter.ChooseStopItem> {

    private List<ChooseStopItem> items;

    private String filter = "";

    public ChooseStopAdapter(@Nullable List<ChooseStopItem> items) {
        super(new ArrayList<>(items));
        this.items = items;
    }

    /**
     * Updates the starred attribute of the ChooseStopItems, sorts them, then updates the display as well
     * If it is filtered it will remove the filter and update the whole data set
     */
    private void updateAndSortDataSet() {
        // remove filter because this function will take the whole list from the constructor into consideration
        if (isFiltered()) {
            setSearchText("");
        }

        // Update starred
        Realm realm = Realm.getDefaultInstance();
        for (int i = 0; i < items.size(); i++)
            items.get(i).setStarred(realm.where(Stop.class).equalTo(Stop.CN_ID, items.get(i).getStopId())
                    .findFirst().isStarred());
        realm.close();

        // sort
        Collections.sort(items);

        // update display
        setNotifyMoveOfFilteredItems(true);
        super.updateDataSet(new ArrayList<>(items), true);
    }

    /**
     * @return Is the FlexibleAdapter filtered in any way?
     */
    public boolean isFiltered() {
        return !filter.equals("");
    }

    @Override
    public void setSearchText(String searchText) {
        this.filter = searchText;

        super.setSearchText(searchText);
    }

    @Override
    protected void onPostFilter() {
        super.onPostFilter();

        // update and sort the data set in case the user starred a stop
        if (filter.equals(""))
            updateAndSortDataSet();
    }

    /**
     * Generates a header HashMap for the given stops. (Only includes the letter which the stops start
     * with). It is a method to be used with the convertToChooseStopItems.
     * @param stops The stops from which to get the starting characters.
     * @return A HashMap with uppercase Character keys and the headers.
     */
    public static HashMap<Character, ChooseStopHeader> getNewHeaders(List<Stop> stops) {
        Set<Character> characters = new HashSet<>();
        characters.add('*');
        HashMap<Character, ChooseStopHeader> headers = new HashMap<>();

        for (Stop stop : stops) characters.add(stop.getName().toUpperCase().charAt(0));

        for (char letter : characters) headers.put(letter, new ChooseStopHeader(letter));

        return headers;
    }

    /**
     * Converts the given items to the ChoseStopItem GUI class so it can be added to FlexibleAdapter.
     * It needs the letter headers which will be added to the FlexibleAdapter as well. They need to be
     * in a HashMap. The keys are the characters (uppercase) and the values are the headers themselves.
     * Keeps the order of the items.
     * @param stops The stops to be converted
     * @param headers The letter headers to be added
     * @return The GUI items
     */
    public static TreeSet<ChooseStopItem> convertToChooseStopItems(List<Stop> stops,
                                                                     HashMap<Character, ChooseStopHeader> headers) {
        TreeSet<ChooseStopItem> items = new TreeSet<>();
        ChooseStopHeader starHeader = headers.get('*');

        for (Stop stop : stops) {
            items.add(new ChooseStopItem(headers.get(stop.getName().toUpperCase().charAt(0)),
                    starHeader, stop.getId(), stop.getName(), stop.isStarred()));
        }

        return items;
    }

    public static class ChooseStopItem extends AbstractSectionableItem<ChooseStopItem.ChooseStopItemViewHolder, ChooseStopHeader>
                                       implements IFilterable, Comparable<ChooseStopItem> {

        private String stopId;
        public String getStopId() { return stopId; }
        private String stopName;
        public String getStopName() { return stopName; }
        private boolean starred;
        public boolean isStarred() { return starred; }
        void setStarred(boolean starred) { this.starred = starred; }

        private ChooseStopHeader starredHeader;
        private ChooseStopHeader letterHeader;

        public ChooseStopItem(ChooseStopHeader letterHeader, ChooseStopHeader starredHeader,
                              String stopId, String stopName, boolean starred) {
            super(starred ? starredHeader : letterHeader);
            this.stopId = stopId;
            this.stopName = stopName;
            this.starred = starred;
            this.starredHeader = starredHeader;
            this.letterHeader = letterHeader;
        }

        @Override
        public boolean isSelectable() {
            return true;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof ChooseStopItem) {
                ChooseStopItem other = (ChooseStopItem) object;
                return other.stopId.equals(stopId);
            }

            return false;
        }

        @Override
        public int hashCode() {
            return stopId.hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.stop_list_item;
        }

        @Override
        public ChooseStopItemViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater,
                                                         ViewGroup parent) {
            ChooseStopItemViewHolder viewHolder =
                    new ChooseStopItemViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);

            return viewHolder;
        }

        @Override
        public void bindViewHolder(final FlexibleAdapter adapter, final ChooseStopItemViewHolder holder, final int position,
                                   final List payloads) {
            holder.stopNameTextView.setText(stopName);
            setStarredImageCorrectly(holder);

            // setup on click listener for the starred image
            holder.starredImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Toggle starred in database
                    Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Stop stop = realm.where(Stop.class).equalTo(Stop.CN_ID, getStopId()).findFirst();

                            stop.setStarred(!stop.isStarred());
                        }
                    });

                    // update the starred boolean here
                    starred = Realm.getDefaultInstance().where(Stop.class).equalTo(Stop.CN_ID, getStopId())
                            .findFirst().isStarred();

                    // update display
                    setStarredImageCorrectly(holder);
                    if (starred) setHeader(starredHeader);
                    else setHeader(letterHeader);

                    ChooseStopAdapter chooseStopAdapter = (ChooseStopAdapter) adapter;

                    // only update and sort the data set if it is not filtered because if it is filtered
                    // it updates it in a way which displays all of the data
                    if (!chooseStopAdapter.isFiltered())
                        chooseStopAdapter.updateAndSortDataSet();
                }
            });
        }

        @Override
        public boolean filter(String constraint) {
            return stopName.toLowerCase().contains(constraint.toLowerCase());
        }

        /**
         * Sets the starred image of the given viewHolder correctly based on this class' starred
         * boolean. It gets the drawable from ResourceCompat
         * @param viewHolder
         */
        private void setStarredImageCorrectly(ChooseStopItemViewHolder viewHolder) {
            if (viewHolder.starredImageView == null) return;

            if (isStarred()) {
                viewHolder.starredImageView.setImageDrawable(
                        ResourcesCompat.getDrawable(viewHolder.starredImageView.getResources(),
                                R.drawable.ic_star, null)
                );
            } else {
                viewHolder.starredImageView.setImageDrawable(
                        ResourcesCompat.getDrawable(viewHolder.starredImageView.getResources(),
                                R.drawable.ic_star_border, null)
                );
            }
        }

        @Override
        public int compareTo(@NonNull ChooseStopItem o) {
            if (o.starred && !starred) return 1;
            if (starred && !o.starred) return -1;

            return -o.stopName.compareTo(stopName);
        }

        public static class ChooseStopItemViewHolder extends FlexibleViewHolder {

            public TextView stopNameTextView;
            public ImageView starredImageView;

            public ChooseStopItemViewHolder(View view, FlexibleAdapter adapter) {
                super(view, adapter, false);

                stopNameTextView = (TextView) view.findViewById(R.id.stopNameText);
                starredImageView = (ImageView) view.findViewById(R.id.starredImgView);
            }
        }
    }

    public static class ChooseStopHeader extends AbstractHeaderItem<ChooseStopHeader.ChooseStopHeaderViewHolder> {

        private char letter;

        public ChooseStopHeader(char letter) {
            this.letter = letter;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ChooseStopHeader) {
                ChooseStopHeader other = (ChooseStopHeader) o;
                return other.letter == letter;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return String.valueOf(letter).hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.letter_item_header;
        }

        @Override
        public ChooseStopHeaderViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
            return new ChooseStopHeaderViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter adapter, ChooseStopHeaderViewHolder holder, int position, List payloads) {
            holder.letterTextView.setText(String.valueOf(letter));
        }

        public static class ChooseStopHeaderViewHolder extends FlexibleViewHolder {

            private TextView letterTextView;

            public ChooseStopHeaderViewHolder(View view, FlexibleAdapter adapter) {
                super(view, adapter, true);

                letterTextView = (TextView) view.findViewById(R.id.letterTextView);
            }
        }
    }
}
