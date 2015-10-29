package com.HMSolutions.thikrallah.Notification;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class ThikrBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		MyAlarmsManager manager=new MyAlarmsManager(context);
		manager.UpdateAllApplicableAlarms();
	}
}
