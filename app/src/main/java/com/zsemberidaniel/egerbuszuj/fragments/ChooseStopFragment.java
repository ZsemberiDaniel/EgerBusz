package com.zsemberidaniel.egerbuszuj.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zsemberidaniel.egerbuszuj.R;
import com.zsemberidaniel.egerbuszuj.adapters.ChooseStopAdapter;
import com.zsemberidaniel.egerbuszuj.realm.objects.Stop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by zsemberi.daniel on 2017. 05. 12..
 */

public class ChooseStopFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private FlexibleAdapter<ChooseStopAdapter.ChooseStopItem> allStopsAdapter;
    private TreeSet<ChooseStopAdapter.ChooseStopItem> allStopItems;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_stop, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.chooseStopRecyclerView);

        // improve performance because the content does not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter with all of the stops
        RealmResults<Stop> allStops = Realm.getDefaultInstance().where(Stop.class).findAll();

        // Get letter headers
        HashMap<Character, ChooseStopAdapter.ChooseStopHeader> headers =
                ChooseStopAdapter.getNewHeaders(allStops);

        // Get the items in the adapter
        allStopItems = ChooseStopAdapter.convertToChooseStopItems(allStops, headers);

        allStopsAdapter = new ChooseStopAdapter(new ArrayList<>(allStopItems));
        allStopsAdapter.setDisplayHeadersAtStartUp(true);
        allStopsAdapter.setStickyHeaders(true);

        allStopsAdapter.addListener(new FlexibleAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(int position) {
                ChooseStopAdapter.ChooseStopItem item = allStopsAdapter.getItem(position);


                return false;
            }
        });

        recyclerView.setAdapter(allStopsAdapter);

        return view;
    }

    public void updateStopFilter(String newFilter) {
        allStopsAdapter.setSearchText(newFilter);

        // Disable headers if the user is searching because they are not really needed
        // And if enabled they get doubled for some reason
        if (!newFilter.equals(""))
            allStopsAdapter.hideAllHeaders();
        else
            allStopsAdapter.showAllHeaders();

        allStopsAdapter.filterItems(new ArrayList<>(allStopItems));
    }
}
