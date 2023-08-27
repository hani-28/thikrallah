package com.HMSolutions.thikrallah.Notification;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.Utilities.PrayTime;

import java.util.Calendar;
import java.util.Date;

public class MyAlarmsManager {
    String TAG = "MyAlarmsManager";
	public static final int requestCodeMorningAlarm=8;
    public static final int requestCodeMulkAlarm=26;
	public static final int requestCodeNightAlarm=20;
	public static final int requestCodeRandomAlarm=1;
    public static final int requestCodeKahfAlarm=25;
    public static final int requestCodeAthan1=100;
    public static final int requestCodeAthan2=101;
    public static final int requestCodeAthan3=102;
    public static final int requestCodeAthan4=103;
    public static final int requestCodeAthan5=104;
	AlarmManager alarmMgr;
	Context context;
    private SharedPreferences sharedPrefs;

    public MyAlarmsManager(Context icontext){
		context=icontext;
	}
	public void UpdateAllApplicableAlarms() {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        setPeriodicAlarmManagerUpdates(alarmMgr);
        String[] MorningReminderTime = sharedPrefs.getString("daytReminderTime", "8:00").split(":", 3);
        String[] NightReminderTime = sharedPrefs.getString("nightReminderTime", "20:00").split(":", 3);
        String[] kahfReminderTime = sharedPrefs.getString("kahfReminderTime", "10:00").split(":", 3);
        String[] mulkReminderTime = sharedPrefs.getString("mulkReminderTime", "10:00").split(":", 3);
        String RandomReminderInterval = sharedPrefs.getString("RemindMeEvery", "60");
        boolean remindMeMorningThikr = sharedPrefs.getBoolean("remindMeMorningThikr", true);
        boolean remindMeNightThikr = sharedPrefs.getBoolean("remindMeNightThikr", true);
        boolean RemindmeThroughTheDay = sharedPrefs.getBoolean("RemindmeThroughTheDay", true);
        boolean Remindmekahf = sharedPrefs.getBoolean("remindMekahf", true);
        boolean Remindmemulk = sharedPrefs.getBoolean("remindMemulk", true);

        Intent launchIntent = new Intent("com.HMSolutions.thikrallah.Notification.ThikrAlarmReceiver");
        // create an explicit intent by defining a class
        launchIntent.setClass(context, ThikrAlarmReceiver.class);
        //Intent launchIntent=new Intent(context, ThikrAlarmReceiver.class);


        Date dat = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(dat);
        //now.add(Calendar.SECOND,10);


        //mulk Reminder
        PendingIntent pendingIntentMulk =PendingIntent.getBroadcast(context, requestCodeMulkAlarm,launchIntent.putExtra("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_QURAN_MULK), PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        if (Remindmemulk){

            // Set the alarm to start at approximately 2:00 p.m.
            Calendar calendar0 = Calendar.getInstance();
            //calendar.setTimeInMillis(System.currentTimeMillis());
            calendar0.set(Calendar.HOUR_OF_DAY, Integer.parseInt(mulkReminderTime[0]));
            calendar0.set(Calendar.MINUTE, Integer.parseInt(mulkReminderTime[1]));
            calendar0.set(Calendar.SECOND, 0);


            setAlarm(calendar0,pendingIntentMulk);


            if(calendar0.after(now)){
                setAlarm(calendar0, pendingIntentMulk);
                Log.d(TAG,"mulk reminder set"+calendar0.getTime());
            }else{
                calendar0.add(Calendar.HOUR,24);
                Log.d(TAG,"mulk reminder time in past. 1 day added. alarm set on "+calendar0.getTime());
                setAlarm(calendar0, pendingIntentMulk);
            }


        }else{
            alarmMgr.cancel(pendingIntentMulk);

        }

        //Morning Reminder
		PendingIntent pendingIntentMorningThikr =PendingIntent.getBroadcast(context, requestCodeMorningAlarm, launchIntent.putExtra("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_DAY_THIKR), PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
		if (remindMeMorningThikr){
			
			// Set the alarm to start at approximately 2:00 p.m.
			Calendar calendar0 = Calendar.getInstance();
			//calendar.setTimeInMillis(System.currentTimeMillis());
			calendar0.set(Calendar.HOUR_OF_DAY, Integer.parseInt(MorningReminderTime[0]));
			calendar0.set(Calendar.MINUTE, Integer.parseInt(MorningReminderTime[1]));
			calendar0.set(Calendar.SECOND, 0);


			setAlarm(calendar0,pendingIntentMorningThikr);


            if(calendar0.after(now)){
                setAlarm(calendar0, pendingIntentMorningThikr);
                Log.d(TAG,"daytime reminder set"+calendar0.getTime());
            }else{
                calendar0.add(Calendar.HOUR,24);
                Log.d(TAG,"daytime reminder time in past. 1 day added. alarm set on "+calendar0.getTime());
                setAlarm(calendar0, pendingIntentMorningThikr);
            }


        }else{
			alarmMgr.cancel(pendingIntentMorningThikr);
		
		}


		//Night Reminder
		PendingIntent pendingIntentNightThikr =PendingIntent.getBroadcast(context, requestCodeNightAlarm,launchIntent.putExtra("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_NIGHT_THIKR), PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

		if (remindMeNightThikr){
			// Set the alarm to start at approximately 2:00 p.m.
			Calendar calendar1 = Calendar.getInstance();
			//calendar.setTimeInMillis(System.currentTimeMillis());
			calendar1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(NightReminderTime[0]));
			calendar1.set(Calendar.MINUTE, Integer.parseInt(NightReminderTime[1]));
			calendar1.set(Calendar.SECOND, 0);

            if(calendar1.after(now)){
                setAlarm(calendar1, pendingIntentNightThikr);
                Log.d(TAG,"night reminder set"+calendar1.getTime());
            }else{
                calendar1.add(Calendar.HOUR,24);
                Log.d(TAG,"nigh reminder time in past. 1 day added. alarm set on "+calendar1.getTime());
                setAlarm(calendar1, pendingIntentNightThikr);
            }

			
		}else{
			alarmMgr.cancel(pendingIntentNightThikr);
			
		}
		PendingIntent pendingIntentGeneral =PendingIntent.getBroadcast(context, requestCodeRandomAlarm,launchIntent.putExtra("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_GENERAL_THIKR), PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
		
		if (RemindmeThroughTheDay){
			//Random Reminder
            alarmMgr.cancel(pendingIntentGeneral);


            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(dat);
            calendar1.add(Calendar.MINUTE,Integer.parseInt(RandomReminderInterval));
            this.setAlarm(calendar1, pendingIntentGeneral);

		}else{
			alarmMgr.cancel(pendingIntentGeneral);
		}

        PendingIntent pendingIntentKahf =PendingIntent.getBroadcast(context, requestCodeKahfAlarm,launchIntent.putExtra("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_QURAN_KAHF), PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);

        if (Remindmekahf && Calendar.getInstance().get(Calendar.DAY_OF_MONTH)!=sharedPrefs.getInt("lastKahfPlayed",-1)){



            //Random Reminder
            alarmMgr.cancel(pendingIntentKahf);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
            calendar1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(kahfReminderTime[0]));
            calendar1.set(Calendar.MINUTE, Integer.parseInt(kahfReminderTime[1]));
            calendar1.set(Calendar.SECOND, 0);

            if(calendar1.after(now)){
                setAlarm(calendar1, pendingIntentKahf);
                Log.d(TAG,"kahf reminder set"+calendar1.getTime());
            }else{
                calendar1.add(Calendar.HOUR,24*7);
                Log.d(TAG,"kahf reminder time in past. 7 days added. alarm set on "+calendar1.getTime());
                setAlarm(calendar1, pendingIntentKahf);
            }


        }else{
            alarmMgr.cancel(pendingIntentKahf);
        }
        updateAllPrayerAlarms();

	}



    @SuppressLint("NewApi")
	private void setAlarm(Calendar time, PendingIntent pendingIntent){
		Long timeInMilliseconds=getFutureTimeIfTimeInPast(time.getTimeInMillis());
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			alarmMgr.set(AlarmManager.RTC_WAKEUP, timeInMilliseconds, pendingIntent);
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
			alarmMgr.setExact(AlarmManager.RTC_WAKEUP,timeInMilliseconds, pendingIntent);
		}else{
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,timeInMilliseconds, pendingIntent);
        }
	}
    void setPeriodicAlarmManagerUpdates(AlarmManager alarmmnager){
        Intent launchIntent=new Intent(context, ThikrBootReceiver.class);

        Date dat  = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(dat);

        PendingIntent intent =PendingIntent.getBroadcast(context, 100, launchIntent, PendingIntent.FLAG_ONE_SHOT|PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.HOUR_OF_DAY, 1);
        calendar1.set(Calendar.MINUTE, 15);
        calendar1.set(Calendar.SECOND, 0);

        if(calendar1.after(now)){
            alarmmnager.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar1.getTimeInMillis(), 12*60*60*1000, intent);
           // Log.d(TAG,"alarms refresh time set on"+calendar1.getTime());
        }else{
            calendar1.add(Calendar.HOUR,24);
           // Log.d(TAG,"alarms refresh time in past. 1 days added. alarm set on "+calendar1.getTime());
            alarmmnager.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar1.getTimeInMillis(), 12*60*60*1000, intent);
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
    private void updateAllPrayerAlarms() {
        updatePrayerAlarms(requestCodeAthan1,"isFajrReminder",0,MainActivity.DATA_TYPE_ATHAN1);
        updatePrayerAlarms(requestCodeAthan2,"isDuhrReminder",2,MainActivity.DATA_TYPE_ATHAN2);
        updatePrayerAlarms(requestCodeAthan3,"isAsrReminder",3,MainActivity.DATA_TYPE_ATHAN3);
        updatePrayerAlarms(requestCodeAthan4,"isMaghribReminder",5,MainActivity.DATA_TYPE_ATHAN4);
        updatePrayerAlarms(requestCodeAthan5,"isIshaaReminder",6,MainActivity.DATA_TYPE_ATHAN5);
    }

    private void updatePrayerAlarms(int requestCode, String isReminderPreference,int prayerPosition,String datatype) {
        PrayTime prayers = PrayTime.instancePrayTime(context);
        prayers.setTimeFormat(PrayTime.TIME_FORMAT_Time24);
        String[] prayerTimes = prayers.getPrayerTimes(context);

        if (prayerTimes[prayerPosition].equalsIgnoreCase(prayers.getInvalidTime())){
            return;
        }
        boolean isAthanReminder=sharedPrefs.getBoolean(isReminderPreference, true);
        Intent launchIntent=new Intent(context, ThikrAlarmReceiver.class);


        Date dat  = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(dat);
        //now.add(Calendar.SECOND,10);

        //athan Reminder
        PendingIntent pendingIntentAthan =PendingIntent.getBroadcast(context, requestCode, launchIntent.putExtra("com.HMSolutions.thikrallah.datatype", datatype), PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        alarmMgr.cancel(pendingIntentAthan);
        if (isAthanReminder) {

            Calendar calendar0 = Calendar.getInstance();
            //calendar.setTimeInMillis(System.currentTimeMillis());
            calendar0.set(Calendar.HOUR_OF_DAY, Integer.parseInt(prayerTimes[prayerPosition].split(":", 3)[0]));
            calendar0.set(Calendar.MINUTE, Integer.parseInt(prayerTimes[prayerPosition].split(":", 3)[1]));
            calendar0.set(Calendar.SECOND, 0);

            if (calendar0.after(now)) {
                setAlarm(calendar0, pendingIntentAthan);
                // Log.d(TAG,"athan reminder set"+calendar0.getTime());
            } else {
                calendar0.add(Calendar.HOUR, 24);
                // Log.d(TAG,"athan reminder time in past. 1 day added. alarm set on "+calendar0.getTime());
                setAlarm(calendar0, pendingIntentAthan);
            }


        }else{
            alarmMgr.cancel(pendingIntentAthan);

        }
    }
}
