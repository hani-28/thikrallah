package com.HMSolutions.thikrallah.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ThikrAlarmReceiver extends BroadcastReceiver {
    String TAG = "ThikrAlarmReceiver";
   @Override
   public void onReceive(Context context, Intent intent) {
	   Log.d(TAG,"onrecieve called");
       Bundle data=intent.getExtras();
       data.putBoolean("isUserAction",false);
	   context.startService(new Intent(context, ThikrService.class).putExtras(data));
      
   }
}