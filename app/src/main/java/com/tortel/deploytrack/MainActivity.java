/*
 * Copyright (C) 2013-2014 Scott Warner
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.fragments.AboutDialogFragment;
import com.tortel.deploytrack.service.NotificationService;

/**
 * The main activity that contains the fragments that show the graphs.
 * Also handles the options menu
 */
public class MainActivity extends ActionBarActivity {
	private static final String KEY_POSITION = "position";
    private static final String KEY_SCREENSHOT = "screenshot";
	
	private DeploymentFragmentAdapter mAdapter;
	
	private int mCurrentPosition;
    private boolean mScreenshotMode = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        // Check for light theme
        Prefs.load(this);
        if(Prefs.useLightTheme()){
            setTheme(R.style.Theme_DeployThemeLight);
        }

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		if(savedInstanceState != null){
			mCurrentPosition = savedInstanceState.getInt(KEY_POSITION);
            mScreenshotMode = savedInstanceState.getBoolean(KEY_SCREENSHOT, false);
            if(mScreenshotMode){
                Prefs.setScreenshotMode(mScreenshotMode, getApplicationContext());
            }
		} else {
			mCurrentPosition = 0;
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Prefs.load(this);
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		final int id = mAdapter.getId(mCurrentPosition);
		
		switch (item.getItemId()) {
		case R.id.menu_create_new:
			intent = new Intent(this, CreateActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_edit:
			//If its the info fragment, ignore
			if(id == -1){
				return true;
			}
			intent = new Intent(this, CreateActivity.class);
			intent.putExtra("id", id);
			startActivity(intent);
			return true;
		case R.id.menu_delete:
			//If its the info fragment, ignore
			if(id == -1){
				return true;
			}
			//show a nice confirmation dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.confirm);
			builder.setTitle(R.string.delete);
			builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.v("Deleting "+id);
					//Delete it
					DatabaseManager.getInstance(getApplicationContext()).deleteDeployment(id);
					reload();
				}
			});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// No op
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
		case R.id.menu_about:
			Fragment about = new AboutDialogFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(about, "about");
            transaction.commit();
			return true;
		case R.id.menu_notification_show:
			if(id == -1){
				return true;
			}
			NotificationService.setSavedId(this, id);
			intent = new Intent(this, NotificationService.class);
			startService(intent);
			return true;
		case R.id.menu_notification_hide:
			NotificationService.setSavedId(this, -1);
			intent = new Intent(this, NotificationService.class);
			stopService(intent);
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
            mScreenshotMode = !mScreenshotMode;
            Prefs.setScreenshotMode(mScreenshotMode, getApplicationContext());
            reload();
            return true;
		case R.id.menu_settings:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
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
        outState.putBoolean(KEY_SCREENSHOT, mScreenshotMode);
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
}