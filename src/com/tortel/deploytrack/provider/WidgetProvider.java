/*
 * Copyright (C) 2013 Scott Warner
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

import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.*;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
    private static final int DEFAULT_SIZE = 200;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        
        for (int widgetId : appWidgetIds) {
            // Get the deployment
            WidgetInfo info = DatabaseManager.getInstance(context).getWidgetInfo(widgetId);

            Log.d("Updating widget "+widgetId);
            
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            
            if(info != null){
                Log.d("Widget "+info.getId()+" with deployment "+info.getDeployment().getId());

                //Draw everything
                remoteViews = updateWidgetView(context, remoteViews, info);
            } else {
                Log.d("Widget "+widgetId+" has null info");
            }
            
            //Update it
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        //Remove them from the database
        for(int id: appWidgetIds){
            Log.d("Deleting widget "+id);
            DatabaseManager.getInstance(context).deleteWidgetInfo(id);
        }
        
        super.onDeleted(context, appWidgetIds);
    }
    
    /**
     * Sets up and fills the RemoteViews with the data provided in the WidgetInfo class
     * @param context
     * @param remoteViews
     * @param info
     */
    public static RemoteViews updateWidgetView(Context context, RemoteViews remoteViews, WidgetInfo info){
        Deployment deployment = info.getDeployment();
        Resources resources = context.getResources();
        
        //Set up the pie chart image
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(DEFAULT_SIZE,DEFAULT_SIZE,conf);
        Canvas canvas = new Canvas(bmp);
        
        //Configure the paint
        Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setColor(deployment.getCompletedColor());
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);
        
        //Pad the pie by 5px
        RectF box = new RectF(5,5,bmp.getWidth() - 5,bmp.getHeight() - 5);
        
        //The length of the arc to use for completed
        float sweep = 360* deployment.getPercentage() * 0.01f;

        //Draw the completed
        canvas.drawArc(box, -90f, sweep, true, mPaint);
        
        //Draw the remaining
        mPaint.setColor(deployment.getRemainingColor());
        canvas.drawArc(box, sweep - 90f, 360f - sweep, true, mPaint);
        
        //Draw a blank circle in the middle
        int middle = bmp.getWidth() / 2;
        int padding = bmp.getWidth() / 10;
        RectF smallbox = new RectF(middle - padding, middle - padding, middle + padding, middle + padding);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawArc(smallbox, 0f, 360f, false, mPaint);
        
        // Set the text
        remoteViews.setTextViewText(R.id.widget_name, deployment.getName());
        remoteViews.setTextViewText(
                R.id.widget_info,
                resources.getString(R.string.days_left,
                        deployment.getRemaining()));
        remoteViews.setImageViewBitmap(R.id.widget_pie, bmp);

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
}
