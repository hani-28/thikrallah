package com.HMSolutions.thikrallah.Notification;

import com.HMSolutions.thikrallah.R;

import android.Manifest;
import android.app.Service;
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
            //thikr=this.getApplicationContext().getResources().getStringArray(R.array.GeneralThikr)[thikrNumber-1];
		    // We want this service to continue running until it is explicitly
		    // stopped, so return sticky.
			windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
			if (chatHead!=null){
				windowManager.removeView(chatHead);
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

			params = new WindowManager.LayoutParams(
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.TYPE_PHONE,
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
					PixelFormat.TRANSLUCENT);

			params.gravity = Gravity.CENTER | Gravity.CENTER;
			params.x = 0;
			params.y = 100;


			chatHead.setOnTouchListener(this);
			int permissionCheck = ContextCompat.checkSelfPermission(this,
					Manifest.permission.ACCESS_FINE_LOCATION);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (Settings.canDrawOverlays(this)) {
                    windowManager.addView(chatHead, params);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopSelf();
                        }
                    }, 10000);    //will stop service after 10 seconds
                }else{
                    //permission not defined
                }
			}else{
				windowManager.addView(chatHead, params);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						stopSelf();
					}
				}, 10000);    //will stop service after 10 seconds
			}
			return START_NOT_STICKY;

	    }else{
	    	this.stopSelf();
	    	return START_NOT_STICKY;
	    }
		
	}
	
	@Override 
	public boolean onTouch(View v, MotionEvent event) {
		stopSelf();
		return true;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (chatHead != null) ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(chatHead);
	}
}
