package com.zsemberidaniel.egerbuszuj.adapters;

import android.util.ArraySet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zsemberidaniel.egerbuszuj.R;
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

public class ChooseStopAdapter {

    /**
     * Generates a header HashMap for the given stops. (Only includes the letter which the stops start
     * with). It is a method to be used with the convertToChooseStopItems.
     * @param stops The stops from which to get the starting characters.
     * @return A HashMap with uppercase Character keys and the headers.
     */
    public static HashMap<Character, ChooseStopHeader> getNewHeaders(List<Stop> stops) {
        Set<Character> characters = new HashSet<>();
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
    public static List<ChooseStopItem> convertToChooseStopItems(List<Stop> stops,
                                                                HashMap<Character, ChooseStopHeader> headers) {
        List<ChooseStopItem> items = new ArrayList<>(stops.size());

        for (Stop stop : stops) {
            items.add(new ChooseStopItem(headers.get(stop.getName().toUpperCase().charAt(0)), stop));
        }

        return items;
    }

    public static class ChooseStopItem extends AbstractSectionableItem<ChooseStopItem.ChooseStopItemViewHolder, ChooseStopHeader>
                                       implements IFilterable {

        private Stop stop;

        public ChooseStopItem(ChooseStopHeader header, Stop stop) {
            super(header);
            this.stop = stop;
        }

        @Override
        public boolean isSelectable() {
            return true;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof ChooseStopItem) {
                ChooseStopItem other = (ChooseStopItem) object;
                return other.stop.getId().equals(stop.getId());
            }

            return false;
        }

        @Override
        public int hashCode() {
            return stop.getId().hashCode();
        }

        @Override
        public int getLayoutRes() {
            return R.layout.stop_list_item;
        }

        @Override
        public ChooseStopItemViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater,
                                                         ViewGroup parent) {
            return new ChooseStopItemViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
        }

        @Override
        public void bindViewHolder(FlexibleAdapter adapter, ChooseStopItemViewHolder holder, int position,
                                   List payloads) {
            holder.stopNameTextView.setText(stop.getName());
        }

        @Override
        public boolean filter(String constraint) {
            return stop.getName().contains(constraint);
        }

        public static class ChooseStopItemViewHolder extends FlexibleViewHolder {

            public TextView stopNameTextView;

            public ChooseStopItemViewHolder(View view, FlexibleAdapter adapter) {
                super(view, adapter, false);

                stopNameTextView = (TextView) view.findViewById(R.id.stopNameText);
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
