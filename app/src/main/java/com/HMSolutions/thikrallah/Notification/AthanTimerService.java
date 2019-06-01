package com.HMSolutions.thikrallah.Notification;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.PrayTime;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.services.common.Crash;


public class AthanTimerService extends Service {
	NotificationCompat.Builder notificationBuilder;
    String TAG = "AthanTimerService";
    private final static int NOTIFICATION_ID=54;
	private Context mContext;


    public static final int JOB_ID = 0x01;


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mContext=this.getApplicationContext();
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		boolean isTimer=sharedPrefs.getBoolean("foreground_athan_timer",true);

		if(isTimer){
			Log.d(TAG,TAG+"started");
			Timer timer = new Timer();

			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					initNotification();

				}
			},0,60000);


			return START_STICKY;
		}else{
			this.stopSelf();
			return START_NOT_STICKY;
		}



	}


	SharedPreferences sharedPrefs;


	@Override
	public void onDestroy() {
		super.onDestroy();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);

	}
	private void initNotification() {
		Log.d(TAG,"initiNotification started");
		Crashlytics.log("AthanTimerService called");
		Intent resultIntent = new Intent(mContext, MainActivity.class);



		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent launchAppPendingIntent = PendingIntent.getActivity(mContext,
				0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT
		);


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			String NOTIFICATION_CHANNEL_ID = "com.HMSolutions.thikrallah.Notification.AthanTimerService";
			String channelName = this.getResources().getString(R.string.athan_timer_notifiaction);
			NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            chan.setSound(null,null);
			chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
			NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			assert manager != null;
			manager.createNotificationChannel(chan);
			notificationBuilder = new NotificationCompat.Builder(mContext,NOTIFICATION_CHANNEL_ID);
		}else{
			notificationBuilder = new NotificationCompat.Builder(mContext);
		}




		notificationBuilder
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(true)
				.setContentTitle(getString(R.string.my_app_name))
				.setPriority(Notification.PRIORITY_DEFAULT)
				.setContentText(getNextPrayer())

				.setContentIntent(launchAppPendingIntent);
		notificationBuilder=setVisibilityPublic(notificationBuilder);
		Log.d(TAG,"started forground");
		Log.d(TAG,"context is "+mContext);
		if (mContext!=null){
			startForeground(NOTIFICATION_ID, notificationBuilder.build());
		}


	}
	private NotificationCompat.Builder setVisibilityPublic(NotificationCompat.Builder inotificationBuilder){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			inotificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}
		return inotificationBuilder;
	}
	String getNextPrayer(){
		PrayTime prayers = PrayTime.instancePrayTime(this);
		prayers.setTimeFormat(PrayTime.TIME_FORMAT_Time24);
		String[] prayerTimes = prayers.getPrayerTimes(this);
		ArrayList<String> prayerTimesList = new ArrayList<>();
		prayerTimesList.add(prayerTimes[0]);
		prayerTimesList.add(prayerTimes[1]);
		prayerTimesList.add(prayerTimes[2]);
		prayerTimesList.add(prayerTimes[3]);
		prayerTimesList.add(prayerTimes[5]);
		prayerTimesList.add(prayerTimes[6]);
		Date dat  = new Date();
		Calendar now = Calendar.getInstance();
		now.setTime(dat);
		long min=Long.MAX_VALUE;
		int nextPrayer=12;
		String prayerName="NA";
		long[] PrayerTimesInMilliseconds=new long[6];
		for (int i=0;i<prayerTimesList.size();i++){
			if (prayerTimesList.get(i).equalsIgnoreCase(prayers.getInvalidTime())){
				return null;
			}else {
				Calendar prayer_time = Calendar.getInstance();
				//calendar.setTimeInMillis(System.currentTimeMillis());
				prayer_time.set(Calendar.HOUR_OF_DAY, Integer.parseInt(prayerTimesList.get(i).split(":")[0]));
				prayer_time.set(Calendar.MINUTE, Integer.parseInt(prayerTimesList.get(i).split(":")[1]));
				prayer_time.set(Calendar.SECOND, 0);
				if (prayer_time.before(now)) {
					prayer_time.add(Calendar.HOUR, 24);
				}
				PrayerTimesInMilliseconds[i] = prayer_time.getTimeInMillis() - now.getTimeInMillis();
				if (PrayerTimesInMilliseconds[i] < min) {
					min = PrayerTimesInMilliseconds[i];
					nextPrayer = i;
				}
			}
		}
		switch (nextPrayer){
			case 0:
				prayerName= getResources().getString(R.string.prayer1);
				break;
			case 1:
				prayerName= getResources().getString(R.string.sunrise);
				break;
			case 2:
				prayerName= getResources().getString(R.string.prayer2);
				break;
			case 3:
				prayerName= getResources().getString(R.string.prayer3);
				break;
			case 4:
				prayerName= getResources().getString(R.string.prayer4);
				break;
			case 5:
				prayerName= getResources().getString(R.string.prayer5);
				break;
			default:
				prayerName="NA";
		}
		min=min/1000;
		int hours=(int) Math.floor(((double) min)/3600);
		long minutes=(min-hours*3600)/60;
		String hoursText="";
		String minutesText="";
		if (hours==1){
			hoursText=hours+" "+getResources().getString(R.string.hour)+" ";
		}else if(hours>1){
			hoursText=hours+" "+getResources().getString(R.string.hours)+" ";
		}else{
			hoursText="";
		}

		minutesText=minutes+" "+getResources().getString(R.string.minute);


		return hoursText+" "+minutesText+" "+getResources().getString(R.string.untill)+" "+prayerName;

	}
}
