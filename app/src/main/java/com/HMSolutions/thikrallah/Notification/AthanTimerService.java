package com.HMSolutions.thikrallah.Notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.PrayTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class AthanTimerService extends Service  {
	NotificationCompat.Builder notificationBuilder;
    String TAG = "AthanTimerService";
    private final static int NOTIFICATION_ID=2323;
	private Context mContext;
	SharedPreferences sharedPrefs;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
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

	@Override
	public void onDestroy() {
		super.onDestroy();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);

	}
	private void initNotification() {
		Intent resultIntent = new Intent(this, MainActivity.class);



		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
				0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT
		);


		notificationBuilder = new NotificationCompat.Builder(this);

		notificationBuilder
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(true)
				.setContentTitle(getString(R.string.app_name))
				.setPriority(Notification.PRIORITY_MAX)
				.setContentText(getNextPrayer())

				.setContentIntent(launchAppPendingIntent);
		notificationBuilder=setVisibilityPublic(notificationBuilder);
		startForeground(NOTIFICATION_ID, notificationBuilder.build());

	}
	private NotificationCompat.Builder setVisibilityPublic(NotificationCompat.Builder inotificationBuilder){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			inotificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
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
