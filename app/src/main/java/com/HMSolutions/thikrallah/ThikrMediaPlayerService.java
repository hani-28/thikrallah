package com.HMSolutions.thikrallah;


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.HMSolutions.thikrallah.Notification.MyAlarmsManager;
import com.HMSolutions.thikrallah.Notification.ThikrMediaBroadcastReciever;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.os.Vibrator;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

public class ThikrMediaPlayerService extends Service implements OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {
    public static final int MEDIA_PLAYER_PAUSE = 1;
    public static final int MEDIA_PLAYER_RESET = 2;
    public static final int MEDIA_PLAYER_PLAY = 3;
    public static final int MEDIA_PLAYER_PLAYALL = 4;
    public static final int MEDIA_PLAYER_ISPLAYING = 5;
    public static final int MEDIA_PLAYER_INNCREMENT = 6;
    public static final int MEDIA_PLAYER_CHANGE_VOLUME = 7;
    public static final int MEDIA_PLAYER_RESUME = 8;
    AudioManager am;

    private MediaPlayer player;
    //public int counter=0;
    public int currentThikrCounter = 0;
    private boolean isPaused;
    private int NOTIFICATION_ID = 23;
    private int currentPlaying;
    private String ThikrType;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat mController;
    private boolean overRideRespectMute=false;
    private boolean isUserAction=true;
    private NotificationCompat.Builder notificationBuilder;


    @Override
    public void onCreate() {
        super.onCreate();
        initMediaPlayer();
        //below is wip
        //initNotification();

    }

