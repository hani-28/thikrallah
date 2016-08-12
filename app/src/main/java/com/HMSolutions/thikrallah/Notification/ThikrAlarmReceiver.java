package com.HMSolutions.thikrallah.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class ThikrAlarmReceiver extends WakefulBroadcastReceiver {
    String TAG = "ThikrAlarmReceiver";
   @Override
   public void onReceive(Context context, Intent intent) {
	   Log.d(TAG,"onrecieve called");
       Bundle data=intent.getExtras();
       data.putBoolean("isUserAction",false);
	   Intent intent2=new Intent(context, ThikrService.class).putExtras(data);
       startWakefulService(context,intent2);
      
   }
}