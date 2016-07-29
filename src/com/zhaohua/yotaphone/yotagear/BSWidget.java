package com.zhaohua.yotaphone.yotagear;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.RemoteViews;

public class BSWidget extends AppWidgetProvider {

	static BroadcastReceiver batteryReceiver;
	int currentBatteryLevel = -1;  
    int currentBatteryStatus = -1;
	
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout);
        Log.d("zhaohua", "zhaohua onUpdate");
        views.setTextViewText(R.id.tv_battery, "zhassjdhs");
        appWidgetManager.updateAppWidget(appWidgetIds, views);
        
        if (batteryReceiver==null){
            batteryReceiver=new BroadcastReceiver()  
            {  
                @Override  
                public void onReceive(Context context, Intent intent) {  
                    int currentBatteryLevel=intent.getIntExtra("level", 0);  
                    int currentBatteryStatus=intent.getIntExtra("status", 0);
                    Log.d("zhaohua", "zhaohua onReceive");
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout);
                    views.setTextViewText(R.id.tv_battery, currentBatteryLevel+" % "+currentBatteryStatus);
                    
                    AppWidgetManager manager=AppWidgetManager.getInstance(context.getApplicationContext());
                    ComponentName thisWidget=new ComponentName(context.getApplicationContext(),BSWidget.class);  
                    manager.updateAppWidget(thisWidget, views);
                }  
                  
            }; 
            context.getApplicationContext().registerReceiver(batteryReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));  
        }
    }
}
