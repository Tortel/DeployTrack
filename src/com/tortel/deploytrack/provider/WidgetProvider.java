package com.tortel.deploytrack.provider;

import java.util.Random;

import com.tortel.deploytrack.Log;
import com.tortel.deploytrack.R;
import com.tortel.deploytrack.data.*;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {

        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                WidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            // Get the deployment
            Deployment deployment = DatabaseManager.getInstance(context)
                    .getAllDeployments().get(0);
            Resources resources = context.getResources();

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap bmp = Bitmap.createBitmap(200,200,conf);
            Canvas canvas = new Canvas(bmp);
            
            Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
            mPaint.setDither(true);
            mPaint.setColor(deployment.getCompletedColor());
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(10);
            
            RectF box = new RectF(5,5,bmp.getWidth() - 5,bmp.getHeight() - 5);
            float sweep = 360* deployment.getPercentage() * 0.01f;

            //Draw the completed
            canvas.drawArc(box, -90f, sweep, true, mPaint);
            
            //Draw the remaining
            mPaint.setColor(deployment.getRemainingColor());
            canvas.drawArc(box, sweep - 90f, 360f - sweep, true, mPaint);
            
            //Draw a blank circle in the middle
            RectF smallbox = new RectF(100 - 20, 100 - 20, 100 + 20, 100 + 20);
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
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews
                    .setOnClickPendingIntent(R.id.widget_name, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
}
