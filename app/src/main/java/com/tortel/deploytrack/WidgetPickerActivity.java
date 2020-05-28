/*
 * Copyright (C) 2013-2020 Scott Warner
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
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.RemoteViews;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.tortel.deploytrack.data.*;
import com.tortel.deploytrack.provider.WidgetProvider;

public class WidgetPickerActivity extends AppCompatActivity {
    private Intent mResultIntent;
    private AppWidgetManager mWidgetManager;
    private int mWidgetId;
    
    private DeploymentFragmentAdapter mAdapter;
    private boolean mUseLightText = true;
    
    private int mCurrentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check for light theme
        Prefs.load(this);
        if(Prefs.useLightTheme()){
            setTheme(R.style.Theme_DeployThemeLight);
        }
        
        setContentView(R.layout.activity_widget_config);
        
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            mWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        mResultIntent = new Intent();
        //Set it to cancelled until explicitly told to save
        setResult(RESULT_CANCELED, mResultIntent);
        
        mWidgetManager = AppWidgetManager.getInstance(this);
        
        mAdapter = new DeploymentFragmentAdapter(this, getSupportFragmentManager());
        
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(mAdapter);

        SmartTabLayout indicator = (SmartTabLayout) findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(new PageChangeListener());
        
        pager.setCurrentItem(mCurrentPosition);
        
        Log.d("WidgetPicker started with mWidgetId "+ mWidgetId);
    }
    
    public void onClick(View v){
        String id = mAdapter.getId(mCurrentPosition);
        switch(v.getId()){
        case R.id.button_save:
            if(id != null){
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
                
                Log.d("WidgetPicker ending for widgetId "+ mWidgetId
                        +" with deployment "+id);
            }
            finish();
            break;
        case R.id.button_cancel:
            finish();
            break;
        case R.id.widget_dark_text:
            mUseLightText = false;
            break;
        case R.id.widget_light_text:
            mUseLightText = true;
            break;
        }
    }
    
    /**
     * Class to listen for page changes.
     * The page number is used for editing and deleting data
     */
    private class PageChangeListener implements ViewPager.OnPageChangeListener{
        @Override
        public void onPageSelected(int position) {
            mCurrentPosition = position;
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
