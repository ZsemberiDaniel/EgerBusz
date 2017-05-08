package rpc.zsemberidaniel.com.egerbusz.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import rpc.zsemberidaniel.com.egerbusz.R;
import rpc.zsemberidaniel.com.egerbusz.data.Line;
import rpc.zsemberidaniel.com.egerbusz.data.Lines;
import rpc.zsemberidaniel.com.egerbusz.data.Stations;
import rpc.zsemberidaniel.com.egerbusz.data.StringAdapter;

/**
 * Created by zsemberi.daniel on 2017. 05. 04..
 */

public class ChooseBusFragment extends Fragment {

    private Spinner lineSpinner;
    private ListView stationListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_bus, container, false);

        lineSpinner = (Spinner) view.findViewById(R.id.lineSpinner);
        stationListView = (ListView) view.findViewById(R.id.stationListView);

        // SPINNER
        // We need a new list because we want to add the option of None next to all of the lines
        ArrayList<String> lineOptions = new ArrayList<>();
        for (Line line : Lines.getLines()) lineOptions.add(line.getId());
        lineOptions.add(0, getResources().getString(R.string.NoneText));

        // Make adapter for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, lineOptions);
        lineSpinner.setAdapter(adapter);

        lineSpinner.setOnItemSelectedListener(new LineSpinnerSelectedListener());

        UpdateListView();

        return view;
    }

    private void UpdateListView() {
        Line chosenLine = Lines.getLine(lineSpinner.getSelectedItem().toString());
        List<String> stationNames;

        // No line was chosen
        if (chosenLine == null) {
            stationNames = Stations.getStationNamesInABCOrder();
        } else { // We can use the line that was chosen
            stationNames = Arrays.asList(chosenLine.getStationNames());
        }

        stationListView.setAdapter(new StringAdapter(getActivity(), stationNames));
    }

    /**
     * Click listener for the line spinner
     */
    private class LineSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {


        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            UpdateListView();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}