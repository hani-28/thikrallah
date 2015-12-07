package com.HMSolutions.thikrallah.Notification;



import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.ThikrMediaPlayerService;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;



public class ThikrService extends IntentService implements OnCompletionListener {
	private final static int NOTIFICATION_ID=0;
	private MediaPlayer player;
	public ThikrService() {
		super("service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		new MyAlarmsManager(this.getApplicationContext()).UpdateAllApplicableAlarms();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		Bundle data=intent.getExtras();
		String thikrType="";
		thikrType=data.getString("com.HMSolutions.thikrallah.datatype");

		if (thikrType.equals(MainActivity.DATA_TYPE_GENERAL_THIKR)){

			int fileNumber=new Random().nextInt(5) + 1;
			//fire text chat head service
			Intent intentChatHead=new Intent(this.getApplicationContext(), ChatHeadService.class);
			intentChatHead.putExtra("thikr", fileNumber);
			startService(intentChatHead);


			int reminderType=Integer.parseInt(sharedPrefs.getString("RemindmeThroughTheDayType", "1"));
			boolean isQuietTime=isTimeNowQuietTime();
			if ((reminderType==1 ||reminderType==2)&&isQuietTime==false){
				player = new MediaPlayer();
				player.reset();
				player.setOnCompletionListener(this);
				player.setWakeMode(this.getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
				AssetFileDescriptor afd;
				try {
					player.setAudioStreamType(AudioManager.STREAM_RING);
					afd = this.getApplicationContext().getAssets().openFd(thikrType+"/"+ fileNumber+".mp3");
					player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
					player.prepare();
					player.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					player.stop();
					player.release();
					e.printStackTrace();
				}
			}




		}
		if (thikrType.equals(MainActivity.DATA_TYPE_DAY_THIKR)){
			int reminderType=Integer.parseInt(sharedPrefs.getString("remindMeDayThikrType", "1"));
			if (reminderType==1){
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				android.support.v4.app.NotificationCompat.Builder mBuilder = new android.support.v4.app.NotificationCompat.Builder(this);
				mBuilder.setContentTitle(this.getString(R.string.app_name))
				.setContentText(this.getString(R.string.morningThikr))
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(true);



				Intent launchAppIntent = new Intent(this, MainActivity.class);

				launchAppIntent.putExtra("FromNotification",true);
				launchAppIntent.putExtra("DataType", MainActivity.DATA_TYPE_DAY_THIKR);
				PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
						0, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT
						);

				mBuilder.setContentIntent(launchAppPendingIntent);

				mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
			}else{
				//new here

				sharedPrefs.edit().putString("thikrType", MainActivity.DATA_TYPE_DAY_THIKR).commit();
				Bundle data2=new Bundle();
				data2.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAYALL);
				this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data2));
			}
		}
		if (thikrType.equals(MainActivity.DATA_TYPE_NIGHT_THIKR)){
			int reminderType=Integer.parseInt(sharedPrefs.getString("remindMeNightThikrType", "1"));
			if (reminderType==1){
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				android.support.v4.app.NotificationCompat.Builder mBuilder = new android.support.v4.app.NotificationCompat.Builder(this);
				mBuilder.setContentTitle(this.getString(R.string.app_name))
				.setContentText(this.getString(R.string.nightThikr))
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(true);



				Intent launchAppIntent = new Intent(this, MainActivity.class);

				launchAppIntent.putExtra("FromNotification",true);
				launchAppIntent.putExtra("DataType", MainActivity.DATA_TYPE_NIGHT_THIKR);
				PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
						0, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT
						);

				mBuilder.setContentIntent(launchAppPendingIntent);

				mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());	
			}else{

				sharedPrefs.edit().putString("thikrType", MainActivity.DATA_TYPE_NIGHT_THIKR).commit();
				Bundle data2=new Bundle();
				data2.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAYALL);
				this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data2));

			}



		}

	}


	private boolean isTimeNowQuietTime() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		boolean quiet_time_choice=sharedPrefs.getBoolean("quiet_time_choice", true);
		if (quiet_time_choice){
			String quiet_time_start=sharedPrefs.getString("quiet_time_start", "22:00");
			String quiet_time_end=sharedPrefs.getString("quiet_time_end", "22:00");




			Calendar now = Calendar.getInstance();

			int hour = now.get(Calendar.HOUR_OF_DAY); // Get hour in 24 hour format
			int minute = now.get(Calendar.MINUTE);

			Date date = parseDate(hour + ":" + minute);
			Date dateCompareOne = parseDate(quiet_time_start);
			Date dateCompareTwo = parseDate(quiet_time_end);
			if (dateCompareOne.after(dateCompareTwo)){
				if (!(dateCompareTwo.before( date ) && dateCompareOne.after(date))) {

					return true;
				}
			}else{
				if (dateCompareOne.before( date ) && dateCompareTwo.after(date)) {

					return true;
				}
			}
			return false;


		}else{
			return false;
		}
	}
	private Date parseDate(String date) {

		final String inputFormat = "HH:mm";
		SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat, Locale.US);
		try {
			return inputParser.parse(date);
		} catch (java.text.ParseException e) {
			return new Date(0);
		}
	}
	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		mp.reset();
		mp.release();
	}

}