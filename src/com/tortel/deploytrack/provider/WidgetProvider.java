package com.tortel.deploytrack.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        
        for (int widgetId : appWidgetIds) {
            // Get the deployment
            WidgetInfo info = DatabaseManager.getInstance(context).getWidgetInfo(widgetId);

            Log.d("Updating widget "+widgetId);
            
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            
            if(info != null){
                Log.d("Widget "+info.getId()+" with deployment "+info.getDeployment().getId());
                
                printDeploymentInfo(info);
                //Draw everything
                remoteViews = updateWidgetView(context, remoteViews, info);
            } else {
                Log.d("Widget "+widgetId+" has null info");
            }
            
            //Update it
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }
    
    private void printDeploymentInfo(WidgetInfo info){
        Deployment deployment = info.getDeployment();
        Log.d("Deployment toString:"+deployment);
        Class<?> c = deployment.getClass();
        for(Method method : c.getDeclaredMethods()){
            if(method.getName().startsWith("get") && method.getParameterTypes().length == 0){
                try {
                    Log.d(method.getName()+": "+method.invoke(deployment, new Object[0]));
                } catch (Exception e) {
                    //Whatever, yo
                }
            }
        }
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
        Bitmap bmp = Bitmap.createBitmap(200,200,conf);
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
