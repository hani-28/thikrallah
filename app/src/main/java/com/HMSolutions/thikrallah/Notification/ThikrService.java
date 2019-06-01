package com.HMSolutions.thikrallah.Notification;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.Models.UserThikr;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.ThikrMediaPlayerService;
import com.HMSolutions.thikrallah.Utilities.MyDBHelper;
import com.crashlytics.android.Crashlytics;
import com.HMSolutions.thikrallah.quran.data.page.provider.madani.MadaniPageProvider;
import com.HMSolutions.thikrallah.quran.data.source.PageProvider;
import com.HMSolutions.thikrallah.BuildConfig;
import com.HMSolutions.thikrallah.quran.labs.androidquran.QuranApplication;
import com.HMSolutions.thikrallah.quran.labs.androidquran.common.QariItem;
import com.HMSolutions.thikrallah.quran.labs.androidquran.dao.audio.AudioPathInfo;
import com.HMSolutions.thikrallah.quran.labs.androidquran.dao.audio.AudioRequest;
import com.HMSolutions.thikrallah.quran.labs.androidquran.data.Constants;
import com.HMSolutions.thikrallah.quran.labs.androidquran.data.SuraAyah;
import com.HMSolutions.thikrallah.quran.labs.androidquran.service.AudioService;
import com.HMSolutions.thikrallah.quran.labs.androidquran.service.QuranDownloadService;
import com.HMSolutions.thikrallah.quran.labs.androidquran.service.util.ServiceIntentHelper;
import com.HMSolutions.thikrallah.quran.labs.androidquran.ui.PagerActivity;
import com.HMSolutions.thikrallah.quran.labs.androidquran.util.AudioUtils;
import com.HMSolutions.thikrallah.quran.labs.androidquran.util.QuranSettings;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;


