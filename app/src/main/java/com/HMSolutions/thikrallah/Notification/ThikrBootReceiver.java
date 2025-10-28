package com.HMSolutions.thikrallah.Notification;


import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;


public class ThikrBootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (null != intent.getAction()){
			Log.d("ThikrBootReceiver","intent called with action"+intent.getAction());
			if (intent.getAction().equalsIgnoreCase(AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)){
				new MyAlarmsManager(context.getApplicationContext()).UpdateAllApplicableAlarms();
			}
			if (intent.getAction().equalsIgnoreCase("com.HMSolutions.thikrallah.Notification.ThikrBootReceiver.android.action.broadcast")
					|| intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)){
				MyAlarmsManager manager=new MyAlarmsManager(context.getApplicationContext());
				manager.UpdateAllApplicableAlarms();

				SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(context);
				boolean isTimer=mPrefs.getBoolean("foreground_athan_timer",true);

				if(isTimer){
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.S){
							if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)){
								context.startForegroundService(new Intent(context,AthanTimerService.class));
							}
							//foreground service start not allowed unless at boot completed since SDK 31


						}else{
							context.startForegroundService(new Intent(context,AthanTimerService.class));
						}

					} else {
						context.startService(new Intent(context,AthanTimerService.class));
					}
				}
			}
		}




	}
}
