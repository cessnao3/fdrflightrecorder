package com.ianorourke.fdrflightrecorder;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

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
        mFragmentManager = getFragmentManager();

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Log.v("FDR", "Child Count: " + mLinearLayout.getChildCount());

        if (mCurrentFragment == null)
            setPosition(getResources().getInteger(R.integer.nav_pilots));
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
        String[] navArray = getResources().getStringArray(R.array.navigation_actions);
        getSupportActionBar().setTitle(navArray[position]);

        Fragment newFragment = null;

        if (position == getResources().getInteger(R.integer.nav_pilots)) {
            if (mCurrentFragment == null || mCurrentFragment.getClass() != PilotFragment.class)
                newFragment = new PilotFragment();
        } else if (position == getResources().getInteger(R.integer.nav_aircraft)) {
            if (mCurrentFragment == null || mCurrentFragment.getClass() != AircraftFragment.class)
                newFragment = new AircraftFragment();
        }

        if (newFragment != null) {
            FragmentTransaction ft = mFragmentManager.beginTransaction();
            ft.replace(mLinearLayout.getId(), newFragment);
            ft.commit();

            mCurrentFragment = newFragment;
        }

        if (position == getResources().getInteger(R.integer.nav_new))
            startActivity(new Intent(MainActivity.this, MapActivity.class));
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
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */

        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}