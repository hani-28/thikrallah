package com.HMSolutions.thikrallah.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;

import com.HMSolutions.thikrallah.ThikrMediaPlayerService;


public class ThikrMediaBroadcastReciever extends BroadcastReceiver {
        private Context context;
    String TAG = "ThikrMediaBroadcastRcvr";
    private Bundle data;

    // Constructor is mandatory
        public ThikrMediaBroadcastReciever ()
        {
            super ();
        }
        @Override
        public void onReceive(Context icontext, Intent intent) {
            context=icontext;
            String intentAction = intent.getAction();
            data=new Bundle();
            Log.d(TAG,"onReceive called");
            data.putAll(intent.getExtras());
            data.putBoolean("isUserAction",true);
            Log.i("mediastyle", intentAction.toString() + " happended");
            if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {

                final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            resumePlayer();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            pausePlayer();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_STOP:
                            stopPlayer();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            if (isPlaying()==true){
                                pausePlayer();
                            }else{
                                resumePlayer();
                            }
                            break;
                    }
                }
            }
            if (intentAction.equalsIgnoreCase("play")){
                resumePlayer();
            }else if(intentAction.equalsIgnoreCase("pause")){
                pausePlayer();
            }else if (intentAction.equalsIgnoreCase("stop")){
                stopPlayer();
            }

        }
    public void pausePlayer() {

        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PAUSE);
        sendActionToMediaService(data);

    }

    public void resumePlayer () {

        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_RESUME);

        sendActionToMediaService(data);

    }

    public boolean isPlaying() {

        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_ISPLAYING);
        sendActionToMediaService(data);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean("ISPLAYING", false);

    }

    public void stopPlayer() {

        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_RESET);
        sendActionToMediaService(data);


    }
    public void sendActionToMediaService(Bundle data){
        if (data.getInt("ACTION",-100)!=-100){
            Log.d(TAG,"sendActionToMediaService called with action"+data.getInt("action",-100));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, ThikrMediaPlayerService.class).putExtras(data));
            } else {
                context.startService(new Intent(context, ThikrMediaPlayerService.class).putExtras(data));
            }
        }

    }
    }
