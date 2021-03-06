package com.ianorourke.fdrflightrecorder;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ianorourke.fdrflightrecorder.Fragments.AircraftFragment;
import com.ianorourke.fdrflightrecorder.Fragments.RecordNewFlight;
import com.ianorourke.fdrflightrecorder.Fragments.PrefsFragment;
import com.ianorourke.fdrflightrecorder.Fragments.RecordedFlightsFragment;

public class MainActivity extends AppCompatActivity {
    private String[] mNavigationActions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private LinearLayout mLinearLayout;
    private Fragment mCurrentFragment;

        private FragmentManager mFragmentManager;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            mLinearLayout = (LinearLayout) findViewById(R.id.main_layout);
            mFragmentManager = getSupportFragmentManager();

        addDrawerItems();
        setupDrawer();

        try {
            ActionBar ab = getSupportActionBar();
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            // Do Nothing
        }

        Log.v("FDR", "Child Count: " + mLinearLayout.getChildCount());

        if (mCurrentFragment == null)
            setPosition(getResources().getInteger(R.integer.nav_aircraft));
    }

    private void addDrawerItems() {
        mNavigationActions = getResources().getStringArray(R.array.navigation_actions);
        Log.v("FDR", mNavigationActions[getResources().getInteger(R.integer.nav_aircraft)]);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mNavigationActions));

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setPosition(position);
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });
    }

    private void setPosition(int position) {
        Fragment newFragment = null;

        if (position == getResources().getInteger(R.integer.nav_aircraft)) {
            if (mCurrentFragment == null || mCurrentFragment.getClass() != AircraftFragment.class)
                newFragment = new AircraftFragment();
        } else if (position == getResources().getInteger(R.integer.nav_recorded)) {
            if (mCurrentFragment == null || mCurrentFragment.getClass() != RecordedFlightsFragment.class)
                newFragment = new RecordedFlightsFragment();
        } else if (position == getResources().getInteger(R.integer.nav_new)) {
            if (mCurrentFragment == null || mCurrentFragment.getClass() != RecordNewFlight.class)
                newFragment = new RecordNewFlight();
        } else if (position == getResources().getInteger(R.integer.nav_settings)) {
            if (mCurrentFragment == null || mCurrentFragment.getClass() != PrefsFragment.class)
                newFragment = new PrefsFragment();
        }

        if (newFragment != null) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.replace(mLinearLayout.getId(), newFragment);
            ft.commit();

            try {
                ActionBar ab = getSupportActionBar();
                ab.setTitle(mNavigationActions[position]);
            } catch (NullPointerException e) {
                // Do Nothing
            }

            mCurrentFragment = newFragment;
        }
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //int id = item.getItemId();

        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
