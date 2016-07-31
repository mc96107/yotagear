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
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;

public class BSWidget extends AppWidgetProvider {

	static BroadcastReceiver batteryReceiver;
	static battery compont_battery=new battery();
	static mywifi  compont_wifi=new mywifi();
	
	static MyPhoneStateListener MyListener = new MyPhoneStateListener();   
	static TelephonyManager Tel;  


	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.layout);
		views.setTextViewText(R.id.tv_battery, "zhassjdhs");
		views.setTextViewText(R.id.tv_wifi, "zhassjdhs");
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
				compont_battery.onReceive(context, intent);
				compont_wifi.onReceive(context, intent);
			}

		};

		public int onStartCommand(Intent intent, int flags, int startId) {
			super.onStartCommand(intent, flags, startId);

			/** 注册接收器 */
			IntentFilter filter=new IntentFilter();
			filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
			filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			filter.addAction(Intent.ACTION_BATTERY_CHANGED);
			registerReceiver(batteryReceiver, filter);

			/**监听移动信号状态*/
			Tel = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);  
		    Tel.listen(MyListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);  
		    
			/** 定义一个AppWidgetManager */
			AppWidgetManager manager = AppWidgetManager.getInstance(this);

			/** 定义一个RemoteViews，实现对AppWidget界面控制 */
			RemoteViews views = new RemoteViews(getPackageName(),
					R.layout.layout);

			compont_battery.onStartCommand(views);
			compont_wifi.onStartCommand(views);
			MyListener.onStartCommand(views);

		    /**注册按键启动应用*/
			PackageManager packageManager = getPackageManager();
			Intent start_intent=new Intent();
			start_intent =packageManager.getLaunchIntentForPackage("com.duokan.reader");
			if(intent==null){
			System.out.println("APP not found!");
			}
			PendingIntent Pfullintent= PendingIntent.getActivity(this, 0, start_intent, 0);
			views.setOnClickPendingIntent(R.id.bt_start, Pfullintent);
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
	
	public static class battery{
		static int currentBatteryLevel = -1;
		static int currentBatteryStatus = -1;
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED))
			{
				currentBatteryLevel = intent.getIntExtra("level", 0);
				currentBatteryStatus = intent.getIntExtra("status", 0);
			}
		}
		public void onStartCommand(RemoteViews views) {
			String s_status;
			switch (currentBatteryStatus) {
			case 2:
				s_status = "正在充电";
				break;
			case 5:
				s_status = "已充满";
				break;
			default:
				s_status = "剩余电量";
			}

			views.setTextViewText(R.id.tv_battery, currentBatteryLevel + "%\n"+s_status);
		}
	}
	
	
	public static class mywifi{
		static int wifistate = -1;
		static int strength = -1;
		static String s_wifi_state;
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
			{
				//WIFI开关
				wifistate=intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_DISABLED);
				if(wifistate==WifiManager.WIFI_STATE_DISABLED)
				{
					s_wifi_state="wifi off";
				}
				else
				{
					strength=getStrength(context);
				}
				
			}
			else if(intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION))
			{
				strength=getStrength(context);
			}
			else if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
				NetworkInfo info=intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if(wifistate!=WifiManager.WIFI_STATE_DISABLED)
				{
					if(info.getState().equals(NetworkInfo.State.DISCONNECTED))
					{//如果断开连接
						s_wifi_state="网络已断开";
					}
					else
					{
						strength=getStrength(context);
					}
				}
			}
		}
		public int getStrength(Context context)
		{
			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wifiManager.getConnectionInfo();
			if (info.getBSSID() != null) {
				s_wifi_state = info.getSSID();
				int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
				return strength;
			}
			return 0;
		}
		public void onStartCommand(RemoteViews views) {
			views.setTextViewText(R.id.tv_wifi, strength + "%\n"+s_wifi_state);
		}
	}
	
	
    public static class MyPhoneStateListener extends PhoneStateListener  
    {
    	static int strength = -1;
    	static String type_string;
	    @Override    
	    public void onSignalStrengthsChanged(SignalStrength signalStrength)    
	    {   
	       super.onSignalStrengthsChanged(signalStrength);
	       int type = Tel.getNetworkType();
	       switch(type)
		   {
	       case TelephonyManager.NETWORK_TYPE_GPRS:
	    	   type_string = "GPRS";
	    	   break;
	       case TelephonyManager.NETWORK_TYPE_EDGE:
	    	   type_string = "EDGE";
	    	   break;
	       case TelephonyManager.NETWORK_TYPE_LTE:
	    	   type_string = "LTE 4G";
	    	   break;
	       default:
	    	   type_string = "unknow "+type;
	    	  
		   }
	       strength = signalStrength.getGsmSignalStrength();  
	    }
	    
		public void onStartCommand(RemoteViews views) {
			views.setTextViewText(R.id.tv_sig, strength + "%\n"+type_string);
		}
    }
}
