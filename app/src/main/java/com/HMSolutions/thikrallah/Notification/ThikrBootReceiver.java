package com.HMSolutions.thikrallah.Notification;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class ThikrBootReceiver extends BroadcastReceiver {
    private String TAG="ThikrBootReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"ThikrBootReceiver onrecieve called");
		MyAlarmsManager manager=new MyAlarmsManager(context);
		manager.UpdateAllApplicableAlarms();
	}
}
