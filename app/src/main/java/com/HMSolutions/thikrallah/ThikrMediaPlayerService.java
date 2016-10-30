package com.HMSolutions.thikrallah;


import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
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
import android.content.res.Configuration;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.os.Vibrator;
import android.widget.Toast;

public class ThikrMediaPlayerService extends Service implements OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener {
    String TAG = "ThikrMediaPlayerService";
    public static final int MEDIA_PLAYER_PAUSE = 1;
    public static final int MEDIA_PLAYER_RESET = 2;
    public static final int MEDIA_PLAYER_PLAY = 3;
    public static final int MEDIA_PLAYER_PLAYALL = 4;
    public static final int MEDIA_PLAYER_ISPLAYING = 5;
    public static final int MEDIA_PLAYER_INNCREMENT = 6;
    public static final int MEDIA_PLAYER_CHANGE_VOLUME = 7;
    public static final int MEDIA_PLAYER_RESUME = 8;
    AudioManager am;
    private boolean StayPaused=false;
    int play_count=0;
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
    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    /** Command to the service to display a message */
    static final int MSG_CURRENT_PLAYING = 100;
    static final int MSG_UNBIND = 99;
    private String filepath;
    private PowerManager.WakeLock wakeLock;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"service recieved message what="+msg.what);
            Message msg2;
            switch (msg.what) {
                case MSG_CURRENT_PLAYING:
                    mClients.clear();
                    mClients.add(msg.replyTo);
                    sendMessageToUI(MSG_CURRENT_PLAYING, getCurrentPlaying());
                   // Toast.makeText(getApplicationContext(), "hello! 1", Toast.LENGTH_SHORT).show();
                    msg2= Message.obtain(null, ThikrMediaPlayerService.MSG_CURRENT_PLAYING, 0, 0);
                  //  try {
                       // msg.replyTo.send(msg2);
                  //  } catch (RemoteException e) {
                   //     e.printStackTrace();
                  //  }
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "hello! 2", Toast.LENGTH_SHORT).show();
                    msg2= Message.obtain(null, ThikrMediaPlayerService.MSG_CURRENT_PLAYING, 0, 0);
                    try {
                        msg.replyTo.send(msg2);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    super.handleMessage(msg);
            }
        }
    }


    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());


    private void sendMessageToUI(int what,int intvaluetosend) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data as an Integer
                Log.d(TAG,"sendmessagetoUI called with what="+what+ " value="+intvaluetosend);
                Message msg = Message.obtain(null, what, intvaluetosend, 0);
                Bundle data=new Bundle();
                data.putString("com.HMSolutions.thikrallah.datatype",this.getThikrType());
                msg.setData(data);
                mClients.get(i).send(msg);
            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                //mClients.remove(i);
            }
        }
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {

        return mMessenger.getBinder();
    }
    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                TAG);
        wakeLock.acquire();



        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang=mPrefs.getString("language",null);

        if (lang!=null){
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
        Log.d(TAG,"oncreate called");
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
                .setPriority(Notification.PRIORITY_MAX)
                .setContentText(getThikrTypeString(this.getThikrType()))

                .setContentIntent(launchAppPendingIntent);
        notificationBuilder=setVisibilityPublic(notificationBuilder);
        updateActions();


    }
    private NotificationCompat.Builder setVisibilityPublic(NotificationCompat.Builder inotificationBuilder){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            inotificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        return inotificationBuilder;
    }

    private void updateActions() {
        boolean isHuawei = (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1 ||
                android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP)
                && (Build.MANUFACTURER.toLowerCase(Locale.getDefault()).contains("huawei")&&
                (Build.MODEL.toLowerCase().contains("u6582")
                ||Build.MODEL.toLowerCase().contains("l6753")));


        if (notificationBuilder != null) {
            if (!isHuawei){//known issue with huawei devices.
                notificationBuilder.mActions.clear();
                if (this.isPlaying()) {
                    Log.d(TAG, "show pause & stop");
                    addAction(notificationBuilder, "pause", R.drawable.ic_media_pause);
                    addAction(notificationBuilder, "stop", R.drawable.ic_media_stop);
                    notificationBuilder.setStyle(new NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(new int[]{0,1})
                            .setMediaSession(mediaSession.getSessionToken()));
                } else {
                    Log.d(TAG, "show play");
                    addAction(notificationBuilder, "play", R.drawable.ic_media_play);
                    addAction(notificationBuilder, "stop", R.drawable.ic_media_stop);
                    notificationBuilder.setStyle(new NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(new int[]{0})
                            .setMediaSession(mediaSession.getSessionToken()));
                }
                mediaSession.setActive(true);
            }
            startForeground(NOTIFICATION_ID, notificationBuilder.build());

        }


    }

    private void addAction(NotificationCompat.Builder builder, String label, int icon) {

        Intent recieverIntent = new Intent(this, ThikrMediaBroadcastReciever.class);
        recieverIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent RecieverPendingIntent = PendingIntent.getBroadcast(this, 1,
                new Intent(label).setClass(this.getApplicationContext(), ThikrMediaBroadcastReciever.class), PendingIntent.FLAG_CANCEL_CURRENT);

        Log.d(TAG, RecieverPendingIntent.getTargetPackage());

        recieverIntent.setAction(label);
        PendingIntent mediaPendingIntent = PendingIntent.getActivity(this,
                0, recieverIntent, PendingIntent.FLAG_CANCEL_CURRENT
        );
        builder.addAction(new NotificationCompat.Action(icon, label, RecieverPendingIntent));


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle data = intent.getExtras();
        this.isUserAction=data.getBoolean("isUserAction",false);
        int action = data.getInt("ACTION", -1);
        String type = data.getString("thikrType", null);
        Log.d(TAG, "action " + action);

        if (intent.getExtras().getString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_DAY_THIKR).equalsIgnoreCase(MainActivity.DATA_TYPE_GENERAL_THIKR) && this.isPlaying()) {
            new MyAlarmsManager(this).UpdateAllApplicableAlarms();
            if (action==MEDIA_PLAYER_RESET){
                Log.d(TAG, "reset called");
                this.resetPlayer();
                this.stopForeground(true);
                this.stopSelf();
            }
            return Service.START_NOT_STICKY;
        } else {

            this.setThikrType(intent.getExtras().getString("com.HMSolutions.thikrallah.datatype", null));
        }
        if(getThikrType()==null){
            //TODO:when does this case happen
            Log.d(TAG,"thikrtype is null... why?");
            new MyAlarmsManager(this).UpdateAllApplicableAlarms();
            return Service.START_NOT_STICKY;
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
        if (this.getThikrType().contains(MainActivity.DATA_TYPE_ATHAN)){
            new MyAlarmsManager(this).UpdateAllApplicableAlarms();
        }
        Log.d(TAG,"onStartCommand called"+intent.getExtras().toString());
        initNotification();

        switch (action) {
            case MEDIA_PLAYER_PAUSE:
                Log.d(TAG, "pause called");

                //  mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                //          .setState(PlaybackStateCompat.STATE_PAUSED, 0, 0)
                //          .setActions(PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_PLAY)
                //           .build());

                this.pausePlayer();

                updateActions();
                break;
            case MEDIA_PLAYER_INNCREMENT:
                Log.d(TAG, "increment called");

                int increment = intent.getExtras().getInt("INCREMENT", 1);
                this.setCurrentPlaying(this.getCurrentPlaying() + increment);
                currentThikrCounter=0;
                this.playAll();
                updateActions();
                break;
            case MEDIA_PLAYER_CHANGE_VOLUME:
                Log.d(TAG, "MEDIA_PLAYER_CHANGE_VOLUME called");
                this.setVolume();
                break;

            case MEDIA_PLAYER_RESET:
                Log.d(TAG, "reset called");
                this.resetPlayer();
                this.stopForeground(true);
                this.stopSelf();
                break;
            case MEDIA_PLAYER_PLAYALL:
                Log.d(TAG, "playall called");
                // mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                //         .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0)
                //       .setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_STOP)
                //     .build());
                currentThikrCounter=0;
                this.playAll();
                updateActions();
                //   if ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE)).getCallState()!=TelephonyManager.CALL_STATE_OFFHOOK;
                break;
            case MEDIA_PLAYER_ISPLAYING:
                Log.d(TAG, "isplaying called");
                this.isPlaying();
                break;
            case MEDIA_PLAYER_PLAY:

                int file=-1;
                filepath="null";
                file = data.getInt("FILE");
                filepath=data.getString("FILE_PATH");

                Log.d(TAG, "play " + file + " called");
                //  mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                //        .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0)
                //        .setActions(PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_STOP)
                //        .build());
                currentThikrCounter=0;
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
        sendMessageToUI(MSG_CURRENT_PLAYING,currentPlaying);
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
        } else if (this.getThikrType().contains(MainActivity.DATA_TYPE_ATHAN)) {
            return AudioManager.STREAM_NOTIFICATION;
        }else {
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

        try {

            if (fileNumber!=-1){
                Log.d(TAG, "file number is " + fileNumber);
                AssetFileDescriptor afd = this.getApplicationContext().getAssets().openFd(this.getMediaFolderName() + "/" + (fileNumber) + ".mp3");
                Log.d(TAG, "file path  is " + this.getMediaFolderName() + "/" + (fileNumber) + ".mp3");
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
                    //am.abandonAudioFocus(this);
                    //this.stopForeground(true);
                    //this.stopSelf();
                }

            }else{
                FileDescriptor afd=null;
                Log.d(TAG,"filepath="+filepath);
                FileInputStream fis = new FileInputStream(this.filepath);
                afd = fis.getFD();
                player.reset();
                player.setAudioStreamType(getStreamType());
                player.setDataSource(afd);
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
                    //am.abandonAudioFocus(this);
                    //this.stopForeground(true);
                    //this.stopSelf();
                }

            }




        } catch (IOException e) {

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
        if (this.getThikrType().contains(MainActivity.DATA_TYPE_QURAN)) {
            int surat=Integer.parseInt(this.getThikrType().split("/")[1]);


            int count=this.getResources().getIntArray(R.array.verses_count)[surat];
            numbers_text = new String[count];
            for (int i=0;i<count;i++){
                numbers_text[i]=String.valueOf(i+1);
            }
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
                Log.d(TAG, "current playing is " + getCurrentPlaying());
                Log.d(TAG, "thikrtype is " + getThikrType());
                afd = this.getApplicationContext().getAssets().openFd(getThikrType() + "/" + this.getCurrentPlaying() + ".mp3");
                //player.reset();
                Log.d(TAG, "now will call initmediaplayer");
                this.initMediaPlayer();
                Log.d(TAG, "finished initmediaplayer");
                player.setAudioStreamType(getStreamType());
                Log.d(TAG, "audio stream type set");
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                Log.d(TAG, "datasource set");
                player.prepare();
                Log.d(TAG, "current playing was prepared successfully " + getCurrentPlaying());
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
        Log.d(TAG,"audiofocus request return code is "+ret);
        if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG,"audiofocus request granted ="+AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
            startPlayerIfAllowed();
            setVolume();
        }

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "ondestroy called");
        if (player != null) {
            player.release();
            player = null;
        }
        am.abandonAudioFocus(this);
        wakeLock.release();
        this.sendMessageToUI(MSG_CURRENT_PLAYING,-99);
        this.sendMessageToUI(MSG_UNBIND,MSG_UNBIND);
        this.stopForeground(true);
        this.stopSelf();
        super.onDestroy();

    }

    public int getCurrentThikrRepeat() {
        int repeat = 1;
        if (this.getThikrType().contains(MainActivity.DATA_TYPE_QURAN)){
            return repeat;
        }
        String currentThikr="";
        try{
            currentThikr = this.getThikrArray()[this.getCurrentPlaying() - 1];
        }catch (IndexOutOfBoundsException e){
            Log.d(TAG,"'index out of bound");
        }
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
        Log.d(TAG, "oncomplete called");

        Log.d(TAG, "thikrtype is " + this.getThikrType() + " vs " + MainActivity.DATA_TYPE_GENERAL_THIKR);
        currentThikrCounter++;
        if (this.getThikrType().equalsIgnoreCase(MainActivity.DATA_TYPE_GENERAL_THIKR)||this.getThikrType().contains(MainActivity.DATA_TYPE_QURAN)||this.getThikrType().contains(MainActivity.DATA_TYPE_ATHAN)) {
            ;
            this.resetPlayer();

            this.stopForeground(true);
            this.stopSelf();
            return;
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
    private String getMediaFolderName(){
        if(getThikrType().contains(MainActivity.DATA_TYPE_ATHAN)){
            return ThikrType=MainActivity.DATA_TYPE_ATHAN;
        }
        return getThikrType();
    }

    private void setThikrType(String iThikrType) {
        if (iThikrType != null) {
            ThikrType = iThikrType;
        }

    }

    private String getThikrTypeString(String thikTypeConstant) {
        switch (thikTypeConstant){
            case MainActivity.DATA_TYPE_ATHAN1:
                return this.getString(R.string.prayer1);

            case MainActivity.DATA_TYPE_ATHAN2:
                return this.getString(R.string.prayer2);

            case MainActivity.DATA_TYPE_ATHAN3:
                return this.getString(R.string.prayer3);

            case MainActivity.DATA_TYPE_ATHAN4:
                return this.getString(R.string.prayer4);
            case MainActivity.DATA_TYPE_ATHAN5:
                return this.getString(R.string.prayer5);
            case MainActivity.DATA_TYPE_DAY_THIKR:
                return this.getString(R.string.morningThikr);
            case MainActivity.DATA_TYPE_NIGHT_THIKR:
                return this.getString(R.string.nightThikr);
            case MainActivity.DATA_TYPE_QURAN_KAHF:
                return this.getString(R.string.surat_alkahf);
            case MainActivity.DATA_TYPE_QURAN_MULK:
                return this.getString(R.string.surat_almulk);
            default:
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
        StayPaused=true;
        if (this.isPlaying()) {
            this.player.pause();
            am.abandonAudioFocus(this);
        }else{
            if(this.play_count==0){
                this.stopSelf();
            }
        }
        //am.abandonAudioFocus(this);


    }


    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                Log.d(TAG, "gained focus");
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
                Log.d(TAG, "lost focus");
                mediaSession.setActive(false);
                if (isPlaying()) {
                    player.stop();
                }
                Log.d(TAG, "reseting player and releasing service");
                this.resetPlayer();
                this.stopForeground(true);
                this.stopSelf();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                Log.d(TAG, "transient loss of  focus");
                if (isPlaying()) player.pause();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                Log.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                if (isPlaying()) {

                    player.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    private void setVolume() {
        if (this.getThikrType().contains(MainActivity.DATA_TYPE_GENERAL_THIKR)){
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            int volumeLevel = sharedPrefs.getInt("volume", 100);
            int maxVolume = 101;
            float volume = (float) (1 - Math.log(maxVolume - volumeLevel) / Math.log(maxVolume));
            player.setVolume(volume, volume);
        }else{
            player.setVolume(1.0f, 1.0f);
        }
    }
    private void startPlayerIfAllowed(){
        Log.d(TAG,"startPlayerIfAllowed called");

        int ret = am.requestAudioFocus(this,
                // Use the music stream.
                this.getStreamType(),
                // Request permanent focus.
                getAudioFocusRequestType());
        if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {


            this.play_count++;
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            boolean isRespectMute = sharedPrefs.getBoolean("mute_thikr_when_ringer_mute", true);
            if ((am.getRingerMode() == AudioManager.RINGER_MODE_SILENT || am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE)
                    && isRespectMute == true && this.overRideRespectMute==false && isUserAction==false) {
                if (!this.getThikrType().equalsIgnoreCase(MainActivity.DATA_TYPE_GENERAL_THIKR)) {
                    this.pausePlayer();
                    vibrate();
                    this.stopSelf();
                }

            }else{
                sendMessageToUI(MSG_CURRENT_PLAYING,currentPlaying);
                StayPaused=false;
                player.start();
                Log.d(TAG,"player started");
            }
        }
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

    private boolean initMediaPlayer() {
        if (player!=null){
            //if (this.isPlaying()) {
                Log.d(TAG, "initiMediaPlayer is called and player is not null");
                this.resetPlayer();
            //}
        }

        //this.setCurrentPlaying(1);
        if (player == null) {
            Log.d(TAG, "initiMediaPlayer is called and player is null");
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
                    Log.d(TAG, "onMediaButtonEvent");
                    return super.onMediaButtonEvent(mediaButtonEvent);
                }

                @Override
                public void onPlay() {
                    Log.d(TAG, "onPlay");
                    super.onPlay();
                }

                @Override
                public void onPause() {
                    Log.d(TAG, "onPause");
                    super.onPause();
                }

                @Override
                public void onSkipToNext() {
                    Log.d(TAG, "onSkipToNext");
                    super.onSkipToNext();
                }

                @Override
                public void onSkipToPrevious() {
                    Log.d(TAG, "onSkipToPrevious");
                    super.onSkipToPrevious();
                }

                @Override
                public void onSeekTo(long pos) {
                    Log.d(TAG, String.format("onSeekTo %d", pos));
                    super.onSeekTo(pos);
                }

                @Override
                public void onStop() {
                    Log.d(TAG, "onStop");
                    super.onStop();
                }
            });
            try{

                mediaSession.setActive(true);
            }catch (Exception e){
                mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
                mediaSession.setActive(true);
            }

            //mController = MediaController.fromToken( mediaSession.getSessionToken() );

        }
        return true;

    }


}