package com.HMSolutions.thikrallah.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ThikrAlarmReceiver extends BroadcastReceiver {
   @Override
   public void onReceive(Context context, Intent intent) {
	   Bundle data=intent.getExtras();
       data.putBoolean("isUserAction",false);
	   context.startService(new Intent(context, ThikrService.class).putExtras(data));
      
   }
}