public class ThikrService extends IntentService  {
    String TAG = "ThikrService";
	private final static int NOTIFICATION_ID=4;
    private final static int NOTIFICATION_ID_BACKGROUND=44;
    private final static int NOTIFICATION_ID_DOWNLOAD1=5523;
    private final static int NOTIFICATION_ID_DOWNLOAD2=412;
    private final static int NOTIFICATION_ID_DOWNLOAD3=9578;
    private AudioManager am;
    private Intent calling_intent;
    Context mcontext;
    @Inject PageProvider quranPageProvider;
    QuranSettings quransettings;
    private static final String QURAN_BASE = "quran_android/";
    private static final String AUDIO_DIRECTORY=new MadaniPageProvider().getAudioDirectoryName();
    private static final String AUDIO_EXTENSION = ".mp3";
    private static final String DATABASE_DIRECTORY=new MadaniPageProvider().getDatabaseDirectoryName();
    private static final String AYAHINFO_DIRECTORY=new MadaniPageProvider().getAyahInfoDirectoryName();
    private static final String DB_EXTENSION = ".db";
    private static final String  ZIP_EXTENSION = ".zip";
    private static final String DATABASE_BASE_URL =new MadaniPageProvider().getAudioDatabasesBaseUrl();
    public ThikrService() {
		super("service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
        Crashlytics.log("ThikrService called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //workaround androidO limitation on background services
            showForegroundNotificationan(NOTIFICATION_ID);
        }

        calling_intent=intent;
        mcontext=this.getApplicationContext();
        quransettings=QuranSettings.getInstance(mcontext);
        //update all alarms
        Intent boot_reciever = new Intent("com.HMSolutions.thikrallah.Notification.ThikrBootReceiver.android.action.broadcast");
        this.sendBroadcast(boot_reciever);
        /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                new MyAlarmsManager(mcontext).UpdateAllApplicableAlarms();
            }
        }, 10000);
        */
		Log.d(TAG,"onhandleintnet called");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        String lang=sharedPrefs.getString("language",null);
        boolean isRespectMute = sharedPrefs.getBoolean("mute_thikr_when_ringer_mute", true);
        if (lang!=null){
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }

        boolean isTimer=sharedPrefs.getBoolean("foreground_athan_timer",true);

        if(isTimer){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this.getApplicationContext(),AthanTimerService.class));
            } else {
                this.startService(new Intent(this.getApplicationContext(),AthanTimerService.class));
            }
        }

        //AthanTimerService.enqueueWork(this.getApplicationContext(), new Intent(this.getApplicationContext(),AthanTimerService.class));
        am = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		Bundle data=intent.getExtras();
		String thikrType="";
		thikrType=data.getString("com.HMSolutions.thikrallah.datatype");
		if (thikrType.equals(MainActivity.DATA_TYPE_GENERAL_THIKR)){
            MyDBHelper db = new MyDBHelper(this);
            UserThikr thikr=db.getRandomThikr();
            if (thikr==null){
                return;
            }
            int fileNumber=-1;
            if(android.text.TextUtils.isDigitsOnly(thikr.getFile()) && !thikr.getFile().equalsIgnoreCase("")){
                fileNumber=Integer.parseInt(thikr.getFile());
            }
            Log.d(TAG,"filenumber is"+fileNumber);
			//fire text chat head service
            Log.d(TAG,"calling chatheadservice");
			Intent intentChatHead=new Intent(this.getApplicationContext(), ChatHeadService.class);
			intentChatHead.putExtra("thikr", thikr.getThikrText());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intentChatHead);
            } else {
                startService(intentChatHead);
            }

			int reminderType=Integer.parseInt(sharedPrefs.getString("RemindmeThroughTheDayType", "1"));
			boolean isQuietTime=isTimeNowQuietTime();
			if (((reminderType==1 ||reminderType==2)&&isQuietTime==false&&(thikr.isBuiltIn()==true||thikr.getFile().length()>2))&&(am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL||isRespectMute==false)){
                sharedPrefs.edit().putString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_GENERAL_THIKR).commit();
                data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAY);
                Log.d(TAG,"fileNumber sent through intent is "+fileNumber);
                data.putInt("FILE", fileNumber);
                data.putString("FILE_PATH",thikr.getFile());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
                } else {
                    this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
                }

			}
            return;



		}
		if (thikrType.equals(MainActivity.DATA_TYPE_DAY_THIKR)){
			int reminderType=Integer.parseInt(sharedPrefs.getString("remindMeDayThikrType", "1"));
			if (reminderType==1){
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
				mBuilder.setContentTitle(this.getString(R.string.my_app_name))
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String NOTIFICATION_CHANNEL_ID = "ThikrService";
                    String channelName = this.getResources().getString(R.string.remember_notification);
                    NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                    chan.setSound(null,null);
                    chan.setLightColor(Color.BLUE);
                    chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                    NotificationManager  manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    assert manager != null;
                    manager.createNotificationChannel(chan);
                    mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                }
                this.startForeground(NOTIFICATION_ID,mBuilder.build());
				mNotificationManager.notify(NOTIFICATION_ID_BACKGROUND, mBuilder.build());
			}else{
				//new here

				sharedPrefs.edit().putString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_DAY_THIKR).commit();

				data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAYALL);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
                } else {
                    this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
                }
			}
            return;
		}
		if (thikrType.equals(MainActivity.DATA_TYPE_NIGHT_THIKR)){
			int reminderType=Integer.parseInt(sharedPrefs.getString("remindMeNightThikrType", "1"));
			if (reminderType==1){
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
				mBuilder.setContentTitle(this.getString(R.string.my_app_name))
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String NOTIFICATION_CHANNEL_ID = "ThikrService";
                    String channelName = this.getResources().getString(R.string.remember_notification);
                    NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                    chan.setSound(null,null);
                    chan.setLightColor(Color.BLUE);
                    chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    assert manager != null;
                    manager.createNotificationChannel(chan);
                    mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                }

                this.startForeground(NOTIFICATION_ID,mBuilder.build());
				mNotificationManager.notify(NOTIFICATION_ID_BACKGROUND, mBuilder.build());
			}else{

				sharedPrefs.edit().putString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_NIGHT_THIKR).commit();
				data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAYALL);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
                } else {
                    this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
                }

			}
            return;


		}
        if (thikrType.equals(MainActivity.DATA_TYPE_QURAN_MULK)){

            int reminderType=Integer.parseInt(sharedPrefs.getString("remindMemulkType", "1"));
            if (reminderType==1){
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                mBuilder.setContentTitle(this.getString(R.string.my_app_name))
                        .setContentText(this.getString(R.string.surat_almulk))
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true);

                mBuilder=setVisibilityPublic(mBuilder);
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mBuilder.setSound(soundUri,AudioManager.STREAM_NOTIFICATION);
                Intent launchAppIntent = new Intent(this, MainActivity.class);
                launchAppIntent.putExtra("page", 562);
                launchAppIntent.putExtra("FromNotification",true);
                launchAppIntent.putExtra("DataType", MainActivity.DATA_TYPE_QURAN);

                PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
                        0, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT
                );

                mBuilder.setContentIntent(launchAppPendingIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String NOTIFICATION_CHANNEL_ID = "ThikrService";
                    String channelName = this.getResources().getString(R.string.remember_notification);
                    NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                    chan.setSound(null,null);
                    chan.setLightColor(Color.BLUE);
                    chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    assert manager != null;
                    manager.createNotificationChannel(chan);
                    mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                }
                this.startForeground(NOTIFICATION_ID,mBuilder.build());
                mNotificationManager.notify(NOTIFICATION_ID_BACKGROUND, mBuilder.build());
            }else{
                //new here

                sharedPrefs.edit().putString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_QURAN_MULK).commit();

                data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAYALL);
                SuraAyah start = new SuraAyah(67, 1);
                SuraAyah end = new SuraAyah(67, 30);
                List<QariItem> qlist = getQariList(this);
                int qari_num=Integer.parseInt(sharedPrefs.getString("quran_readers_name","11"));
                QariItem qari=qlist.get(qari_num);

                AudioPathInfo audioPathInfo = this.getLocalAudioPathInfo(qari);
               /*
                File databaseFile=new File(audioPathInfo.getGaplessDatabase());
                if(!databaseFile.exists()){

                    Intent downloadIntent = ServiceIntentHelper.getAudioDownloadIntent(this, getGaplessDatabaseUrl(qari), audioPathInfo.getLocalDirectory(), mcontext.getString(R.string.timing_database));
                    startService(downloadIntent);
                }

*/
                if (audioPathInfo != null) {
                    // override streaming if all the files are already downloaded
                    boolean stream = false;
                    if (quransettings.shouldStream()) {
                        stream = !haveAllFiles(audioPathInfo.getUrlFormat(),audioPathInfo.getLocalDirectory(), start, end,qari.isGapless());
                    }

                    // if we're still streaming, change the base qari format in audioPathInfo
                    // to a remote url format (instead of a path to a local directory)
                    AudioPathInfo audioPath;
                    if (stream) {
                        audioPath = audioPathInfo.copy(getQariUrl(qari), audioPathInfo.getLocalDirectory(), audioPathInfo.getGaplessDatabase());

                    } else {
                        audioPath = audioPathInfo;
                        //check if the audio files are available
                        if (!haveAllFiles(audioPathInfo.getUrlFormat(),audioPathInfo.getLocalDirectory(), start, end,qari.isGapless())){

                        }
                    }


                    Log.d(TAG, "ready to play Quran");
                    if (audioPathInfo != null) {
                        AudioRequest audioRequest = new AudioRequest(start, end, qari, 0, 0, true, false, audioPath);


                        ArrayList<Intent> DownloadIntents=DownloadedNeededFiles(this,audioRequest);
                        Log.d(TAG, "DownloadIntents are "+DownloadIntents.size());
                        if (DownloadIntents.size()==0){
                            Log.d(TAG, "calling handlePlayback");
                            handlePlayback(audioRequest);
                        }else{
                            Intent RecieverIntent_=new Intent(mcontext, QuranThikrDownloadNeeds.class);
                            RecieverIntent_.putExtra("sura",start.sura);
                            RecieverIntent_.putExtra("ayah",end.ayah);
                            RecieverIntent_.putExtra("qari",qari_num);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(mcontext, NOTIFICATION_ID_DOWNLOAD1, RecieverIntent_, PendingIntent.FLAG_UPDATE_CURRENT);
                            handleRequiredDownload(pendingIntent,NOTIFICATION_ID_DOWNLOAD1);




                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                            mBuilder.setContentTitle(this.getString(R.string.my_app_name))
                                    .setContentText(this.getString(R.string.surat_almulk))
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setAutoCancel(true);

                            mBuilder=setVisibilityPublic(mBuilder);
                            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            mBuilder.setSound(soundUri,AudioManager.STREAM_NOTIFICATION);
                            Intent launchAppIntent = new Intent(this, PagerActivity.class);
                            launchAppIntent.putExtra("page", 562);

                            PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
                                    0, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT
                            );

                            mBuilder.setContentIntent(launchAppPendingIntent);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                String NOTIFICATION_CHANNEL_ID = "ThikrService";
                                String channelName = this.getResources().getString(R.string.remember_notification);
                                NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                                chan.setSound(null,null);
                                chan.setLightColor(Color.BLUE);
                                chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                assert manager != null;
                                manager.createNotificationChannel(chan);
                                mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                            }
                            this.startForeground(NOTIFICATION_ID,mBuilder.build());
                            mNotificationManager.notify(NOTIFICATION_ID_BACKGROUND, mBuilder.build());
                        }

                    }
                }
               /*
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
                } else {
                    this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
                }
                */
            }
            return;
        }
        if (thikrType.equals(MainActivity.DATA_TYPE_QURAN_KAHF)){
            sharedPrefs.edit().putInt("", Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).commit();

            int reminderType=Integer.parseInt(sharedPrefs.getString("remindMekahfType", "1"));
            if (reminderType==1){
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                mBuilder.setContentTitle(this.getString(R.string.my_app_name))
                        .setContentText(this.getString(R.string.surat_alkahf))
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true);

                mBuilder=setVisibilityPublic(mBuilder);
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mBuilder.setSound(soundUri,AudioManager.STREAM_NOTIFICATION);
                Intent launchAppIntent = new Intent(this, MainActivity.class);
                launchAppIntent.putExtra("page", 293);
                launchAppIntent.putExtra("FromNotification",true);
                launchAppIntent.putExtra("DataType", MainActivity.DATA_TYPE_QURAN);

                PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
                        0, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT
                );

                mBuilder.setContentIntent(launchAppPendingIntent);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String NOTIFICATION_CHANNEL_ID = "ThikrService";
                    String channelName = this.getResources().getString(R.string.remember_notification);
                    NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                    chan.setSound(null,null);
                    chan.setLightColor(Color.BLUE);
                    chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    assert manager != null;
                    manager.createNotificationChannel(chan);
                    mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                }
                mNotificationManager.notify(NOTIFICATION_ID_BACKGROUND, mBuilder.build());
                this.startForeground(NOTIFICATION_ID,mBuilder.build());
            }else{

                sharedPrefs.edit().putString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_QURAN_MULK).commit();

                data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAYALL);
                SuraAyah start = new SuraAyah(18, 1);
                SuraAyah end = new SuraAyah(18, 110);
                List<QariItem> qlist = getQariList(this);
                int qari_num=Integer.parseInt(sharedPrefs.getString("quran_readers_name","11"));
                QariItem qari=qlist.get(qari_num);

                AudioPathInfo audioPathInfo = this.getLocalAudioPathInfo(qari);
                if (audioPathInfo != null) {
                    // override streaming if all the files are already downloaded
                    boolean stream = false;
                    if (quransettings.shouldStream()) {
                        stream = !haveAllFiles(audioPathInfo.getUrlFormat(),audioPathInfo.getLocalDirectory(), start, end,qari.isGapless());
                    }

                    // if we're still streaming, change the base qari format in audioPathInfo
                    // to a remote url format (instead of a path to a local directory)
                    AudioPathInfo audioPath;
                    if (stream) {
                        audioPath = audioPathInfo.copy(getQariUrl(qari), audioPathInfo.getLocalDirectory(), audioPathInfo.getGaplessDatabase());

                    } else {
                        audioPath = audioPathInfo;
                        //check if the audio files are available
                        if (!haveAllFiles(audioPathInfo.getUrlFormat(),audioPathInfo.getLocalDirectory(), start, end,qari.isGapless())){

                        }
                    }


                    Log.d(TAG, "ready to play Quran");
                    if (audioPathInfo != null) {
                        AudioRequest audioRequest = new AudioRequest(start, end, qari, 0, 0, true, false, audioPath);


                        ArrayList<Intent> DownloadIntents=DownloadedNeededFiles(this,audioRequest);
                        Log.d(TAG, "DownloadIntents are "+DownloadIntents.size());
                        if (DownloadIntents.size()==0){
                            Log.d(TAG, "calling handlePlayback");
                            handlePlayback(audioRequest);
                        }else{
                            Intent RecieverIntent_=new Intent(mcontext, QuranThikrDownloadNeeds.class);
                            RecieverIntent_.putExtra("sura",start.sura);
                            RecieverIntent_.putExtra("ayah",end.ayah);
                            RecieverIntent_.putExtra("qari",qari_num);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(mcontext, NOTIFICATION_ID_DOWNLOAD1, RecieverIntent_, PendingIntent.FLAG_UPDATE_CURRENT);
                            handleRequiredDownload(pendingIntent,NOTIFICATION_ID_DOWNLOAD1);




                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
                            mBuilder.setContentTitle(this.getString(R.string.my_app_name))
                                    .setContentText(this.getString(R.string.surat_almulk))
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setAutoCancel(true);

                            mBuilder=setVisibilityPublic(mBuilder);
                            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            mBuilder.setSound(soundUri,AudioManager.STREAM_NOTIFICATION);
                            Intent launchAppIntent = new Intent(this, PagerActivity.class);
                            launchAppIntent.putExtra("page", 562);

                            PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
                                    0, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT
                            );

                            mBuilder.setContentIntent(launchAppPendingIntent);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                String NOTIFICATION_CHANNEL_ID = "ThikrService";
                                String channelName = this.getResources().getString(R.string.remember_notification);
                                NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                                chan.setSound(null,null);
                                chan.setLightColor(Color.BLUE);
                                chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                assert manager != null;
                                manager.createNotificationChannel(chan);
                                mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
                            }
                            this.startForeground(NOTIFICATION_ID,mBuilder.build());
                            mNotificationManager.notify(NOTIFICATION_ID_BACKGROUND, mBuilder.build());
                        }

                    }
                }
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

            if ((reminderType==1)||(am.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
                    ||(am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE)){//vibrate

                Log.d(TAG,"vibrating now");
                if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE){
                    vibrate();
                }
                Log.d(TAG,"calling chatheadservice");
                Intent intentChatHead=new Intent(this.getApplicationContext(), ChatHeadService.class);
                intentChatHead.putExtra("thikr", athan);
                intentChatHead.putExtra("isAthan",true);
                //startService(intentChatHead);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //startForegroundService(intentChatHead);
                    startForegroundService(intentChatHead);
                } else {
                    startService(intentChatHead);
                }

            }else{
                sharedPrefs.edit().putString("com.HMSolutions.thikrallah.datatype", thikrType).commit();
                data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAY);
                int file=reminderType;
                Log.d(TAG,"fileNumber sent through intent is "+file);
                data.putInt("FILE", file);
                data.putInt("reminderType",reminderType);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
                } else {
                    this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
                }
            }


        }

	}
    public void handleRequiredDownload(PendingIntent launchAppPendingIntent,int notification_id) {


        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "com.HMSolutions.thikrallah.Notification.DownloadQuran";
            String channelName = this.getResources().getString(R.string.notification_channel_download);

            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            chan.setSound(null,null);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(chan);
            mBuilder = new NotificationCompat.Builder(mcontext,NOTIFICATION_CHANNEL_ID);
        }else{
            mBuilder = new NotificationCompat.Builder(mcontext);
        }

        mBuilder.setContentTitle(this.getString(R.string.my_app_name))
                .setContentText(mcontext.getResources().getString(R.string.quran_thikr_need_files))
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true);
        mBuilder=setVisibilityPublic(mBuilder);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(soundUri,AudioManager.STREAM_NOTIFICATION);

        mBuilder.setContentIntent(launchAppPendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "ThikrService";
            String channelName = this.getResources().getString(R.string.remember_notification);
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            chan.setSound(null,null);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
        }
        Log.d(TAG,"showing notifiaction");

        this.startForeground(NOTIFICATION_ID,mBuilder.build());
        mNotificationManager.notify(NOTIFICATION_ID_BACKGROUND, mBuilder.build());
    }
    private void showForegroundNotificationan(int ID){
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(this.getString(R.string.my_app_name))
                .setContentText(this.getString(R.string.my_app_name))
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true);
        mBuilder=setVisibilityPublic(mBuilder);
        mBuilder.setSound(null);
        Intent launchAppIntent = new Intent(this, MainActivity.class);
        PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
                0, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT
        );
        mBuilder.setContentIntent(launchAppPendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "ThikrService";
            String channelName = this.getResources().getString(R.string.remember_notification);
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            chan.setSound(null,null);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
        }

        this.startForeground(ID,mBuilder.build());
    }
    private boolean makeQuranDatabaseDirectory(Context context) {
        return makeDirectory(getQuranDatabaseDirectory(context));
    }

    private boolean makeQuranAyahDatabaseDirectory(Context context) {
        return makeQuranDatabaseDirectory(context) &&
                makeDirectory(getQuranAyahDatabaseDirectory(context));
    }
    public String getQuranAyahDatabaseDirectory(Context context) {
        String base = getQuranBaseDirectory(context);
        return base == null ? null : base + File.separator + AYAHINFO_DIRECTORY;
    }
    private boolean makeDirectory(String path) {
        if (path == null) {
            return false;
        }

        File directory = new File(path);
        return (directory.exists() && directory.isDirectory()) || directory.mkdirs();
    }
    public String getQuranDatabaseDirectory(Context context) {
        String base = getQuranBaseDirectory(context);
        return (base == null) ? null : base + DATABASE_DIRECTORY;
    }

    private ArrayList<Intent> DownloadedNeededFiles(Context context, AudioRequest request){
        ArrayList<Intent> downloadIntents=new ArrayList<Intent>();
        QariItem qari = request.getQari();
        AudioPathInfo audioPathInfo = request.getAudioPathInfo();
        String path = audioPathInfo.getLocalDirectory();
        String gaplessDb = audioPathInfo.getGaplessDatabase();
        if (gaplessDb != null && !new File(gaplessDb).exists()) {

            Intent DatabaseIntent=getDownloadIntent(context,
                    getGaplessDatabaseUrl(qari),
                    path,
                    context.getString(R.string.timing_database));
            downloadIntents.add(DatabaseIntent);
        } else if (!request.getShouldStream() &&
                shouldDownloadBasmallah(path,
                        request.getStart(),
                        request.getEnd(),
                        qari.isGapless())) {

            String title = getNotificationTitle(
                    context, request.getStart(), request.getStart(), qari.isGapless());
            Intent beslmalahIntent = getDownloadIntent(context, getQariUrl(qari), path, title);

            beslmalahIntent.putExtra(QuranDownloadService.EXTRA_START_VERSE, request.getStart());
            beslmalahIntent.putExtra(QuranDownloadService.EXTRA_END_VERSE, request.getStart());
            downloadIntents.add(beslmalahIntent);

        } else if (!request.getShouldStream() &&
                !haveAllFiles(audioPathInfo.getUrlFormat(),audioPathInfo.getLocalDirectory(), request.getStart(), request.getEnd(),qari.isGapless())) {

            String title = getNotificationTitle(
                    context, request.getStart(), request.getEnd(), qari.isGapless());
            Intent AudioIntent=getDownloadIntent(context, getQariUrl(qari), path, title);
            AudioIntent.putExtra(QuranDownloadService.EXTRA_START_VERSE, request.getStart());
            AudioIntent.putExtra(QuranDownloadService.EXTRA_END_VERSE, request.getEnd());
            AudioIntent.putExtra(QuranDownloadService.EXTRA_IS_GAPLESS, qari.isGapless());
            downloadIntents.add(AudioIntent);

        }
        return downloadIntents;
    }
    private boolean shouldDownloadBasmallah( String baseDirectory,
                                SuraAyah start,
                                SuraAyah end,
                                Boolean isGapless)  {
        if (isGapless) {
            return false;
        }

        if (!baseDirectory.isEmpty()) {
            File f = new File(baseDirectory);
            if (f.exists()) {
                String filename = "1" + File.separator + 1 + AUDIO_EXTENSION;
                f = new File(baseDirectory + File.separator + filename);
                if (f.exists()) {
                    Timber.d("already have basmalla...");
                    return false;
                }
            } else {
                f.mkdirs();
            }
        }

        return doesRequireBasmallah(start, end);
    }

    private boolean doesRequireBasmallah(SuraAyah minAyah, SuraAyah maxAyah) {
        Timber.d("seeing if need basmalla...");

        for (int i = minAyah.sura; i <= maxAyah.sura; i++) {
            int firstAyah;
            if (i == minAyah.sura) {
                firstAyah = minAyah.ayah;
            } else {
                firstAyah = 1;
            }

            if (firstAyah == 1 && i != 1 && i != 9) {
                return true;
            }
        }

        return false;
    }
    public String getSuraName(Context context, int sura, boolean wantPrefix, boolean wantTranslation) {
        if (sura < Constants.SURA_FIRST ||
                sura > Constants.SURA_LAST) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        String[] suraNames = context.getResources().getStringArray(com.HMSolutions.thikrallah.R.array.sura_names);
        if (wantPrefix) {
            builder.append(context.getString(com.HMSolutions.thikrallah.R.string.quran_sura_title, suraNames[sura - 1]));
        } else {
            builder.append(suraNames[sura - 1]);
        }
        if (wantTranslation) {
            String translation = context.getResources().getStringArray(com.HMSolutions.thikrallah.R.array.sura_names_translation)[sura - 1];
            if (!TextUtils.isEmpty(translation)) {
                // Some sura names may not have translation
                builder.append(" (");
                builder.append(translation);
                builder.append(")");
            }
        }

        return builder.toString();
    }
    public String getNotificationTitle(Context context,
                                       SuraAyah minVerse,
                                       SuraAyah maxVerse,
                                       boolean isGapless) {
        int minSura = minVerse.sura;
        int maxSura = maxVerse.sura;

        String notificationTitle =
                getSuraName(context, minSura, true, false);
        if (isGapless) {
            // for gapless, don't show the ayah numbers since we're
            // downloading the entire sura(s).
            if (minSura == maxSura) {
                return notificationTitle;
            } else {
                return notificationTitle + " - " +
                        getSuraName(context, maxSura, true, false);
            }
        }

        int maxAyah = maxVerse.ayah;
        if (maxAyah == 0) {
            maxSura--;
            maxAyah = getNumAyahs(maxSura);
        }

        if (minSura == maxSura) {
            if (minVerse.ayah == maxAyah) {
                notificationTitle += " (" + maxAyah + ")";
            } else {
                notificationTitle += " (" + minVerse.ayah +
                        "-" + maxAyah + ")";
            }
        } else {
            notificationTitle += " (" + minVerse.ayah +
                    ") - " + getSuraName(context, maxSura, true, false) +
                    " (" + maxAyah + ")";
        }

        return notificationTitle;
    }

    private Intent getDownloadIntent(Context context,
                                  String url,
                                   String destination,
                                  String title) {
        return ServiceIntentHelper.getAudioDownloadIntent(context, url, destination, title);
    }
    private String getGaplessDatabaseUrl( QariItem qari) {
        if (!qari.isGapless() || qari.getDatabaseName() == null) {
            return null;
        }

        String dbName = qari.getDatabaseName() + ZIP_EXTENSION;
        return DATABASE_BASE_URL + "/" + dbName;
    }
    private String getQariUrl(QariItem item) {
        if (item.isGapless()) {
            return item.getUrl() + "%03d" + AudioUtils.AUDIO_EXTENSION;
        } else {
            return item.getUrl() + "%03d%03d" + AudioUtils.AUDIO_EXTENSION;
        }
    }
    private List<QariItem> getQariList( Context context) {
        Resources resources = context.getResources();
        String[] shuyookh = resources.getStringArray(R.array.quran_readers_name);
        String[]paths = resources.getStringArray(R.array.quran_readers_path);
        String[]urls = resources.getStringArray(R.array.quran_readers_urls);
        String[]databases = resources.getStringArray(R.array.quran_readers_db_name);
        int[] hasGaplessEquivalent = resources.getIntArray(R.array.quran_readers_have_gapless_equivalents);
        List<QariItem> items = new ArrayList<QariItem>();

        for (int i=0;i<shuyookh.length;i++ ) {
                items.add(new QariItem(i, shuyookh[i], urls[i], paths[i], databases[i]));
        }

        return items;
    }
    public void handlePlayback(AudioRequest request) {
        boolean needsPermissionToDownloadOver3g = true;
        final Intent intent = new Intent(this, AudioService.class);
        intent.setAction(AudioService.ACTION_PLAYBACK);

        if (request != null) {
            intent.putExtra(AudioService.EXTRA_PLAY_INFO, request);
            intent.putExtra("isFromService",true);
        }

        Crashlytics.log("starting service for audio playback");
        Log.d(TAG,"starting service for audio playback");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }
    private boolean isSDCardMounted() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public String getQuranBaseDirectory(Context context) {
        String basePath = QuranSettings.getInstance(context).getAppCustomLocation();

        if (!isSDCardMounted()) {
            // if our best guess suggests that we won't have access to the data due to the sdcard not
            // being mounted, then set the base path to null for now.
            if (basePath == null || basePath.equals(
                    Environment.getExternalStorageDirectory().getAbsolutePath()) ||
                    (basePath.contains(BuildConfig.APPLICATION_ID) && context.getExternalFilesDir(null) == null)) {
                basePath = null;
            }
        }

        if (basePath != null) {
            if (!basePath.endsWith(File.separator)) {
                basePath += File.separator;
            }
            return basePath + QURAN_BASE;
        }
        return null;
    }
    private boolean haveAllFiles(String baseUrl,
                     String path,
                     SuraAyah start,
                     SuraAyah end,
                     Boolean isGapless) {
        if (path.isEmpty()) {
            return false;
        }

        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
            return false;
        }

        int startSura = start.sura;
        int startAyah = start.ayah;

        int endSura = end.sura;
        int endAyah = end.ayah;

        if (endSura < startSura || (endSura == startSura && endAyah < startAyah)) {
            throw new IllegalStateException("End isn't larger than the start");
        }
        int lastAyah;
        int firstAyah;
        for (int i = startSura; i<=endSura;i++) {
            if (i == endSura) {
                 lastAyah =endAyah;
            } else {
                 lastAyah = getNumAyahs(i) ;
            }
           if (i == startSura) {
                firstAyah = startAyah;
           } else {
                firstAyah = 1;
           }

            if (isGapless) {
                if (i == endSura && endAyah == 0) {
                    continue;
                }
                String fileName = String.format(Locale.US, baseUrl, i);
                Timber.d("gapless, checking if we have %s", fileName);
                f = new File(fileName);
                if (!f.exists()) {
                    return false;
                }
                continue;
            }

            Timber.d("not gapless, checking each ayah...");
            for (int j = firstAyah; j<=lastAyah;j++) {
                String filename = i + File.separator + j + AUDIO_EXTENSION;
                f =new File(path + File.separator + filename);
                if (!f.exists()) {
                    return false;
                }
            }
        }

        return true;
    }
    public int getNumAyahs(int sura) {
        if (sura==67){
            return 30;
        }else if (sura == 18){
            return 110;
        }else{
            return 1;
        }
    }
    @Nullable
    private String getQuranAudioDirectory(Context context){
        String path = getQuranBaseDirectory(context);
        if (path == null) {
            return null;
        }
        path += AUDIO_DIRECTORY;
        File dir = new File(path);
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }
        return path + File.separator;
    }
    private String getLocalQariUrl(Context context,QariItem item) {
        String rootDirectory = getQuranAudioDirectory(this);
        if (rootDirectory == null){
            return null;
        } else{
            return rootDirectory + item.getPath();
        }
    }
    private String getQariDatabasePathIfGapless(Context context,  QariItem item) {
        String databaseName = item.getDatabaseName();
        if (databaseName != null) {
            String path = getLocalQariUrl(context, item);
            if (path != null) {
                databaseName = path + File.separator + databaseName + DB_EXTENSION;
            }
        }
        return databaseName;
    }
    private AudioPathInfo getLocalAudioPathInfo(QariItem item) {
        String databaseName = item.getDatabaseName();
        if (databaseName != null) {
            String localPath = getLocalQariUrl(this, item);
            if (localPath != null) {
                String databasePath = getQariDatabasePathIfGapless(this, item);
                String urlFormat;
                if (databasePath == null || databasePath.isEmpty()) {
                    urlFormat = localPath + File.separator + "%d" + File.separator +
                            "%d" + AudioUtils.AUDIO_EXTENSION;
                } else {
                    urlFormat = localPath + File.separator + "%03d" + AudioUtils.AUDIO_EXTENSION;
                }
                return new AudioPathInfo(urlFormat, localPath, databasePath);
            }
        }
        return null;
    }
    private NotificationCompat.Builder setVisibilityPublic(NotificationCompat.Builder inotificationBuilder){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            inotificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
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
        Log.d(TAG,"vibrating method here");

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
Log.d(TAG,"calling on destroy");
        super.onDestroy();

    }
}