    private void initNotification() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        if (getThikrType() != MainActivity.DATA_TYPE_GENERAL_THIKR) {
            resultIntent.putExtra("FromNotification", true);
            resultIntent.putExtra("DataType", this.getThikrType());
        }


        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
                0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT
        );


        notificationBuilder = new NotificationCompat.Builder(this);

        notificationBuilder
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(this.getString(R.string.now_playing) + " " + getThikrTypeString(this.getThikrType()))

                .setContentIntent(launchAppPendingIntent);

        updateActions();


    }

    private void updateActions() {
        if (notificationBuilder != null) {
            notificationBuilder.mActions.clear();
            if (this.isPlaying()) {
                Log.d("mediastyle", "show pause & stop");
                addAction(notificationBuilder, "pause", R.drawable.ic_media_pause);
                addAction(notificationBuilder, "stop", R.drawable.ic_media_stop);
                notificationBuilder.setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(new int[]{0})
                        .setMediaSession(mediaSession.getSessionToken()));
            } else {
                Log.d("mediastyle", "show play");
                addAction(notificationBuilder, "play", R.drawable.ic_media_play);
                notificationBuilder.setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(new int[]{0})
                        .setMediaSession(mediaSession.getSessionToken()));
            }
            mediaSession.setActive(true);
            startForeground(NOTIFICATION_ID, notificationBuilder.build());

        }


    }

    private void addAction(NotificationCompat.Builder builder, String label, int icon) {

        Intent recieverIntent = new Intent(this, ThikrMediaBroadcastReciever.class);
        recieverIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent RecieverPendingIntent = PendingIntent.getBroadcast(this, 1,
                new Intent(label).setClass(this.getApplicationContext(), ThikrMediaBroadcastReciever.class), PendingIntent.FLAG_CANCEL_CURRENT);

        Log.d("mediastyle", RecieverPendingIntent.getTargetPackage());

        recieverIntent.setAction(label);
        PendingIntent mediaPendingIntent = PendingIntent.getActivity(this,
                0, recieverIntent, PendingIntent.FLAG_CANCEL_CURRENT
        );
        builder.addAction(new NotificationCompat.Action(icon, label, RecieverPendingIntent));


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getExtras().getString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_DAY_THIKR).equalsIgnoreCase(MainActivity.DATA_TYPE_GENERAL_THIKR) && this.isPlaying()) {
            new MyAlarmsManager(this).UpdateAllApplicableAlarms();
            return Service.START_NOT_STICKY;
        } else {
            this.setThikrType(intent.getExtras().getString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_DAY_THIKR));
        }
        if (this.getThikrType().equalsIgnoreCase(MainActivity.DATA_TYPE_GENERAL_THIKR)) {
            new MyAlarmsManager(this).UpdateAllApplicableAlarms();
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

            boolean isRespectMute = sharedPrefs.getBoolean("mute_thikr_when_ringer_mute", true);
            if ((am.getRingerMode() == AudioManager.RINGER_MODE_SILENT || am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE)
                    && isRespectMute == true) {
                return Service.START_NOT_STICKY;
            }
        }
        initNotification();
        Bundle data = intent.getExtras();
        this.isUserAction=data.getBoolean("isUserAction",false);
        int action = data.getInt("ACTION", -1);
        Log.d("media1", "action " + action);
        switch (action) {
            case MEDIA_PLAYER_PAUSE:
                Log.d("media1", "pause called");

                //  mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                //          .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                //          .setActions(PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_PLAY)
                //           .build());

                this.pausePlayer();
                updateActions();
                break;
            case MEDIA_PLAYER_INNCREMENT:
                Log.d("media1", "increment called");
                int increment = intent.getExtras().getInt("INCREMENT", 1);
                this.setCurrentPlaying(this.getCurrentPlaying() + increment);

                this.playAll();
                updateActions();
                break;
            case MEDIA_PLAYER_CHANGE_VOLUME:
                Log.d("media1", "MEDIA_PLAYER_CHANGE_VOLUME called");
                this.setVolume();
                break;

            case MEDIA_PLAYER_RESET:
                Log.d("media1", "reset called");
                this.resetPlayer();

                this.stopForeground(true);
                this.stopSelf();
                break;
            case MEDIA_PLAYER_PLAYALL:
                Log.d("media1", "playall called");
                // mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                //         .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0)
                //       .setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_STOP)
                //     .build());

                this.playAll();
                updateActions();
                //   if ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE)).getCallState()!=TelephonyManager.CALL_STATE_OFFHOOK;
                break;
            case MEDIA_PLAYER_ISPLAYING:
                Log.d("media1", "isplaying called");
                this.isPlaying();
                break;
            case MEDIA_PLAYER_PLAY:

                int file = data.getInt("FILE");
                Log.d("media1", "play " + file + " called");
                //  mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                //        .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0)
                //        .setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_STOP)
                //        .build());
                this.play(file);
                updateActions();
                break;
            case MEDIA_PLAYER_RESUME:
                this.play();
                updateActions();
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
        currentPlaying = icurrentPlaying;
    }

    public int getAudioFocusRequestType() {
        if (this.getThikrType().equalsIgnoreCase(MainActivity.DATA_TYPE_GENERAL_THIKR)) {
            return AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
        }
        return AudioManager.AUDIOFOCUS_GAIN;
    }

    private int getStreamType() {
        if (this.getThikrType().equalsIgnoreCase(MainActivity.DATA_TYPE_GENERAL_THIKR)) {
            return AudioManager.STREAM_NOTIFICATION;
        } else {
            return AudioManager.STREAM_MUSIC;
        }
    }

    public void play() {
        player.setOnCompletionListener(this);


        int ret = am.requestAudioFocus(this,
                // Use the music stream.
                this.getStreamType(),
                // Request permanent focus.
                getAudioFocusRequestType());
        if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            startPlayerIfAllowed();
            setVolume();
        }


    }

    public void play(int fileNumber) {
        this.initMediaPlayer();
        setCurrentPlaying(fileNumber);
        player.setOnCompletionListener(this);
        AssetFileDescriptor afd;
        try {

            Log.d("media1 player", "file number is " + fileNumber);
            afd = this.getApplicationContext().getAssets().openFd(this.getThikrType() + "/" + (fileNumber) + ".mp3");
            Log.d("media1 player", "file path  is " + this.getThikrType() + "/" + (fileNumber) + ".mp3");
            player.reset();
            player.setAudioStreamType(getStreamType());
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();

            int ret = am.requestAudioFocus(this,
                    // Use the music stream.
                    this.getStreamType(),
                    // Request permanent focus.
                    getAudioFocusRequestType());
            if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

                startPlayerIfAllowed();
                setVolume();
            } else {
                am.abandonAudioFocus(this);
                this.stopForeground(true);
                this.stopSelf();
            }


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String[] getThikrArray() {
        String[] numbers_text = null;
        if (this.getThikrType().equals(MainActivity.DATA_TYPE_DAY_THIKR)) {
            numbers_text = getResources().getStringArray(R.array.MorningThikr);
        }
        if (this.getThikrType().equals(MainActivity.DATA_TYPE_NIGHT_THIKR)) {
            numbers_text = getResources().getStringArray(R.array.NightThikr);
        }
        if (this.getThikrType().equals(MainActivity.DATA_TYPE_GENERAL_THIKR)) {
            numbers_text = getResources().getStringArray(R.array.GeneralThikr);
        }

        return numbers_text;
    }

    public void playAll() {


        if (isPaused == false) {  //mediaplayer was stopped

            if (this.getCurrentPlaying() < 1) {
                setCurrentPlaying(1);
            }

            AssetFileDescriptor afd;
            try {
                Log.d("media1 player", "current playing is " + getCurrentPlaying());
                Log.d("media1 player", "thikrtype is " + getThikrType());
                afd = this.getApplicationContext().getAssets().openFd(getThikrType() + "/" + this.getCurrentPlaying() + ".mp3");
                //player.reset();
                Log.d("media1 player", "now will call initmediaplayer");
                this.initMediaPlayer();
                Log.d("media1 player", "finished initmediaplayer");
                player.setAudioStreamType(getStreamType());
                Log.d("media1 player", "audio stream type set");
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                Log.d("media1 player", "datasource set");
                player.prepare();
                Log.d("media1 player", "current playing was prepared successfully " + getCurrentPlaying());
            } catch (IOException e) {
                if (this.getCurrentPlaying() < 1) {
                    setCurrentPlaying(1);
                }
                if (this.getCurrentPlaying() > this.getThikrArray().length) {
                    setCurrentPlaying(this.getThikrArray().length);
                }
                //setCurrentPlaying(1);
                //e.printStackTrace();

            }

        }
        isPaused = false;


        player.setOnCompletionListener(this);
        int ret = am.requestAudioFocus(this,
                // Use the music stream.
                this.getStreamType(),
                // Request permanent focus.
                getAudioFocusRequestType());
        if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            startPlayerIfAllowed();
            setVolume();
        }

    }

    @Override
    public void onDestroy() {
        Log.d("media1 player", "ondestroy called");
        if (player != null) {
            player.release();
            player = null;
        }
        am.abandonAudioFocus(this);
        this.stopForeground(true);
        this.stopSelf();
        super.onDestroy();

    }

    public int getCurrentThikrRepeat() {
        int repeat = 1;
        String currentThikr = this.getThikrArray()[this.getCurrentPlaying() - 1];

        Pattern pattern = Pattern.compile("[\\d]+");
        Matcher matcher = pattern.matcher(currentThikr);
        if (matcher.find()) {


            repeat = Integer.parseInt(matcher.group(0));
        } else {
            repeat = 1;
        }
        return repeat;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.reset();
        Log.d("media1 player", "oncomplete called");

        Log.d("media1 player", "thikrtype is " + this.getThikrType() + " vs " + MainActivity.DATA_TYPE_GENERAL_THIKR);
        currentThikrCounter++;
        if (this.getThikrType().equalsIgnoreCase(MainActivity.DATA_TYPE_GENERAL_THIKR)) {
            ;
            this.resetPlayer();

            this.stopForeground(true);
            this.stopSelf();
        }
        if (this.getCurrentPlaying() >= getThikrArray().length && currentThikrCounter >= getCurrentThikrRepeat()) {

            setCurrentPlaying(1);
            currentThikrCounter = 0;

            this.resetPlayer();

            this.stopForeground(true);
            this.stopSelf();
            return;
        } else {
            if (currentThikrCounter >= getCurrentThikrRepeat()) {
                currentThikrCounter = 0;
                setCurrentPlaying(this.getCurrentPlaying() + 1);
            } else {
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

    private String getThikrType() {
        return ThikrType;

    }

    private void setThikrType(String iThikrType) {
        if (iThikrType != null) {
            ThikrType = iThikrType;
        }

    }

    private String getThikrTypeString(String thikTypeConstant) {
        if (thikTypeConstant.equals(MainActivity.DATA_TYPE_DAY_THIKR)) {
            return this.getString(R.string.morningThikr);
        } else if (thikTypeConstant.equals(MainActivity.DATA_TYPE_NIGHT_THIKR)) {
            return this.getString(R.string.nightThikr);
        } else {
            return "تذكير بالله";
        }

    }

    public void resetPlayer() {
        if (this.player != null) {
            this.player.stop();
            this.player.reset();
            //am.abandonAudioFocus(this);
            //player.release();
            //this.stopSelf();
        }

    }

    public boolean isPlaying() {
        boolean isPlaying = false;
        if (player != null) {
            try {
                isPlaying = this.player.isPlaying();
            } catch (Exception e) {

            }

        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        sharedPrefs.edit().putBoolean("ISPLAYING", isPlaying);

        return isPlaying;

    }

    public void pausePlayer() {
        isPaused = true;
        if (this.isPlaying()) {
            this.player.pause();
        }
        //am.abandonAudioFocus(this);


    }


    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                Log.d("media1 player", "gained focus");
                mediaSession.setActive(true);
                if (player == null) {
                    initMediaPlayer();
                } else if (!isPlaying()) {

                    startPlayerIfAllowed();
                }
                this.setVolume();


                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                Log.d("media1 player", "lost focus");
                mediaSession.setActive(false);
                if (isPlaying()) {
                    player.stop();
                }
                Log.d("media1", "reseting player and releasing service");
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

    private void setVolume() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        int volumeLevel = sharedPrefs.getInt("volume", 100);
        int maxVolume = 101;
        float volume = (float) (1 - Math.log(maxVolume - volumeLevel) / Math.log(maxVolume));
        player.setVolume(volume, volume);


    }
    private void startPlayerIfAllowed(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        boolean isRespectMute = sharedPrefs.getBoolean("mute_thikr_when_ringer_mute", true);
        if ((am.getRingerMode() == AudioManager.RINGER_MODE_SILENT || am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE)
                && isRespectMute == true && this.overRideRespectMute==false && isUserAction==false) {
            if (!this.getThikrType().equalsIgnoreCase(MainActivity.DATA_TYPE_GENERAL_THIKR)) {
                this.pausePlayer();
                Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
            }

        }else{
            player.start();
        }
    }

    private boolean initMediaPlayer() {
        if (this.isPlaying()) {
            Log.d("media1 player", "initiMediaPlayer is called and player is not null");
            this.resetPlayer();
        }
        //this.setCurrentPlaying(1);
        if (player == null) {
            Log.d("media1 player", "initiMediaPlayer is called and player is null");
            player = new MediaPlayer();
            player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
            am = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

//            MediaSessionManager mManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);

            ComponentName receiver = new ComponentName("com.HMSolutions.thikrallah.Notification", ThikrMediaBroadcastReciever.class.getName());

            mediaSession = new MediaSessionCompat(this
                    , "MEDIA_SESSION_THIKRALLAH"
                    , receiver
                    , null);
            mController = new MediaControllerCompat(this, mediaSession);
            mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
            // updateActions();
            // mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
            ////       .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
            //   .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
            //     .build());

            mediaSession.setCallback(new MediaSessionCompat.Callback() {
                @Override
                public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                    Log.d("mediastyle", "onMediaButtonEvent");
                    return super.onMediaButtonEvent(mediaButtonEvent);
                }

                @Override
                public void onPlay() {
                    Log.d("mediastyle", "onPlay");
                    super.onPlay();
                }

                @Override
                public void onPause() {
                    Log.d("mediastyle", "onPause");
                    super.onPause();
                }

                @Override
                public void onSkipToNext() {
                    Log.d("mediastyle", "onSkipToNext");
                    super.onSkipToNext();
                }

                @Override
                public void onSkipToPrevious() {
                    Log.d("mediastyle", "onSkipToPrevious");
                    super.onSkipToPrevious();
                }

                @Override
                public void onSeekTo(long pos) {
                    Log.d("mediastyle", String.format("onSeekTo %d", pos));
                    super.onSeekTo(pos);
                }

                @Override
                public void onStop() {
                    Log.d("mediastyle", "onStop");
                    super.onStop();
                }
            });
            mediaSession.setActive(true);

            //mController = MediaController.fromToken( mediaSession.getSessionToken() );

        }
        return true;

    }


}