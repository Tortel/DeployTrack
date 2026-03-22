/*
 * Copyright (C) 2013-2026 Scott Warner
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

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.RemoteViews;

import com.google.android.material.tabs.TabLayout;
import com.tortel.deploytrack.data.*;
import com.tortel.deploytrack.provider.WidgetProvider;

public class WidgetPickerActivity extends AppCompatActivity {
    private Intent mResultIntent;
    private AppWidgetManager mWidgetManager;
    private int mWidgetId;
    
    private DeploymentFragmentAdapter mAdapter;
    private boolean mUseLightText = true;
    
    private int mCurrentPosition = 0;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
		androidx.activity.EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        
        // Check for light theme
        Prefs.load(this);
        if (Prefs.useLightTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        
        setContentView(R.layout.activity_widget_config);

        View rootView = findViewById(R.id.rootView);

        // Apply the insets listener
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, windowInsets) -> {
            // Get the heights of the system bars (status bar, navigation bar, etc.)
            androidx.core.graphics.Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Apply the insets as padding to the view
            // This pushes the content of the view inwards so it doesn't overlap the system bars
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            // Return CONSUMED if you don't want the insets to be passed down to child views
            // Or return windowInsets if you want children to also have a chance to handle them.
            return WindowInsetsCompat.CONSUMED;
        });
        
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            mWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        mResultIntent = new Intent();
        //Set it to cancelled until explicitly told to save
        setResult(RESULT_CANCELED, mResultIntent);
        
        mWidgetManager = AppWidgetManager.getInstance(this);
        
        mAdapter = new DeploymentFragmentAdapter(this, getSupportFragmentManager());
        
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(mAdapter);

        mTabLayout = findViewById(R.id.tabs);
        pager.setCurrentItem(mCurrentPosition);
        mTabLayout.addOnTabSelectedListener(mTabSelectedListener);
        
        Log.d("WidgetPicker started with mWidgetId "+ mWidgetId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTabLayout != null) {
            mTabLayout.removeOnTabSelectedListener(mTabSelectedListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTabLayout != null) {
            mTabLayout.addOnTabSelectedListener(mTabSelectedListener);
        }
    }

    public void onClick(View v) {
        String id = mAdapter.getId(mCurrentPosition);

        if (v.getId() == R.id.button_save) {
            if (id != null) {
                DatabaseManager db = DatabaseManager.getInstance(this);

                //Get the data
                Deployment deployment = db.getDeployment(id);

                //Save it
                WidgetInfo info = new WidgetInfo(mWidgetId, deployment);
                info.setLightText(mUseLightText);
                db.saveWidgetInfo(info);

                //Set it all up
                RemoteViews remoteView = WidgetProvider.updateWidgetView(this, info);

                mWidgetManager.updateAppWidget(mWidgetId, remoteView);
                mResultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
                setResult(RESULT_OK, mResultIntent);

                Log.d("WidgetPicker ending for widgetId " + mWidgetId
                        + " with deployment " + id);
            }
            finish();
        } else if (v.getId() == R.id.button_cancel) {
            finish();
        } else if (v.getId() == R.id.widget_dark_text) {
            mUseLightText = false;
        } else if (v.getId() == R.id.widget_light_text) {
            mUseLightText = true;
        }
    }
    
    /**
     * Class to listen for page changes.
     * The page number is used for editing and deleting data
     */
    private TabLayout.OnTabSelectedListener mTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            mCurrentPosition = tab.getPosition();
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
}
