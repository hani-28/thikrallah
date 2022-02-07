package com.HMSolutions.thikrallah.Utilities;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.HMSolutions.thikrallah.R;

import java.lang.ref.WeakReference;

public class AppRater {
	
	private final static int DAYS_UNTIL_PROMPT = 2;
	private final static int LAUNCHES_UNTIL_PROMPT = 5;
	private static Context context;
	private static  String APP_PNAME = "";

	public static void app_launched(WeakReference<Context> mContext) {
		context=mContext.get();
		SharedPreferences prefs;
		if (mContext.get()!=null){
			Log.d("AppRater","apprater app_launched called");
			APP_PNAME=mContext.get().getResources().getString(R.string.app_package);
			prefs = mContext.get().getSharedPreferences("apprater", 0);
		}else{
			return;
		}
		if (prefs.getBoolean("dontshowagain", false)) { return ; }

		SharedPreferences.Editor editor = prefs.edit();

		// Increment launch counter
		long launch_count = prefs.getLong("launch_count", 0) + 1;
		editor.putLong("launch_count", launch_count);

		// Get date of first launch
		Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong("date_firstlaunch", date_firstLaunch);
		}

		// Wait at least n days before opening
		if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
			if (System.currentTimeMillis() >= date_firstLaunch + 
					(DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
				if (mContext.get()!=null){
					showRateDialog(mContext.get(), editor);
				}

			}
		}

		editor.commit();
	}   

	public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
		final Dialog dialog = new Dialog(mContext);
		dialog.setTitle(context.getString(R.string.rate));

		LinearLayout ll = new LinearLayout(mContext);
		ll.setOrientation(LinearLayout.VERTICAL);

		TextView tv = new TextView(mContext);
		tv.setText(context.getString(R.string.rate_message));
		tv.setWidth(240);
		tv.setPadding(4, 0, 4, 10);
		ll.addView(tv);

		Button b1 = new Button(mContext);
		b1.setText(context.getString(R.string.rate));
		b1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (editor != null) {
					editor.putBoolean("dontshowagain", true);
					editor.commit();
				} mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
				dialog.dismiss();

			}
		});        
		ll.addView(b1);

		Button b2 = new Button(mContext);
		b2.setText(context.getString(R.string.remind_later));
		b2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		ll.addView(b2);

		Button b3 = new Button(mContext);
		b3.setText(context.getString(R.string.dont_rate));
		b3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (editor != null) {
					editor.putBoolean("dontshowagain", true);
					editor.commit();
				}
				dialog.dismiss();
			}
		});
		ll.addView(b3);

		dialog.setContentView(ll);        
		dialog.show(); 
	}
}
// see http://androidsnippets.com/prompt-engaged-users-to-rate-your-app-in-the-android-market-appirater