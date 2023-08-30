package com.HMSolutions.thikrallah.Notification;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;


public class ThikrBootReceiver extends BroadcastReceiver {
    private String TAG="ThikrBootReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG,"ThikrBootReceiver onrecieve called");

		MyAlarmsManager manager=new MyAlarmsManager(context);
		manager.UpdateAllApplicableAlarms();

		SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(context);
		boolean isTimer=mPrefs.getBoolean("foreground_athan_timer",true);

		if(isTimer){
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				context.startForegroundService(new Intent(context,AthanTimerService.class));
			} else {
				context.startService(new Intent(context,AthanTimerService.class));
			}
		}




	}
}
