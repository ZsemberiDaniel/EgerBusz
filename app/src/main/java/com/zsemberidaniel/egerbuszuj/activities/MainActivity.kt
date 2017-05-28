package com.zsemberidaniel.egerbuszuj.activities

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem

import com.miguelcatalan.materialsearchview.MaterialSearchView
import com.zsemberidaniel.egerbuszuj.R
import com.zsemberidaniel.egerbuszuj.fragments.BothRouteFragment
import com.zsemberidaniel.egerbuszuj.misc.TodayType
import com.zsemberidaniel.egerbuszuj.fragments.ChooseStopFragment
import com.zsemberidaniel.egerbuszuj.realm.FileToRealm

import net.danlew.android.joda.JodaTimeAndroid

import io.realm.Realm

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var toolbar: Toolbar
    private lateinit var searchView: MaterialSearchView

    /**
     * The search menu item, which we need to be disabled when a route is selected
     */
    private var searchMenuItem: MenuItem? = null

    /**
     * Adapter for the viewPager
     */
    private lateinit var mainPagerAdapter: MainPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        JodaTimeAndroid.init(this)
        Realm.init(this)
        // TODO put these in some kind of load stuff
        TodayType.init(this)
        FileToRealm.init(this)

        viewPager = findViewById(R.id.mainViewPager) as ViewPager
        tabLayout = findViewById(R.id.bothRouteTabLayout) as TabLayout
        mainPagerAdapter = MainPagerAdapter(supportFragmentManager)
        viewPager.adapter = mainPagerAdapter

        tabLayout.setupWithViewPager(viewPager)

        toolbar = findViewById(R.id.mainToolbar) as Toolbar
        setSupportActionBar(toolbar)

        searchView = findViewById(R.id.search_view) as MaterialSearchView
        searchView.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                mainPagerAdapter.chooseStopFragment?.updateStopFilter(newText)

                return true
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_all_stops, menu)

        searchMenuItem = menu.findItem(R.id.actionSearch)
        searchView.setMenuItem(searchMenuItem)

        return super.onCreateOptionsMenu(menu)
    }

    private inner class MainPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        var chooseStopFragment: ChooseStopFragment? = null
            private set

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment? {
            when (position) {
                0 -> return ChooseStopFragment()
                1 -> return BothRouteFragment()
                else -> return null
            }
        }
    }
}
