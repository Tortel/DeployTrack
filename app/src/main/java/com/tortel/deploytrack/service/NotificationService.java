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
package com.tortel.deploytrack.service;

import org.joda.time.DateTime;

import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.Prefs;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.*;
import com.tortel.deploytrack.provider.WidgetProvider;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class NotificationService extends Service {
    private static final String KEY_ID = "id";
    private static final int NOTIFICATION_ID = 1234;
    private static final boolean DEBUG = true;
    private static final int SIZE = 250;
    
    private static final String UPDATE_INTENT = "com.tortel.deploytrack.update_notification";

    private int deploymentId;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        deploymentId = getSavedId(this);
        
        //Register receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_INTENT);
        registerReceiver(updateReceiver, filter);

        Prefs.load(this);
        
        showNotification();
        
        if (DEBUG){
            Toast.makeText(this, "NotificationService onCreate", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(NOTIFICATION_ID);
        
        unregisterReceiver(updateReceiver);

        if (DEBUG){
            Toast.makeText(this, "NotificationService onDestroy", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        deploymentId = getSavedId(this);
        // Reload the notification
        showNotification();
        // Stick damn you!
        return START_STICKY;
    }

    @SuppressLint("NewApi")
    private void showNotification() {
        // If there isnt an ID saved, shut down the service
        if (deploymentId == -1) {
            stopSelf();
            return;
        }
        if (DEBUG){
            Toast.makeText(this, "NotificationService loading notification", Toast.LENGTH_SHORT)
                    .show();
        }

        // Load the Deployment object
        Deployment deployment = DatabaseManager.getInstance(this).getDeployment(deploymentId);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        RemoteViews view = new RemoteViews(getPackageName(), R.layout.notification);
        view.setImageViewBitmap(R.id.notification_pie,
                WidgetProvider.getChartBitmap(deployment, SIZE));
        view.setTextViewText(R.id.notification_title, deployment.getName());
        

        view.setTextViewText(
                R.id.notification_main,
                getResources().getString(R.string.small_notification, deployment.getPercentage(),
                        deployment.getCompleted(), deployment.getLength()));

        if(Prefs.hideDate()){
            view.setViewVisibility(R.id.notification_daterange, View.GONE);
        } else {
            view.setTextViewText(
                    R.id.notification_daterange,
                    getResources().getString(R.string.date_range, deployment.getFormattedStart(),
                            deployment.getFormattedEnd()));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(deployment.getName());
        builder.setContentText(getResources().getString(R.string.small_notification,
                deployment.getPercentage(), deployment.getCompleted(), deployment.getLength()));
        builder.setOngoing(true);
        
        // Hide the time, its persistent
        builder.setWhen(0);
        
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setPriority(Integer.MAX_VALUE);
        
        Notification notification = builder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification.bigContentView = view;
        }

        notificationManager.notify(NOTIFICATION_ID, notification);
        
        //Schedule an update at midnight
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        DateTime now = new DateTime();
        DateTime tomorrow = new DateTime(now.plusDays(1)).withTimeAtStartOfDay();
        
        PendingIntent pending = PendingIntent.getBroadcast(getBaseContext(), 0, 
                new Intent(UPDATE_INTENT), PendingIntent.FLAG_UPDATE_CURRENT);
        
        //Adding 100msec to make sure its triggered after midnight
        Log.d("Scheduling notification update for "+tomorrow.getMillis() + 100);
        
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            alarmManager.setExact(AlarmManager.RTC, tomorrow.getMillis() + 100, pending);
        } else {
            alarmManager.set(AlarmManager.RTC, tomorrow.getMillis() + 100, pending);
        }
    }

    /**
     * Load the ID for the deployment to be shown as a notification. Returns -1
     * if one isn't saved yet
     */
    private static int getSavedId(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(KEY_ID, -1);
    }

    /**
     * Sets the ID of the deployment to show as a notification
     * 
     * @param context
     * @param id
     *            the id to save
     */
    public static void setSavedId(Context context, int id) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(KEY_ID, id).apply();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    /**
     * Simple binder to get access to the service
     */
    private class LocalBinder extends Binder {
        NotificationService getService() {
            return NotificationService.this;
        }
    }
    
    private final BroadcastReceiver updateReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update the notification
            showNotification();
        }
    };
}
