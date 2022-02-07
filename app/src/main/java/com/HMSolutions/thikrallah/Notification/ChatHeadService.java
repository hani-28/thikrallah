package com.HMSolutions.thikrallah.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.ThikrMediaPlayerService;

import java.lang.ref.WeakReference;
import java.util.Locale;

import timber.log.Timber;

public class ChatHeadService extends Service implements View.OnTouchListener {

	private WindowManager windowManager;
	//private ImageView chatHead;
	private TextView chatHead;
	String TAG = "ChatHeadService";
	private final static int NOTIFICATION_ID = 235;
	WindowManager.LayoutParams params;
	private String thikr;
	private boolean isAthan;

	@Override
	public IBinder onBind(Intent intent) {
		// Not used
		return null;
	}

	private void startnotification() {
		String NOTIFICATION_CHANNEL_ID = "com.HMSolutions.thikrallah.Notification.ChatHeadService";
		String channelName = this.getResources().getString(R.string.floating_notification);
		NotificationCompat.Builder mBuilder;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


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
		if (thikr==null){
			this.thikr=getResources().getString(R.string.remember_notification);
		}
		mBuilder.setContentTitle(this.getString(R.string.my_app_name))
				.setContentText(thikr)
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(true);
		mBuilder = setVisibilityPublic(mBuilder);
		Intent launchAppIntent = new Intent(this, MainActivity.class);
		PendingIntent launchAppPendingIntent = PendingIntent.getActivity(this,
				0, launchAppIntent, PendingIntent.FLAG_CANCEL_CURRENT
		);

		mBuilder.setContentIntent(launchAppPendingIntent);
		startForeground(NOTIFICATION_ID, mBuilder.build());
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		if (chatHead!=null ){
			//remove any overlay windows from previous run
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				if(chatHead.isAttachedToWindow()){
					windowManager.removeView(chatHead);
				}
			}else{
				windowManager.removeView(chatHead);
			}
		}
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
			Log.d(TAG,"starting foreground (null intent?)");
			startnotification();
			this.stopSelf();
			return START_NOT_STICKY;
		}
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		int reminderType=Integer.parseInt(sharedPrefs.getString("RemindmeThroughTheDayType", "1"));
	    if (reminderType==1 ||reminderType==3) {

			String thikr = intent.getStringExtra("thikr");
			isAthan = intent.getBooleanExtra("isAthan", false);

			if (isAthan) {
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				NotificationCompat.Builder mBuilder;

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					String NOTIFICATION_CHANNEL_ID = "com.HMSolutions.thikrallah.Notification.AthanTimerService";
					String channelName = this.getResources().getString(R.string.athan_timer_notifiaction);
					NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
					chan.setSound(null, null);
					chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
					NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					assert manager != null;
					manager.createNotificationChannel(chan);
					mBuilder = new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
				}else{
					mBuilder = new NotificationCompat.Builder(this);
				}
				mBuilder.setContentTitle(this.getString(R.string.my_app_name))
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
				Log.d(TAG,"starting foreground ( athan)");
				startForeground(NOTIFICATION_ID, mBuilder.build());
				//mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

			}else{
				Log.d(TAG,"starting foreground (not athan)");
				startnotification();
				//mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
			}
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


			params.gravity = Gravity.CENTER;
			params.x = 0;
			params.y = 100;


			chatHead.setOnTouchListener(this);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (Settings.canDrawOverlays(this)) {
                    windowManager.addView(chatHead, params);

					if (!isAthan){
						new Handler().postDelayed(new DestroyRunnable(this) , 10000);    //will stop service after 10 seconds
					}

                }else{
                    //permission not defined
                }
			}else{
				windowManager.addView(chatHead, params);
				if (!isAthan){
					new Handler().postDelayed(new DestroyRunnable(this) , 10000);    //will stop service after 10 seconds
				}
			}
			return START_NOT_STICKY;

	    }else{
			Log.d(TAG,"not reminder type 1 or 3? what then?");
			startnotification();
	    	this.stopSelf();
	    	return START_NOT_STICKY;
	    }
		
	}

	static class DestroyRunnable implements Runnable {
		public WeakReference<ChatHeadService> getmService() {
			return mService;
		}

		private final WeakReference<ChatHeadService> mService;

		DestroyRunnable(ChatHeadService service) {
			mService = new WeakReference<ChatHeadService>(service);
		}


		@Override
		public void run() {
			if (mService.get()!=null){
				mService.get().stopSelf();
			}
		}
	}
    private NotificationCompat.Builder setVisibilityPublic(NotificationCompat.Builder inotificationBuilder){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			inotificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }
        return inotificationBuilder;
    }
	@Override 
	public boolean onTouch(View v, MotionEvent event) {
		if (isAthan) {
			Bundle data = new Bundle();
			data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_RESET);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				this.startForegroundService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
			} else {
				this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
			}
		}
		stopSelf();
		return true;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG,"ondestroy called");
		if (chatHead != null){
			windowManager.removeView(chatHead);
		}
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(NOTIFICATION_ID);
	}
}
