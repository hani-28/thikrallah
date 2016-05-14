package com.HMSolutions.thikrallah.Notification;



import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.Models.UserThikr;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.ThikrMediaPlayerService;
import com.HMSolutions.thikrallah.Utilities.MyDBHelper;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;


public class ThikrService extends IntentService  {
    String TAG = "ThikrService";
	private final static int NOTIFICATION_ID=0;

    public ThikrService() {
		super("service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		new MyAlarmsManager(this.getApplicationContext()).UpdateAllApplicableAlarms();
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String lang=sharedPrefs.getString("language",null);

        if (lang!=null){
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }


		Bundle data=intent.getExtras();
		String thikrType="";
		thikrType=data.getString("com.HMSolutions.thikrallah.datatype");
		if (thikrType.equals(MainActivity.DATA_TYPE_GENERAL_THIKR)){
            int ThikrCount = this.getResources().getStringArray(R.array.GeneralThikr).length;
            MyDBHelper db = new MyDBHelper(this);
            UserThikr thikr=db.getRandomThikr();
            if (thikr==null){
                return;
            }
            int fileNumber=Integer.parseInt(thikr.getFile());
			//fire text chat head service
			Intent intentChatHead=new Intent(this.getApplicationContext(), ChatHeadService.class);
			intentChatHead.putExtra("thikr", thikr.getThikrText());
			startService(intentChatHead);


			int reminderType=Integer.parseInt(sharedPrefs.getString("RemindmeThroughTheDayType", "1"));
			boolean isQuietTime=isTimeNowQuietTime();
			if ((reminderType==1 ||reminderType==2)&&isQuietTime==false&&thikr.isBuiltIn()==true){
                sharedPrefs.edit().putString("thikrType", MainActivity.DATA_TYPE_GENERAL_THIKR).commit();
                data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAY);
                Log.d(TAG,"fileNumber sent through intent is "+fileNumber);
                data.putInt("FILE", fileNumber);
                this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
			}
            return;



		}
		if (thikrType.equals(MainActivity.DATA_TYPE_DAY_THIKR)){
			int reminderType=Integer.parseInt(sharedPrefs.getString("remindMeDayThikrType", "1"));
			if (reminderType==1){
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
				mBuilder.setContentTitle(this.getString(R.string.app_name))
				.setContentText(this.getString(R.string.morningThikr))
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(true);
                mBuilder=setVisibilityPublic(mBuilder);
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mBuilder.setSound(soundUri,AudioManager.STREAM_NOTIFICATION);

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

				data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAYALL);
				this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
			}
            return;
		}
		if (thikrType.equals(MainActivity.DATA_TYPE_NIGHT_THIKR)){
			int reminderType=Integer.parseInt(sharedPrefs.getString("remindMeNightThikrType", "1"));
			if (reminderType==1){
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
				mBuilder.setContentTitle(this.getString(R.string.app_name))
				.setContentText(this.getString(R.string.nightThikr))
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(true);
                mBuilder=setVisibilityPublic(mBuilder);
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mBuilder.setSound(soundUri,AudioManager.STREAM_NOTIFICATION);

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
				data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAYALL);
				this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));

			}
            return;


		}
        if (thikrType.equals(MainActivity.DATA_TYPE_QURAN_KAHF)){
            sharedPrefs.edit().putInt("", Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).commit();

            int reminderType=Integer.parseInt(sharedPrefs.getString("remindMekahfType", "1"));
            if (reminderType==1){
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                mBuilder.setContentTitle(this.getString(R.string.app_name))
                        .setContentText(this.getString(R.string.surat_alkahf))
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true);

                mBuilder=setVisibilityPublic(mBuilder);
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mBuilder.setSound(soundUri,AudioManager.STREAM_NOTIFICATION);
                Intent launchAppIntent = new Intent(this, MainActivity.class);

                launchAppIntent.putExtra("FromNotification",true);
                launchAppIntent.putExtra("DataType", MainActivity.DATA_TYPE_QURAN);
                launchAppIntent.putExtra("surat", 0);
                PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
                        0, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT
                );

                mBuilder.setContentIntent(launchAppPendingIntent);

                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            }else{
                //new here

                sharedPrefs.edit().putString("thikrType", MainActivity.DATA_TYPE_QURAN_KAHF).commit();

                data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAYALL);
                this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
            }
            return;
        }
        if (thikrType.contains(MainActivity.DATA_TYPE_ATHAN)){
            int reminderType=3;


            String athan=this.getString(R.string.athan);
            switch (thikrType){
                case MainActivity.DATA_TYPE_ATHAN1:
                    athan=this.getString(R.string.prayer1);
                    reminderType=Integer.parseInt(sharedPrefs.getString("fajr_reminder_type", "3"));
                    break;
                case MainActivity.DATA_TYPE_ATHAN2:
                    athan=this.getString(R.string.prayer2);
                    reminderType=Integer.parseInt(sharedPrefs.getString("duhr_reminder_type", "3"));
                    break;
                case MainActivity.DATA_TYPE_ATHAN3:
                    athan=this.getString(R.string.prayer3);
                    reminderType=Integer.parseInt(sharedPrefs.getString("asr_reminder_type", "3"));
                    break;
                case MainActivity.DATA_TYPE_ATHAN4:
                    athan=this.getString(R.string.prayer4);
                    reminderType=Integer.parseInt(sharedPrefs.getString("maghrib_reminder_type", "3"));
                    break;
                case MainActivity.DATA_TYPE_ATHAN5:
                    athan=this.getString(R.string.prayer5);
                    reminderType=Integer.parseInt(sharedPrefs.getString("isha_reminder_type", "3"));
                    break;
            }

            if (reminderType==1){//vibrate
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                mBuilder.setContentTitle(this.getString(R.string.app_name))
                        .setContentText(athan )
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true);

                mBuilder=setVisibilityPublic(mBuilder);

                Intent launchAppIntent = new Intent(this, MainActivity.class);

                launchAppIntent.putExtra("FromNotification",true);
                launchAppIntent.putExtra("DataType", MainActivity.DATA_TYPE_ATHAN);
                PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
                        0, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT
                );

                mBuilder.setContentIntent(launchAppPendingIntent);

                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                vibrate();
            }else if (reminderType==2){
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                //Define sound URI
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                mBuilder.setContentTitle(this.getString(R.string.app_name))
                        .setContentText(athan )
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true)
                        .setSound(soundUri,AudioManager.STREAM_NOTIFICATION);

                mBuilder=setVisibilityPublic(mBuilder);

                Intent launchAppIntent = new Intent(this, MainActivity.class);

                launchAppIntent.putExtra("FromNotification",true);
                launchAppIntent.putExtra("DataType", MainActivity.DATA_TYPE_ATHAN);
                PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
                        0, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT
                );

                mBuilder.setContentIntent(launchAppPendingIntent);

                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            }else{
                sharedPrefs.edit().putString("thikrType", thikrType).commit();
                data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAY);
                Log.d(TAG,"fileNumber sent through intent is "+1);
                data.putInt("FILE", 1);
                data.putInt("reminderType",reminderType);
                this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
            }


        }

	}
    private NotificationCompat.Builder setVisibilityPublic(NotificationCompat.Builder inotificationBuilder){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            inotificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        return inotificationBuilder;
    }
    private void vibrate(){
        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

// This example will cause the phone to vibrate "SOS" in Morse Code
// In Morse Code, "s" = "dot-dot-dot", "o" = "dash-dash-dash"
// There are pauses to separate dots/dashes, letters, and words
// The following numbers represent millisecond lengths
        int dot = 200;      // Length of a Morse Code "dot" in milliseconds
        int dash = 500;     // Length of a Morse Code "dash" in milliseconds
        int short_gap = 200;    // Length of Gap Between dots/dashes
        int medium_gap = 500;   // Length of Gap Between Letters
        int long_gap = 1000;    // Length of Gap Between Words
        long[] pattern = {
                0,  // Start immediately

                dash, medium_gap, dash, // o
                medium_gap,
        };

// Only perform this pattern one time (-1 means "do not repeat")
        v.vibrate(pattern, -1);

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
    public void onDestroy(){
        super.onDestroy();

    }
}