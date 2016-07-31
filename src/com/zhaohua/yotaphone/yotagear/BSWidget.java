package com.zhaohua.yotaphone.yotagear;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

public class BSWidget extends AppWidgetProvider {

	static BroadcastReceiver batteryReceiver;
	static int currentBatteryLevel = -1;
	static int currentBatteryStatus = -1;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.layout);
		views.setTextViewText(R.id.tv_battery, "zhassjdhs");
		appWidgetManager.updateAppWidget(appWidgetIds, views);

		context.startService(new Intent(context, updateService.class));
	}

	public static class updateService extends Service {
		@Override
		public IBinder onBind(Intent intent) {
			// TODO Auto-generated method stub
			return null;
		}

		/** 定义一个接收电池信息的broascastReceiver */
		private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				currentBatteryLevel = intent.getIntExtra("level", 0);
				currentBatteryStatus = intent.getIntExtra("status", 0);
			}

		};

		public int onStartCommand(Intent intent, int flags, int startId) {
			super.onStartCommand(intent, flags, startId);

			/** 注册接收器 */
			registerReceiver(batteryReceiver, new IntentFilter(
					Intent.ACTION_BATTERY_CHANGED));

			/** 定义一个AppWidgetManager */
			AppWidgetManager manager = AppWidgetManager.getInstance(this);

			/** 定义一个RemoteViews，实现对AppWidget界面控制 */
			RemoteViews views = new RemoteViews(getPackageName(),
					R.layout.layout);

			String s_status;
			switch (currentBatteryStatus) {
			case 2:
				s_status = "正在充电: ";
				break;
			case 5:
				s_status = "已充满: ";

			default:
				s_status = "剩余电量: ";
			}

			views.setTextViewText(R.id.tv_battery, s_status
					+ currentBatteryLevel + "% ");

			ComponentName thisWidget = new ComponentName(this, BSWidget.class);

			/** 使用AlarmManager实现每隔一秒发送一次更新提示信息，实现信息实时动态变化 */
			long now = System.currentTimeMillis();
			long pause = 1000;

			Intent alarmIntent = new Intent();
			alarmIntent = intent;

			PendingIntent pendingIntent = PendingIntent.getService(this, 0,
					alarmIntent, 0);
			AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			alarm.set(AlarmManager.RTC_WAKEUP, now + pause, pendingIntent);

			/** 更新AppWidget */
			manager.updateAppWidget(thisWidget, views);
			return START_STICKY;

		}
	}
}
