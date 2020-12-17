/*
 * Copyright (C) 2020 Scott Warner
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
package com.tortel.deploytrack.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.tortel.deploytrack.Analytics;
import com.tortel.deploytrack.DeploymentFragmentAdapter;
import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.Prefs;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.SettingsActivity;
import com.tortel.deploytrack.SyncSetupActivity;
import com.tortel.deploytrack.data.DatabaseManager;
import com.tortel.deploytrack.data.DatabaseUpgrader;
import com.tortel.deploytrack.dialog.DatabaseUpgradeDialog;
import com.tortel.deploytrack.dialog.DeleteDialog;
import com.tortel.deploytrack.dialog.ScreenShotModeDialog;
import com.tortel.deploytrack.dialog.WelcomeDialog;
import com.tortel.deploytrack.provider.WidgetProvider;

import java.util.List;

/**
 * Main fragment that displays the charts and such
 */
public class MainFragment extends Fragment {
    private static final String KEY_POSITION = "position";
    private static final String KEY_SCREENSHOT = "screenshot";

    private FirebaseAnalytics mFirebaseAnalytics;
    private DeploymentFragmentAdapter mAdapter;

    private int mCurrentPosition;
    private boolean mScreenShotMode = false;
    private TabLayout mTabLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        if(savedInstanceState != null){
            mCurrentPosition = savedInstanceState.getInt(KEY_POSITION);
            mScreenShotMode = savedInstanceState.getBoolean(KEY_SCREENSHOT, false);
            if(mScreenShotMode){
                Prefs.setScreenShotMode(true, getContext());
            }
        } else {
            // Check if we need to upgrade the database
            if (DatabaseUpgrader.needsUpgrade(getContext())) {
                DatabaseUpgradeDialog upgradeDialog = new DatabaseUpgradeDialog();
                upgradeDialog.show(getParentFragmentManager(), "upgrade");
            } else {
                // Only show the welcome dialog if its the first time the app is opened,
                // and the DB doesn't need to be upgraded
                if (!Prefs.isWelcomeShown()) {
                    Prefs.setWelcomeShown(getContext());
                    WelcomeDialog dialog = new WelcomeDialog();
                    dialog.show(getParentFragmentManager(), "welcome");
                }
            }

            mCurrentPosition = 0;
            // Sync should only need to be set up once
            setupSync();
        }

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.registerReceiver(mChangeListener, new IntentFilter(DatabaseManager.DATA_DELETED));
        lbm.registerReceiver(mChangeListener, new IntentFilter(DatabaseManager.DATA_ADDED));
        lbm.registerReceiver(mChangeListener, new IntentFilter(DatabaseManager.DATA_CHANGED));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTabLayout != null) {
            mTabLayout.removeOnTabSelectedListener(mTabSelectedListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs.load(getContext());
        if(mScreenShotMode) {
            Prefs.setScreenShotMode(true, getContext());
        }
        reload();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, mCurrentPosition);
        outState.putBoolean(KEY_SCREENSHOT, mScreenShotMode);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.unregisterReceiver(mChangeListener);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        toolbar.setOnMenuItemClickListener((MenuItem item) -> {
            Intent intent;
            final String id = mAdapter.getId(mCurrentPosition);

            switch (item.getItemId()) {
                case R.id.menu_create_new:
                    MainFragmentDirections.MainToCreateAction createAction = MainFragmentDirections.mainToCreateAction();
                    NavHostFragment.findNavController(this).navigate(createAction);
                    return true;
                case R.id.menu_edit:
                    //If its the info fragment, ignore
                    if(id == null){
                        return true;
                    }
                    MainFragmentDirections.MainToCreateAction editAction = MainFragmentDirections.mainToCreateAction();
                    editAction.setId(id);
                    NavHostFragment.findNavController(this).navigate(editAction);
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
                    dialog.show(getParentFragmentManager(), "delete");
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
                        aboutDialog.show(getParentFragmentManager(), "screenshot");
                        Prefs.setAboutScreenShotShown(getContext());
                    }

                    mScreenShotMode = !mScreenShotMode;
                    Prefs.setScreenShotMode(mScreenShotMode, getContext());

                    // Propagate screen shot mode to the widgets
                    Intent updateWidgetIntent = new Intent(WidgetProvider.UPDATE_INTENT);
                    updateWidgetIntent.putExtra(WidgetProvider.KEY_SCREENSHOT_MODE, mScreenShotMode);
                    getActivity().sendBroadcast(updateWidgetIntent);

                    reload();
                    return true;
                case R.id.menu_settings:
                    intent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(intent);
                    return true;
            }
            return super.onOptionsItemSelected(item);
        });

        return view;
    }

    private void reload(){
        Log.v("Reloading data");
        if (mAdapter == null) {
            mAdapter = new DeploymentFragmentAdapter(getActivity(), getChildFragmentManager());
        }
        mAdapter.reloadData();

        ViewPager pager = getView().findViewById(R.id.pager);

        // Make sure that the position does not go past the end
        if (mCurrentPosition >= mAdapter.getCount()) {
            mCurrentPosition = Math.max(0, mCurrentPosition - 1);
        }

        // Re-set the adapter and position
        pager.setAdapter(mAdapter);
        pager.setCurrentItem(mCurrentPosition);

        mTabLayout = getView().findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(pager);
        mTabLayout.addOnTabSelectedListener(mTabSelectedListener);

        if(mScreenShotMode){
            mTabLayout.setVisibility(View.INVISIBLE);
        } else {
            mTabLayout.setVisibility(View.VISIBLE);
        }

        // Set the analytics properties
        setAnalyticsProperties();
    }

    /**
     * If the user is logged in, make sure that sync is set up
     */
    private void setupSync(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null){
            DatabaseManager.getInstance(getContext()).setFirebaseUser(auth.getCurrentUser());
        } else if(Prefs.isSyncEnabled(getContext())){
            // If sync is/was enabled, and no account was found, let the user know
            Snackbar.make(getView().findViewById(R.id.root), R.string.sync_account_error, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.menu_sync, view -> {
                        startActivity(new Intent(getActivity(), SyncSetupActivity.class));
                    }).show();
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
        final PackageManager mgr = getContext().getPackageManager();
        List<ResolveInfo> list = mgr.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * Class to listen for page changes.
     * The page number is used for editing and deleting data
     */
    private final TabLayout.OnTabSelectedListener mTabSelectedListener = new TabLayout.OnTabSelectedListener() {

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            mCurrentPosition = tab.getPosition();
            mAdapter.getItem(mCurrentPosition).onResume();
            Log.v("Page changed to " + mCurrentPosition);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            // Ignore
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            // Ignore
        }
    };

    private final BroadcastReceiver mChangeListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DatabaseManager.DATA_DELETED) && mAdapter != null) {
                mAdapter.deploymentDeleted(intent.getStringExtra(DeleteDialog.KEY_ID));
            }
            reload();
        }
    };
}
