package com.HMSolutions.thikrallah.Utilities;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.HMSolutions.thikrallah.R;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public class AppRater {
	
	private final static int DAYS_UNTIL_PROMPT = 2;
	private final static int LAUNCHES_UNTIL_PROMPT = 5;
	private  Context context;
	private static  String APP_PNAME = "";
	public void app_launched(WeakReference<Context> mContext) {
		context=mContext.get();
		SharedPreferences prefs;
		if (context!=null){
			Timber.tag("AppRater").d("apprater app_launched called");
			APP_PNAME=context.getResources().getString(R.string.app_package);
			prefs = context.getSharedPreferences("apprater", 0);
		}else{
			return;
		}
		if (prefs.getBoolean("dontshowagain", false)) { return ; }

		SharedPreferences.Editor editor = prefs.edit();

		// Increment launch counter
		long launch_count = prefs.getLong("launch_count", 0) + 1;
		editor.putLong("launch_count", launch_count);

		// Get date of first launch
		long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
		if (date_firstLaunch == 0) {
			date_firstLaunch = System.currentTimeMillis();
			editor.putLong("date_firstlaunch", date_firstLaunch);
		}
		Timber.d("apprater called and showrateDialog");
		// Wait at least n days before opening
		if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
			if (System.currentTimeMillis() >= date_firstLaunch + 
					(DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
				if (context!=null){
					showRateDialog(context, editor);
				}

			}
		}

		editor.apply();
	}   

	public void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
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
		b1.setOnClickListener(v -> {
			if (editor != null) {
				editor.putBoolean("dontshowagain", true);
				editor.commit();
			}
			Intent intent =new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME));
			if (intent.resolveActivity(context.getPackageManager()) != null) {
				mContext.startActivity(intent);
			}else{
				Timber.d("playstore not available");
			}

			dialog.dismiss();

		});
		ll.addView(b1);

		Button b2 = new Button(mContext);
		b2.setText(context.getString(R.string.remind_later));
		b2.setOnClickListener(v -> dialog.dismiss());
		ll.addView(b2);

		Button b3 = new Button(mContext);
		b3.setText(context.getString(R.string.dont_rate));
		b3.setOnClickListener(v -> {
			if (editor != null) {
				editor.putBoolean("dontshowagain", true);
				editor.commit();
			}
			dialog.dismiss();
		});
		ll.addView(b3);

		dialog.setContentView(ll);        
		dialog.show(); 
	}
}
// see http://androidsnippets.com/prompt-engaged-users-to-rate-your-app-in-the-android-market-appirater