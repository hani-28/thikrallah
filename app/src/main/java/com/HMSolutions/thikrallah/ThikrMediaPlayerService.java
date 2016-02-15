package com.HMSolutions.thikrallah;



import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.HMSolutions.thikrallah.Notification.MyAlarmsManager;
import com.HMSolutions.thikrallah.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;


public class ThikrMediaPlayerService extends Service implements OnCompletionListener,
AudioManager.OnAudioFocusChangeListener{
	public static final int MEDIA_PLAYER_PAUSE=1;
	public static final int MEDIA_PLAYER_RESET=2;
	public static final int MEDIA_PLAYER_PLAY=3;
	public static final int MEDIA_PLAYER_PLAYALL=4;
	public static final int MEDIA_PLAYER_ISPLAYING=5;
	AudioManager am;
	
	private MediaPlayer player;
	//public int counter=0;
	public int currentThikrCounter=0;
	private boolean isPaused;
	private int NOTIFICATION_ID=23;
    private int currentPlaying;
    private String ThikrType;


    @Override
	public void onCreate() {
		initMediaPlayer();
		//below is wip
		//initNotification();

	}

	private void initNotification() {
		// Create remote view and set bigContentView.
		//RemoteViews expandedView = new RemoteViews(this.getPackageName(), R.layout.thikr_notification);
		// Set Notification Title
		
		// Set Notification Text
	//	expandedView.setTextViewText(R.id.title,  getString(R.string.app_name));
	//	expandedView.setTextViewText(R.id.subtitle,  getString(R.string.now_playing)+" "+getThikrTypeString(getThikrType()));
		

		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);
		if (getThikrType()!=MainActivity.DATA_TYPE_GENERAL_THIKR){
            resultIntent.putExtra("FromNotification", true);
            resultIntent.putExtra("DataType", this.getThikrType());
        }

		
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
				0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT
				);
		
		
		
		
		////
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
		//below line is for custom layout
		//.setContent(expandedView)
		.setSmallIcon(R.drawable.ic_launcher)		        
		.setAutoCancel(true)
		.setContentTitle(getString(R.string.app_name))
		.setContentText(this.getString(R.string.now_playing) + " " + getThikrTypeString(this.getThikrType()));

	
		mBuilder.setContentIntent(launchAppPendingIntent);
		Notification notification = mBuilder.build();
		startForeground(NOTIFICATION_ID, notification);
		//mNotificationManager.notify(NOTIFICATION_ID,notification );

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getExtras().getString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_DAY_THIKR).equalsIgnoreCase(MainActivity.DATA_TYPE_GENERAL_THIKR)&&this.isPlaying()){
            new MyAlarmsManager(this).UpdateAllApplicableAlarms();
            return Service.START_NOT_STICKY;
        }else{
            this.setThikrType(intent.getExtras().getString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_DAY_THIKR));
        }
        if (this.getThikrType().equalsIgnoreCase(MainActivity.DATA_TYPE_GENERAL_THIKR)){
            new MyAlarmsManager(this).UpdateAllApplicableAlarms();
        }
        initNotification();
        Bundle data=intent.getExtras();
		int action =data.getInt("ACTION", -1);
		Log.d("media1", "action " + action);
		switch (action){
		case MEDIA_PLAYER_PAUSE:
			Log.d("media1","pause called");
			this.pausePlayer();
			break;

		case MEDIA_PLAYER_RESET:
			Log.d("media1","reset called");
			this.resetPlayer();
			
			this.stopForeground(true);
			this.stopSelf();
			break;
		case MEDIA_PLAYER_PLAYALL:
			Log.d("media1", "playall called");
			this.playAll();
         //   if ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE)).getCallState()!=TelephonyManager.CALL_STATE_OFFHOOK;
			break;
		case MEDIA_PLAYER_ISPLAYING:
			Log.d("media1","isplaying called");
			this.isPlaying();
			break;
		case MEDIA_PLAYER_PLAY:

			int file=data.getInt("FILE");
			Log.d("media1","play "+file+" called");
			this.play(file);
			break;
		}
		return Service.START_NOT_STICKY;
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub

		return null;//hanihanimBinder;
	}





	/** method for clients */



	/**
	 * @return the currentPlaying
	 */
	public int getCurrentPlaying() {
		return currentPlaying;
	}
	/**
	 * @param icurrentPlaying the currentPlaying to set
	 */
	public void setCurrentPlaying(int icurrentPlaying) {
        currentPlaying=icurrentPlaying;
	}

    public int getAudioFocusRequestType(){
        if (this.getThikrType().equalsIgnoreCase(MainActivity.DATA_TYPE_GENERAL_THIKR)){
            return AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
        }
        return AudioManager.AUDIOFOCUS_GAIN;
    }

	public void play(int fileNumber){
		this.initMediaPlayer();
		setCurrentPlaying(fileNumber);
		player.setOnCompletionListener(this);
		AssetFileDescriptor afd;
		try {

            Log.d("media1 player", "file number is "+fileNumber);
			afd = this.getApplicationContext().getAssets().openFd(this.getThikrType()+"/"+(fileNumber)+".mp3");
            Log.d("media1 player", "file path  is " + this.getThikrType() + "/" + (fileNumber) + ".mp3");
            player.reset();
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			player.prepare();

			int ret=am.requestAudioFocus(this,
					// Use the music stream.
					AudioManager.STREAM_MUSIC,
					// Request permanent focus.
                    getAudioFocusRequestType());
			if (ret==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                setVolume();
                player.start();
            }else{
                am.abandonAudioFocus(this);
                this.stopForeground(true);
                this.stopSelf();
            }


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String[] getThikrArray(){
		String[] numbers_text = null;
		if (this.getThikrType().equals(MainActivity.DATA_TYPE_DAY_THIKR)){
			numbers_text = getResources().getStringArray(R.array.MorningThikr);
		}
		if (this.getThikrType().equals(MainActivity.DATA_TYPE_NIGHT_THIKR)){
			numbers_text = getResources().getStringArray(R.array.NightThikr); 
		}
        if (this.getThikrType().equals(MainActivity.DATA_TYPE_GENERAL_THIKR)){
            numbers_text = getResources().getStringArray(R.array.GeneralThikr);
        }

		return numbers_text;
	}
	public void playAll( ) {


		if (isPaused==false){  //mediaplayer was stopped

			AssetFileDescriptor afd;
			try {
				Log.d("media1 player", "current playing is "+getCurrentPlaying());
				afd = this.getApplicationContext().getAssets().openFd(getThikrType()+"/"+this.getCurrentPlaying()+".mp3");
				player.reset();
				this.initMediaPlayer();
				player.setAudioStreamType(AudioManager.STREAM_MUSIC);
				player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
				player.prepare();
				Log.d("media1 player", "current playing was prepared successfully "+getCurrentPlaying());
			} catch (IOException e) {
				if(this.getCurrentPlaying()<1){
					setCurrentPlaying(1);
				}
				if (this.getCurrentPlaying()>this.getThikrArray().length){
					setCurrentPlaying(this.getThikrArray().length);
				}
				//setCurrentPlaying(1);
				//e.printStackTrace();

			}

		}
		isPaused=false;



		player.setOnCompletionListener(this);
		int ret= am.requestAudioFocus(this,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                getAudioFocusRequestType());
        if (ret==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            setVolume();
            player.start();
        }

	}

    @Override
    public void onDestroy(){
        Log.d("media1 player", "ondestroy called");
        if (player != null){
            player.release();
            player=null;
        }
        am.abandonAudioFocus(this);
        this.stopForeground(true);
        this.stopSelf();
        super.onDestroy();

    }
	public int getCurrentThikrRepeat(){
		int repeat=1;
		String currentThikr=this.getThikrArray()[this.getCurrentPlaying()-1];

		Pattern pattern = Pattern.compile("[\\d]+");
		Matcher matcher = pattern.matcher(currentThikr);
		if (matcher.find())
		{


			repeat =Integer.parseInt(matcher.group(0));
		}else{
			repeat= 1;
		}
		return repeat;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mp.reset();
		Log.d("media1 player", "oncomplete called");

        Log.d("media1 player", "thikrtype is "+this.getThikrType()+" vs "+MainActivity.DATA_TYPE_GENERAL_THIKR);
		currentThikrCounter++;
		if (this.getThikrType().equalsIgnoreCase(MainActivity.DATA_TYPE_GENERAL_THIKR)){;
            this.resetPlayer();

            this.stopForeground(true);
            this.stopSelf();
        }
        if (this.getCurrentPlaying()>=getThikrArray().length&&currentThikrCounter>=getCurrentThikrRepeat()){

            setCurrentPlaying(1);
			currentThikrCounter=0;
			
			this.resetPlayer();
			
			this.stopForeground(true);
			this.stopSelf();
			return;
		}else{
			if (currentThikrCounter>=getCurrentThikrRepeat()){
				currentThikrCounter=0;
				setCurrentPlaying(this.getCurrentPlaying()+1);
			}else{
				setCurrentPlaying(this.getCurrentPlaying());
			}

			playAll();
		}
	}
	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class MyBinder extends Binder {
		ThikrMediaPlayerService getService() {
			// Return this instance of LocalService so clients can call public methods
			return ThikrMediaPlayerService.this;
		}
	}

	private String getThikrType(){
		return ThikrType;

	}
    private void setThikrType(String iThikrType){
        ThikrType=iThikrType;

    }
	private String getThikrTypeString(String thikTypeConstant){
		if (thikTypeConstant.equals(MainActivity.DATA_TYPE_DAY_THIKR)){
			return this.getString(R.string.morningThikr);
		}else if(thikTypeConstant.equals(MainActivity.DATA_TYPE_NIGHT_THIKR)){
			return this.getString(R.string.nightThikr);
		}else{
            return "تذكير بالله";
        }

	}

	public void resetPlayer() {
		if(this.player!=null){
			this.player.stop();
			this.player.reset();
			//am.abandonAudioFocus(this);
			//player.release();
			//this.stopSelf();
		}

	}

	public boolean isPlaying() {
		boolean isPlaying=false;
		if (player!=null){
			try{
				isPlaying=this.player.isPlaying();
			}catch (Exception e){

			}

		}
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		sharedPrefs.edit().putBoolean("ISPLAYING", isPlaying);

		return isPlaying;

	}

	public void pausePlayer() {
		if (this.isPlaying()){
			this.player.pause();
			isPaused=true;
		}
		//am.abandonAudioFocus(this);


	}


	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_GAIN:
			// resume playback
			Log.d("media1 player", "gained focus");
			if (player == null){
				initMediaPlayer();
			}else if (!isPlaying()){

                player.start();
			}
            this.setVolume();


			break;

		case AudioManager.AUDIOFOCUS_LOSS:
			// Lost focus for an unbounded amount of time: stop playback and release media player
			Log.d("media1 player", "lost focus");
			if (isPlaying()){
				player.stop();
			}
            Log.d("media1","reseting player and releasing service");
            this.resetPlayer();
            this.stopForeground(true);
            this.stopSelf();
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			// Lost focus for a short time, but we have to stop
			// playback. We don't release the media player because playback
			// is likely to resume
			Log.d("media1 player", "transient loss of  focus");
			if (isPlaying()) player.pause();
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
			// Lost focus for a short time, but it's ok to keep playing
			// at an attenuated level
			Log.d("media1 player", "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
			if (isPlaying()) {

				player.setVolume(0.1f, 0.1f);
			}
			break;
		}
	}
    private void setVolume(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        boolean isRespectMute = sharedPrefs.getBoolean("mute_thikr_when_ringer_mute",true);
        int volumeLevel =sharedPrefs.getInt("volume",100);
        if ((am.getRingerMode()==AudioManager.RINGER_MODE_SILENT ||am.getRingerMode()==AudioManager.RINGER_MODE_VIBRATE) && isRespectMute==true){
            player.setVolume(0f,0f);
        }else{
            int maxVolume = 101;
            float volume=(float)(1-Math.log(maxVolume-volumeLevel)/Math.log(maxVolume));
            player.setVolume(volume,volume);
        }
    }
	private boolean initMediaPlayer() {
		if(this.isPlaying()){
			Log.d("media1 player", "initiMediaPlayer is called and player is not null");
			this.resetPlayer();
		}
		//this.setCurrentPlaying(1);
		if (player==null){
			Log.d("media1 player", "initiMediaPlayer is called and player is null");
			player = new MediaPlayer();
			player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
			am = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
			/*
            int ret=am.requestAudioFocus(this,
					// Use the music stream.
					AudioManager.STREAM_MUSIC,
					// Request permanent focus.
                    getAudioFocusRequestType());
            Log.d("media1 player", "audio focus requested");
			if (ret==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                Log.d("media1 player", "audio focus granted");
                setVolume();
				return true;
			}else {
                Log.d("media1 player", "audio focus denied");
				return false;
			}
			*/
		}
		return true;

	}


}