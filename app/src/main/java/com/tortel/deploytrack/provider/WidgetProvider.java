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
package com.tortel.deploytrack.provider;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.tortel.deploytrack.Analytics;
import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.Prefs;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.*;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Class that manages updating the widgets
 */
public class WidgetProvider extends AppWidgetProvider {
    public static final String UPDATE_INTENT = "com.tortel.deploytrack.WIDGET_UPDATE";
    public static final String KEY_SCREENSHOT_MODE = "screenshot";
    
    private static final int DEFAULT_SIZE = 190;
    private static final float PADDING = 0.5f;

    private static final int SCREENSHOT_TIMEOUT = 3;
    private static final int MILIS_PER_MIN = 60000;

    private boolean mScreenShotMode = false;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("Got intent: "+intent.toString());
        Log.v("Intent action: "+intent.getAction());
        // The standard widget intents are handled in the super call
        if(UPDATE_INTENT.equals(intent.getAction())) {
            // Check if the database needs to be upgraded
            if(DatabaseUpgrader.needsUpgrade(context)){
                DatabaseUpgrader.doDatabaseUpgrade(context);
            }
            mScreenShotMode = intent.getBooleanExtra(KEY_SCREENSHOT_MODE, false);
            Log.v("Update intent received");
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
            onUpdate(context, widgetManager, new int[0]);

            return;
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds){
        // Check if the database needs to be upgraded
        if(DatabaseUpgrader.needsUpgrade(context)){
            DatabaseUpgrader.doDatabaseUpgrade(context);
        }

        updateAllWidgets(context, appWidgetManager);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(mScreenShotMode){
            // Time screenshot mode out in 3 min
            Intent cancelScreenShotMode = new Intent(UPDATE_INTENT);
            cancelScreenShotMode.putExtra(KEY_SCREENSHOT_MODE, false);

            PendingIntent screenshotPending = PendingIntent.getBroadcast(context, 0,
                    cancelScreenShotMode, PendingIntent.FLAG_CANCEL_CURRENT);

            long triggerTime = new Date().getTime() + SCREENSHOT_TIMEOUT * MILIS_PER_MIN;

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
                alarmManager.setExact(AlarmManager.RTC, triggerTime, screenshotPending);
            } else {
                alarmManager.set(AlarmManager.RTC, triggerTime, screenshotPending);
            }
        } else {
            //Schedule an update at midnight
            DateTime now = new DateTime();
            DateTime tomorrow = new DateTime(now.plusDays(1)).withTimeAtStartOfDay();

            PendingIntent pending = PendingIntent.getBroadcast(context, 0,
                    new Intent(UPDATE_INTENT), PendingIntent.FLAG_CANCEL_CURRENT);

            //Adding 100msec to make sure its triggered after midnight
            Log.d("Scheduling update for "+tomorrow.getMillis() + 100);

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
                alarmManager.setExact(AlarmManager.RTC, tomorrow.getMillis() + 100, pending);
            } else {
                alarmManager.set(AlarmManager.RTC, tomorrow.getMillis() + 100, pending);
            }

        }
    }

    /**
     * Update all widgets in the database
     * @param context
     * @param appWidgetManager
     */
    private void updateAllWidgets(Context context, AppWidgetManager appWidgetManager){
        List<WidgetInfo> infoList = DatabaseManager.getInstance(context).getAllWidgetInfo();

        // Log how many widgets there are
        FirebaseAnalytics.getInstance(context)
                .setUserProperty(Analytics.PROPERTY_WIDGET_COUNT, ""+infoList.size());

        for(WidgetInfo info : infoList){
            int widgetId = info.getId();

            Log.d("Updating widget "+widgetId);

            Log.d("Widget "+info.getId()+" with deployment "+info.getDeployment().getUuid());

            //Draw everything
            RemoteViews remoteViews = updateWidgetView(context, info, mScreenShotMode);

            //Update it
            try{
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            } catch(Exception e){
                /*
                 * Catching all exceptions, because I suspect that if a widget has been deleted,
                 * yet not removed from the database, it will still try to update it and probably cause
                 * some sort of exception. So Ill just go ahead and keep the app from crashing.
                 */
                Log.e("Uhoh!",e);

                // Show the error view
                showErrorView(context, appWidgetManager, widgetId);
            }
        }
    }

    /**
     * Show the error view for the widget ID
     * @param context
     * @param appWidgetManager
     * @param widgetId
     */
    private void showErrorView(Context context, AppWidgetManager appWidgetManager, int widgetId){
        // Show the error message
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_error);
        try {
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        } catch(Exception e){
            // Ohwell. We tried
            Log.e("Uhoh showing error view", e);
        }
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onAppWidgetOptionsChanged(Context context,
            AppWidgetManager appWidgetManager, int appWidgetId,
            Bundle newOptions) {
        DatabaseManager db = DatabaseManager.getInstance(context);
        WidgetInfo widgetInfo = db.getWidgetInfo(appWidgetId);
        // Bail if the info is null
        if(widgetInfo == null) {
            return;
        }
        
        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
        
        Log.v("Widget size: "+minWidth+"/"+maxWidth+" W, "+minHeight+"/"+maxHeight+"H");
        
        widgetInfo.setMinWidth(minWidth);
        widgetInfo.setMaxWidth(maxWidth);
        widgetInfo.setMinHeight(minHeight);
        widgetInfo.setMaxHeight(maxHeight);
        
        // Save it
        db.saveWidgetInfo(widgetInfo);
        
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
                newOptions);
    }
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        //Remove them from the database
        for(int id: appWidgetIds){
            DatabaseManager.getInstance(context).deleteWidgetInfo(id);
        }
        
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * Sets up and fills the RemoteViews with the data provided in the WidgetInfo class
     */
    public static RemoteViews updateWidgetView(Context context, WidgetInfo info){
        return updateWidgetView(context, info, false);
    }

    /**
     * Sets up and fills the RemoteViews with the data provided in the WidgetInfo class
     */
    public static RemoteViews updateWidgetView(Context context,
                                               WidgetInfo info, boolean screenShotMode){
        Deployment deployment = info.getDeployment();
        Resources resources = context.getResources();
        Prefs.load(context);

        Log.v("Updating widget info for "+deployment);

        // Check for a null name and no percentage - it probably means that the deployment was deleted, but
        // the widgetinfo object is still around
        if(deployment.getName() == null && deployment.getPercentage() == 0){
            return new RemoteViews(context.getPackageName(), R.layout.widget_error);
        }

        RemoteViews remoteViews;

        if(info.isWide()){
            Log.v("Using wide layout");
            remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_wide_layout);
        } else {
            remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
        }

        if(screenShotMode){
            // If screen shot mode is enabled, hide the details
            Log.v("Screen shot mode enabled, hiding detail views");
            remoteViews.setViewVisibility(R.id.widget_percent, View.GONE);
            remoteViews.setViewVisibility(R.id.widget_name, View.GONE);
            remoteViews.setViewVisibility(R.id.widget_info, View.GONE);
        } else {
            // Set the text
            remoteViews.setTextViewText(R.id.widget_percent, deployment.getPercentage() + "%");
            remoteViews.setTextViewText(R.id.widget_name, deployment.getName());
            remoteViews.setTextViewText(
                    R.id.widget_info,
                    resources.getQuantityString(R.plurals.days_remaining,
                            deployment.getRemaining(), deployment.getRemaining()));

            remoteViews.setViewVisibility(R.id.widget_name, View.VISIBLE);

            // Apply hide preferences
            if(Prefs.hideDate()){
                remoteViews.setViewVisibility(R.id.widget_info, View.GONE);
            } else {
                remoteViews.setViewVisibility(R.id.widget_info, View.VISIBLE);
            }

            if(Prefs.hidePercent()){
                remoteViews.setViewVisibility(R.id.widget_percent, View.GONE);
            } else {
                remoteViews.setViewVisibility(R.id.widget_percent, View.VISIBLE);
            }
        }
        
        int size = DEFAULT_SIZE;
        if(info.getMinHeight() > 0){
            size = info.getMaxWidth();
            Log.v("Using chart size "+size);
        }
        
        remoteViews.setImageViewBitmap(R.id.widget_pie, getChartBitmap(deployment, size));
        
        // Apply text color
        if(info.isLightText()){
            remoteViews.setTextColor(R.id.widget_info, Color.LTGRAY);
            remoteViews.setTextColor(R.id.widget_name, Color.LTGRAY);
            remoteViews.setTextColor(R.id.widget_percent, Color.LTGRAY);
        } else {
            remoteViews.setTextColor(R.id.widget_info, Color.DKGRAY);
            remoteViews.setTextColor(R.id.widget_name, Color.DKGRAY);
            remoteViews.setTextColor(R.id.widget_percent, Color.DKGRAY);
        }
        
        // Register an onClickListener
        Intent intent = new Intent(context, WidgetProvider.class);

        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int array[] = new int[1];
        array[0] = info.getId();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, array);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews
                .setOnClickPendingIntent(R.id.widget_pie, pendingIntent);
        
        return remoteViews;
    }
    
    public static Bitmap getChartBitmap(Deployment deployment, int size){
    	//Set up the pie chart image
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(size,size,conf);
        Canvas canvas = new Canvas(bmp);
    	
    	canvas.drawColor(Color.TRANSPARENT);
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		Path p = new Path();
		
		RectF rect = new RectF();
		Region region = new Region();
		
		//Reset variables
	    float currentAngle = 270;
	    float currentSweep;
	    float totalLength = deployment.getCompleted() + deployment.getRemaining();
	    int thickness = size / 3;
		
		float midX = size / 2f;
		float midY = size / 2f;
		float radius;
		if (midX < midY){
			radius = midX;
		} else {
			radius = midY;
		}
		radius -= PADDING;
		float innerRadius = radius - thickness;
		
		// Draw completed
		if(deployment.getCompleted() > 0){
			p.reset();
			paint.setColor(deployment.getCompletedColor());
			currentSweep = (deployment.getCompleted() / totalLength)*(360);
			rect.set(midX-radius, midY-radius, midX+radius, midY+radius);
			p.arcTo(rect, currentAngle+PADDING, currentSweep - PADDING);
			
			rect.set(midX-innerRadius, midY-innerRadius, midX+innerRadius, midY+innerRadius);
			p.arcTo(rect, (currentAngle+PADDING) + (currentSweep - PADDING), -(currentSweep-PADDING));
			p.close();
			
			region.set((int)(midX-radius), (int)(midY-radius), (int)(midX+radius), (int)(midY+radius));
			canvas.drawPath(p, paint);
			
			currentAngle = currentAngle+currentSweep;
		}
		
		// Draw remaining
		if(deployment.getCompleted() > 0){
			p.reset();
			paint.setColor(deployment.getRemainingColor());
			currentSweep = (deployment.getRemaining() / totalLength)*(360);
			rect.set(midX-radius, midY-radius, midX+radius, midY+radius);
			p.arcTo(rect, currentAngle+PADDING, currentSweep - PADDING);
			
			rect.set(midX-innerRadius, midY-innerRadius, midX+innerRadius, midY+innerRadius);
			p.arcTo(rect, (currentAngle+PADDING) + (currentSweep - PADDING), -(currentSweep-PADDING));
			p.close();
			
			region.set((int)(midX-radius), (int)(midY-radius), (int)(midX+radius), (int)(midY+radius));
			canvas.drawPath(p, paint);
		}

		return bmp;
    }

}
