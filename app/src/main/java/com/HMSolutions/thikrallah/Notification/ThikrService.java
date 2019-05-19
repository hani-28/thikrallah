package com.HMSolutions.thikrallah.Notification;



import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.Models.UserThikr;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.ThikrMediaPlayerService;
import com.HMSolutions.thikrallah.Utilities.MyDBHelper;
import com.crashlytics.android.Crashlytics;
import com.thikrallah.quran.data.page.provider.madani.MadaniPageProvider;
import com.thikrallah.quran.data.source.PageProvider;
import com.thikrallah.quran.labs.androidquran.BuildConfig;
import com.thikrallah.quran.labs.androidquran.common.QariItem;
import com.thikrallah.quran.labs.androidquran.dao.audio.AudioPathInfo;
import com.thikrallah.quran.labs.androidquran.dao.audio.AudioRequest;
import com.thikrallah.quran.labs.androidquran.data.Constants;
import com.thikrallah.quran.labs.androidquran.data.QuranInfo;
import com.thikrallah.quran.labs.androidquran.data.SuraAyah;
import com.thikrallah.quran.labs.androidquran.presenter.audio.AudioPresenter;
import com.thikrallah.quran.labs.androidquran.service.AudioService;
import com.thikrallah.quran.labs.androidquran.ui.PagerActivity;
import com.thikrallah.quran.labs.androidquran.util.AudioUtils;
import com.thikrallah.quran.labs.androidquran.util.QuranFileUtils;
import com.thikrallah.quran.labs.androidquran.util.QuranSettings;
import com.thikrallah.quran.labs.androidquran.widgets.AudioStatusBar;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import javax.inject.Inject;

import timber.log.Timber;


public class ThikrService extends IntentService  {
    String TAG = "ThikrService";
	private final static int NOTIFICATION_ID=4;
    private AudioManager am;
    private Intent calling_intent;
    Context mcontext;
    @Inject PageProvider quranPageProvider;
    private static final String QURAN_BASE = "quran_android/";
    private static final String AUDIO_DIRECTORY="audio";
    private static final String AUDIO_EXTENSION = ".mp3";

    private static final String DB_EXTENSION = ".db";
    private static final String  ZIP_EXTENSION = ".zip";
    public ThikrService() {
		super("service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
        calling_intent=intent;
        //TODO: Add channels here?
        mcontext=this.getApplicationContext();
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

				mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
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

				mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());	
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
                Intent launchAppIntent = new Intent(this, PagerActivity.class);
                launchAppIntent.putExtra("page", 562);

                PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
                        0, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT
                );

                mBuilder.setContentIntent(launchAppPendingIntent);

                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            }else{
                //new here

                sharedPrefs.edit().putString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_QURAN_MULK).commit();

                data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAYALL);

                //TODO: Fix below to use new Quran Functionality
                SuraAyah start = new SuraAyah(67, 1);
                SuraAyah end = new SuraAyah(67, 30);
                List<QariItem> qlist = getQariList(this);
                QariItem qari=qlist.get(0);

                AudioPathInfo audioPathInfo = this.getLocalAudioPathInfo(qari);
                Log.d(TAG,"ready to play Quran");
                if (audioPathInfo != null) {
                    AudioRequest audioRequest = new AudioRequest(start, end, qari, 0, 0, true, false, audioPathInfo);
                    Log.d(TAG,"calling handlePlayback");
                    handlePlayback(audioRequest);
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
                Intent launchAppIntent = new Intent(this, PagerActivity.class);
                launchAppIntent.putExtra("page", 293);


                PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
                        0, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT
                );

                mBuilder.setContentIntent(launchAppPendingIntent);

                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            }else{
                //new here

                sharedPrefs.edit().putString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_QURAN_KAHF).commit();
                //TODO: Fix below to use new Quran Functionality
                data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAYALL);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
                } else {
                    this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
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
                    startService(intentChatHead);
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
        startService(intent);
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

        if (endSura < startSura || endSura == startSura && endAyah < startAyah) {
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

        super.onDestroy();

    }
}