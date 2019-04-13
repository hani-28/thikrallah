package com.HMSolutions.thikrallah.Notification;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.R;

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
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Locale;

public class ChatHeadService extends Service implements View.OnTouchListener {

	private WindowManager windowManager;
	//private ImageView chatHead;
	private TextView chatHead;
    String TAG = "ChatHeadService";
    private final static int NOTIFICATION_ID=0;
	WindowManager.LayoutParams params;
	private String thikr;
	@Override 
	public IBinder onBind(Intent intent) {
		// Not used
		return null;
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang=mPrefs.getString("language",null);
        Log.d(TAG,"chatheadservice started");
        if (lang!=null){
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }
        if (intent==null){
			return START_NOT_STICKY;
		}
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		int reminderType=Integer.parseInt(sharedPrefs.getString("RemindmeThroughTheDayType", "1"));
	    if (reminderType==1 ||reminderType==3){
	    	String thikr=intent.getStringExtra("thikr");
			boolean isAthan=intent.getBooleanExtra("isAthan",false);
            //thikr=this.getApplicationContext().getResources().getStringArray(R.array.GeneralThikr)[thikrNumber-1];
		    // We want this service to continue running until it is explicitly
		    // stopped, so return sticky.
			windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
			if (chatHead!=null ){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if(chatHead.isAttachedToWindow()){
                        windowManager.removeView(chatHead);
                    }
                }else{
                    windowManager.removeView(chatHead);
                }
			}
			//chatHead = new ImageView(this);
			
			chatHead=new TextView(this);
			chatHead.setTextAppearance(this.getApplicationContext(), android.R.style.TextAppearance_Large);
			chatHead.setText(thikr, TextView.BufferType.SPANNABLE);
			chatHead.setBackgroundResource(R.drawable.chat_head);
			chatHead.setTextColor(Color.BLACK);
			chatHead.setGravity(Gravity.CENTER);
			chatHead.setGravity(Gravity.CENTER);
			
			//chatHead.setImageResource(R.drawable.chat_head);
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
				params = new WindowManager.LayoutParams(
						WindowManager.LayoutParams.WRAP_CONTENT,
						WindowManager.LayoutParams.WRAP_CONTENT,
						WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
						WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
						PixelFormat.TRANSLUCENT);
			} else {
				params = new WindowManager.LayoutParams(
						WindowManager.LayoutParams.WRAP_CONTENT,
						WindowManager.LayoutParams.WRAP_CONTENT,
						WindowManager.LayoutParams.TYPE_PHONE,
						WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
						PixelFormat.TRANSLUCENT);
			}


			params.gravity = Gravity.CENTER | Gravity.CENTER;
			params.x = 0;
			params.y = 100;


			chatHead.setOnTouchListener(this);



            if (isAthan) {
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationCompat.Builder mBuilder;

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					String NOTIFICATION_CHANNEL_ID = "com.HMSolutions.thikrallah.Notification.AthanTimerService";
					String channelName = this.getResources().getString(R.string.athan_timer_notifiaction);
					NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
					chan.setSound(null,null);
					chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
					NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					assert manager != null;
					manager.createNotificationChannel(chan);
					mBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
				}else{
					mBuilder = new NotificationCompat.Builder(this);
				}




                mBuilder.setContentTitle(this.getString(R.string.app_name))
                        .setContentText(thikr)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setAutoCancel(true);

                mBuilder = setVisibilityPublic(mBuilder);

                Intent launchAppIntent = new Intent(this, MainActivity.class);

                launchAppIntent.putExtra("FromNotification", true);
                launchAppIntent.putExtra("DataType", MainActivity.DATA_TYPE_ATHAN);
                PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
                        0, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT
                );

                mBuilder.setContentIntent(launchAppPendingIntent);
				//TODO:ADD CHANNEL TO THE NOTIFICATION
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

            }


			int permissionCheck = ContextCompat.checkSelfPermission(this,
					Manifest.permission.ACCESS_FINE_LOCATION);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (Settings.canDrawOverlays(this)) {
                    windowManager.addView(chatHead, params);
                    if (!isAthan){
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								stopSelf();
							}
						}, 10000);    //will stop service after 10 seconds
					}

                }else{
                    //permission not defined
                }
			}else{
				windowManager.addView(chatHead, params);
				if (!isAthan){
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							stopSelf();
						}
					}, 10000);    //will stop service after 10 seconds
				}
			}
			return START_NOT_STICKY;

	    }else{
	    	this.stopSelf();
	    	return START_NOT_STICKY;
	    }
		
	}
    private NotificationCompat.Builder setVisibilityPublic(NotificationCompat.Builder inotificationBuilder){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            inotificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }
        return inotificationBuilder;
    }
	@Override 
	public boolean onTouch(View v, MotionEvent event) {
		stopSelf();
		return true;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
		if (chatHead != null) ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(chatHead);
	}
}
