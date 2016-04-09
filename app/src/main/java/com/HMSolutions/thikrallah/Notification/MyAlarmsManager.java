package com.HMSolutions.thikrallah.Notification;

import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.HMSolutions.thikrallah.MainActivity;

public class MyAlarmsManager {
    String TAG = "MyAlarmsManager";
	public static final int requestCodeMorningAlarm=8;
	public static final int requestCodeNightAlarm=20;
	public static final int requestCodeRandomAlarm=1;
    public static final int requestCodeKahfAlarm=25;
	AlarmManager alarmMgr;
	Context context;
	public MyAlarmsManager(Context icontext){
		context=icontext;
	}
	public void UpdateAllApplicableAlarms() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		String[] MorningReminderTime=sharedPrefs.getString("daytReminderTime", "8:00").split(":");
		String[] NightReminderTime=sharedPrefs.getString("nightReminderTime", "20:00").split(":");
        String[] kahfReminderTime=sharedPrefs.getString("kahfReminderTime", "10:00").split(":");
		String RandomReminderInterval= sharedPrefs.getString("RemindMeEvery", "60");
		boolean remindMeMorningThikr=sharedPrefs.getBoolean("remindMeMorningThikr", true);
		boolean remindMeNightThikr=sharedPrefs.getBoolean("remindMeNightThikr", true);
		boolean RemindmeThroughTheDay=sharedPrefs.getBoolean("RemindmeThroughTheDay", true);
        boolean Remindmekahf=sharedPrefs.getBoolean("remindMekahf", true);
		Intent launchIntent=new Intent(context, ThikrAlarmReceiver.class);

		//Morning Reminder		
		PendingIntent pendingIntentMorningThikr =PendingIntent.getBroadcast(context, requestCodeMorningAlarm, launchIntent.putExtra("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_DAY_THIKR), PendingIntent.FLAG_UPDATE_CURRENT);		
		if (remindMeMorningThikr){
			
			// Set the alarm to start at approximately 2:00 p.m.
			Calendar calendar0 = Calendar.getInstance();
			//calendar.setTimeInMillis(System.currentTimeMillis());
			calendar0.set(Calendar.HOUR_OF_DAY, Integer.parseInt(MorningReminderTime[0]));
			calendar0.set(Calendar.MINUTE, Integer.parseInt(MorningReminderTime[1]));
			calendar0.set(Calendar.SECOND, 0);


			setAlarm(calendar0,pendingIntentMorningThikr);
			
		}else{
			alarmMgr.cancel(pendingIntentMorningThikr);
		
		}


		//Night Reminder
		PendingIntent pendingIntentNightThikr =PendingIntent.getBroadcast(context, requestCodeNightAlarm,launchIntent.putExtra("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_NIGHT_THIKR), PendingIntent.FLAG_UPDATE_CURRENT);		

		if (remindMeNightThikr){
			// Set the alarm to start at approximately 2:00 p.m.
			Calendar calendar1 = Calendar.getInstance();
			//calendar.setTimeInMillis(System.currentTimeMillis());
			calendar1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(NightReminderTime[0]));
			calendar1.set(Calendar.MINUTE, Integer.parseInt(NightReminderTime[1]));
			calendar1.set(Calendar.SECOND, 0);

			setAlarm(calendar1,pendingIntentNightThikr);
			
		}else{
			alarmMgr.cancel(pendingIntentNightThikr);
			
		}
		PendingIntent pendingIntentGeneral =PendingIntent.getBroadcast(context, requestCodeRandomAlarm,launchIntent.putExtra("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_GENERAL_THIKR), PendingIntent.FLAG_UPDATE_CURRENT);
		
		if (RemindmeThroughTheDay){
			//Random Reminder
            alarmMgr.cancel(pendingIntentGeneral);
           // alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Long.parseLong(RandomReminderInterval) * 1000 * 60,
            //        Long.parseLong(RandomReminderInterval) * 1000 * 60, pendingIntentGeneral);


            Date dat  = new Date();
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(dat);
            calendar1.add(Calendar.MINUTE,Integer.parseInt(RandomReminderInterval));
            this.setAlarm(calendar1, pendingIntentGeneral);

		}else{
			alarmMgr.cancel(pendingIntentGeneral);
		}

        PendingIntent pendingIntentKahf =PendingIntent.getBroadcast(context, requestCodeKahfAlarm,launchIntent.putExtra("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_QURAN_KAHF), PendingIntent.FLAG_UPDATE_CURRENT);

        if (Remindmekahf && sharedPrefs.getInt("lastKahfPlayed",-1)!=Calendar.getInstance().get(Calendar.DAY_OF_MONTH)){
            //Random Reminder
            alarmMgr.cancel(pendingIntentKahf);
            // alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Long.parseLong(RandomReminderInterval) * 1000 * 60,
            //        Long.parseLong(RandomReminderInterval) * 1000 * 60, pendingIntentGeneral);

            Calendar calendar1 = Calendar.getInstance();
            //calendar.setTimeInMillis(System.currentTimeMillis());
            calendar1.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
            calendar1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(kahfReminderTime[0]));
            calendar1.set(Calendar.MINUTE, Integer.parseInt(kahfReminderTime[1]));
            calendar1.set(Calendar.SECOND, 0);

            setAlarm(calendar1, pendingIntentKahf);

        }else{
            alarmMgr.cancel(pendingIntentKahf);
        }

	}
	@SuppressLint("NewApi")
	private void setAlarm(Calendar time, PendingIntent pendingIntent){
		Long timeInMilliseconds=getFutureTimeIfTimeInPast(time.getTimeInMillis());
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			alarmMgr.set(AlarmManager.RTC_WAKEUP, timeInMilliseconds, pendingIntent);
		} else{
			alarmMgr.setExact(AlarmManager.RTC_WAKEUP,timeInMilliseconds, pendingIntent);
		}
	}
	private Long getFutureTimeIfTimeInPast(Long time){
		Long remainingTime=time-System.currentTimeMillis();
		if(remainingTime<0){
			return time+24*60*60*1000;//If time in past. set time to 24 hours after;
		}else{
			return time;
		}

	}
}
