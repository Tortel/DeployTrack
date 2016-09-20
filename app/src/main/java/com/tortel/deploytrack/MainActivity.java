/*
 * Copyright (C) 2013-2016 Scott Warner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tortel.deploytrack;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.data.DatabaseUpgrader;
import com.tortel.deploytrack.dialog.AboutDialog;
import com.tortel.deploytrack.dialog.DatabaseUpgradeDialog;
import com.tortel.deploytrack.dialog.DeleteDialog;
import com.tortel.deploytrack.dialog.ScreenShotModeDialog;
import com.tortel.deploytrack.dialog.WelcomeDialog;
import com.tortel.deploytrack.provider.WidgetProvider;

/**
 * The main activity that contains the fragments that show the graphs.
 * Also handles the options menu
 */
public class MainActivity extends AppCompatActivity {
	private static final String KEY_POSITION = "position";
    private static final String KEY_SCREENSHOT = "screenshot";
	
	private DeploymentFragmentAdapter mAdapter;
	private FirebaseAnalytics mFirebaseAnalytics;
	
	private int mCurrentPosition;
    private boolean mScreenShotMode = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        // Check for light theme
        Prefs.load(this);
        if(Prefs.useLightTheme()){
            setTheme(R.style.Theme_DeployThemeLight);
        }
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
		
		if(savedInstanceState != null){
			mCurrentPosition = savedInstanceState.getInt(KEY_POSITION);
            mScreenShotMode = savedInstanceState.getBoolean(KEY_SCREENSHOT, false);
            if(mScreenShotMode){
                Prefs.setScreenShotMode(true, this);
            }
		} else {
			// Check if we need to upgrade the database
			if(DatabaseUpgrader.needsUpgrade(this)){
				DatabaseUpgradeDialog upgradeDialog = new DatabaseUpgradeDialog();
				upgradeDialog.show(getSupportFragmentManager(), "upgrade");
			} else {
				// Only show the welcome dialog if its the first time the app is opened,
				// and the DB doesn't need to be upgraded
				if(!Prefs.isWelcomeShown()){
					Prefs.setWelcomeShown(this);
					WelcomeDialog dialog = new WelcomeDialog();
					dialog.show(getSupportFragmentManager(), "welcome");
				}
			}

			mCurrentPosition = 0;
			// Sync should only need to be set up once
			setupSync();

			// Log the event
			mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);
		}

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        lbm.registerReceiver(mChangeListener, new IntentFilter(DatabaseManager.DATA_DELETED));
		lbm.registerReceiver(mChangeListener, new IntentFilter(DatabaseManager.DATA_ADDED));
		lbm.registerReceiver(mChangeListener, new IntentFilter(DatabaseManager.DATA_CHANGED));
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        lbm.unregisterReceiver(mChangeListener);
    }

    @Override
	public void onResume(){
		super.onResume();
        Prefs.load(this);
        if(mScreenShotMode) {
            Prefs.setScreenShotMode(true, this);
        }
		reload();
	}
	
	private void reload(){
		Log.v("Reloading activity");
		mAdapter = new DeploymentFragmentAdapter(this, getSupportFragmentManager());

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(mAdapter);

        PagerSlidingTabStrip indicator = (PagerSlidingTabStrip) findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		indicator.setOnPageChangeListener(new PageChangeListener());

        // Set the tab text color
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.tabTextColor, typedValue, true);
        int color = typedValue.data;
        indicator.setTextColor(color);
		
		pager.setCurrentItem(mCurrentPosition);
		indicator.notifyDataSetChanged();

        if(mScreenShotMode){
            indicator.setVisibility(View.INVISIBLE);
        } else {
            indicator.setVisibility(View.VISIBLE);
        }

		// Set the analytics properties
		setAnalyticsProperties();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		final String id = mAdapter.getId(mCurrentPosition);
		
		switch (item.getItemId()) {
		case R.id.menu_create_new:
			intent = new Intent(this, CreateActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_edit:
			//If its the info fragment, ignore
			if(id == null){
				return true;
			}
			intent = new Intent(this, CreateActivity.class);
			intent.putExtra("id", id);
			startActivity(intent);
			return true;
		case R.id.menu_delete:
			//If its the info fragment, ignore
			if(id == null){
				return true;
			}
            DeleteDialog dialog = new DeleteDialog();
            Bundle args = new Bundle();
            args.putString(DeleteDialog.KEY_ID, id);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "delete");
			return true;
		case R.id.menu_about:
			Fragment about = new AboutDialog();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(about, "about");
            transaction.commit();
			return true;
		case R.id.menu_feedback:
		    intent = new Intent(Intent.ACTION_SEND);
		    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"Swarner.dev@gmail.com"});
		    intent.putExtra(Intent.EXTRA_SUBJECT, "Deployment Tracker Feedback");
		    intent.setType("plain/text");
		    if(isAvailable(intent)){
		        startActivity(intent);
		    }
		    return true;
        case R.id.menu_screenshot:
            if(!Prefs.isAboutScreenShotShown()){
                ScreenShotModeDialog aboutDialog = new ScreenShotModeDialog();
                aboutDialog.show(getSupportFragmentManager(), "screenshot");
                Prefs.setAboutScreenShotShown(getApplicationContext());
            }

            mScreenShotMode = !mScreenShotMode;
            Prefs.setScreenShotMode(mScreenShotMode, getApplicationContext());

            // Propagate screen shot mode to the widgets
            Intent updateWidgetIntent = new Intent(WidgetProvider.UPDATE_INTENT);
            updateWidgetIntent.putExtra(WidgetProvider.KEY_SCREENSHOT_MODE, mScreenShotMode);
            sendBroadcast(updateWidgetIntent);

            reload();
            return true;
		case R.id.menu_settings:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_sync:
            intent = new Intent(this, SyncSetupActivity.class);
            startActivity(intent);
            return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * If the user is logged in, make sure that sync is set up
	 */
	private void setupSync(){
		FirebaseAuth auth = FirebaseAuth.getInstance();
		if(auth.getCurrentUser() != null){
			DatabaseManager.getInstance(this).setFirebaseUser(auth.getCurrentUser());
		}
	}

	/**
	 * Set the various analytics properties
	 */
	private void setAnalyticsProperties(){
		// Record the number of deployments
		if(mAdapter != null) {
			mFirebaseAnalytics.setUserProperty(Analytics.PROPERTY_DEPLOYMENT_COUNT, "" + mAdapter.getCount());
		}

		Analytics.recordPreferences(mFirebaseAnalytics, mScreenShotMode);
	}

	/**
	 * Check if there is an app available to handle an intent
     */
    private boolean isAvailable(Intent intent) {
        final PackageManager mgr = getPackageManager();
        List<ResolveInfo> list = mgr.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_POSITION, mCurrentPosition);
        outState.putBoolean(KEY_SCREENSHOT, mScreenShotMode);
	}

	/**
	 * Class to listen for page changes.
	 * The page number is used for editing and deleting data
	 */
	private class PageChangeListener implements ViewPager.OnPageChangeListener{
		@Override
		public void onPageSelected(int position) {
			mCurrentPosition = position;
			mAdapter.getItem(mCurrentPosition).onResume();
			Log.v("Page changed to "+position);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			//Ignore
		}
		@Override
		public void onPageScrollStateChanged(int state) {
			//Ignore
		}
	}

    private BroadcastReceiver mChangeListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reload();
        }
    };
}
