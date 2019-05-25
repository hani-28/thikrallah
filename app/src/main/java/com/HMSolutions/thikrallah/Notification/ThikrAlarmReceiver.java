package com.HMSolutions.thikrallah.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
public class ThikrAlarmReceiver extends BroadcastReceiver {
    String TAG = "ThikrAlarmReceiver";
   @Override
   public void onReceive(Context context, Intent intent) {
	   Log.d(TAG,"onrecieve called");
       Bundle data=intent.getExtras();
       data.putBoolean("isUserAction",false);
	   Intent intent2=new Intent(context, ThikrService.class).putExtras(data);
       //startWakefulService(context,intent2);
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
           Log.d(TAG,"starting foreground service ThikrService");
           context.startForegroundService(intent2);
       } else {
           Log.d(TAG,"starting background service ThikrService");
           context.startService(intent2);
       }
      
   }
}