package rpc.zsemberidaniel.com.egerbusz.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

import rpc.zsemberidaniel.com.egerbusz.R;
import rpc.zsemberidaniel.com.egerbusz.data.GTFS;

/**
 * Created by zsemberi.daniel on 2017. 05. 09..
 */

public class ChooseBusBothWaysFragment extends Fragment {

    private Spinner lineSpinner;
    private TabLayout tabLayout;
    private FragmentPagerAdapter fragmentPagerAdapter;

    /**
     * Stores both fragments which are created for the viewpager
     */
    private ChooseBusFragment[] chooseBusFragments = new ChooseBusFragment[2];

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_bus_both_ways, container, false);

        ViewPager viewPager = (ViewPager) view.findViewById(R.id.chooseBusBothWaysViewPager);
        lineSpinner = (Spinner) view.findViewById(R.id.lineSpinner);
        tabLayout = (TabLayout) view.findViewById(R.id.chooseBusBothWaysTabLayout);

        // VIEW PAGER
        fragmentPagerAdapter = new BusBothWaysPagerAdapter(getFragmentManager());
        viewPager.setAdapter(fragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager, true);

        // SPINNER
        // We need a new list because we want to add the option of None next to all of the lines
        ArrayList<String> lineOptions = new ArrayList<>();
        // Get routes from the database
        Cursor routeCursor = GTFS.getInstance().getReadableDatabase().query(
                GTFS.RouteTable.TABLE_NAME,
                new String[] { GTFS.RouteTable.COLUMN_NAME_ID },
                null, null, null, null, null
        );
        while (routeCursor.moveToNext())
            lineOptions.add(routeCursor.getString(routeCursor.getColumnIndex(GTFS.RouteTable.COLUMN_NAME_ID)));
        routeCursor.close();

        lineOptions.add(0, getResources().getString(R.string.NoneText));

        // Make adapter for spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, lineOptions);
        lineSpinner.setAdapter(adapter);

        // listener
        lineSpinner.setOnItemSelectedListener(new LineSpinnerSelectedListener());

        // update child fragments
        updateChildFragmentLists();

        return view;
    }

    private void updateChildFragmentLists() {
        String selected = lineSpinner.getSelectedItem().toString();

        for (int i = 0; i < chooseBusFragments.length; i++)
            if (chooseBusFragments[i] != null) {
                chooseBusFragments[i].updateListView(selected);

                tabLayout.getTabAt(i).setText(chooseBusFragments[i].getHeadSignText());
            }

        if (selected.equals(getResources().getString(R.string.NoneText)))
            tabLayout.setVisibility(View.GONE);
        else
            tabLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Click listener for the line spinner
     */
    private class LineSpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            updateChildFragmentLists();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { }
    }

    private class BusBothWaysPagerAdapter extends FragmentPagerAdapter {

        public BusBothWaysPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // We need to store the fragments because friking android won't do it for us...
            // Also fuck getItems....
            ChooseBusFragment fragment = (ChooseBusFragment) super.instantiateItem(container, position);

            chooseBusFragments[position] = fragment;

            return fragment;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Fragment direction0 = new ChooseBusFragment();

                    // Set in which direction the bus goes
                    Bundle bundle0 = new Bundle();
                    bundle0.putInt(ChooseBusFragment.ARG_DIRECTION, 0);

                    direction0.setArguments(bundle0);
                    return direction0;
                case 1:
                    Fragment direction1 = new ChooseBusFragment();

                    // Set in which direction the bus goes
                    Bundle bundle1 = new Bundle();
                    bundle1.putInt(ChooseBusFragment.ARG_DIRECTION, 1);

                    direction1.setArguments(bundle1);
                    return direction1;
                default: return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (chooseBusFragments[position] == null) return "";

            return chooseBusFragments[position].getHeadSignText();
        }
    }
}