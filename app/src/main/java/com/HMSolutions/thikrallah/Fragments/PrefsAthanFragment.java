package com.HMSolutions.thikrallah.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.Notification.AthanTimerService;
import com.HMSolutions.thikrallah.Notification.MyAlarmsManager;
import com.HMSolutions.thikrallah.Notification.ThikrService;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.ThikrMediaPlayerService;
import com.HMSolutions.thikrallah.Utilities.TimePreference;

import java.util.Locale;

public class PrefsAthanFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences_athan);
		initSummary(getPreferenceScreen());
	}
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		MainActivity.setLocale(context);
	}
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

	}

	private void updatePrefSummary(Preference pref) {

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
		if (pref instanceof ListPreference) {
			Log.d("prefs", "pref is instance of listpreference");
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		}
		if (pref instanceof TimePreference) {
			Log.d("prefs","pref is instance of TimePreference");
			String time=sharedPreferences.getString(pref.getKey(), "00:00");
			String AMPM="AM";
			int hour=TimePreference.getHour(time);
			if (hour>12){
				hour=hour-12;
				AMPM="PM";
			}
			if (hour==0){
				hour=12;
			}
			String hourString="";
			if (hour<10){
				hourString="0"+hour;
			}else{
				hourString=""+hour;
			}
			int minutes=TimePreference.getMinute(time);
			String minutesString="";
			if (minutes<10){
				minutesString="0"+minutes;
			}else{
				minutesString=""+minutes;
			}
			pref.setSummary(hourString+":"+minutesString+" "+AMPM);
		}
	}


	private void initSummary(PreferenceScreen p) {
		if (p != null) {
			PreferenceGroup pGrp = (PreferenceGroup) p;
			for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
				initSummary(pGrp.getPreference(i));
			}
		} else {
			updatePrefSummary(p);
		}
	}
	private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePrefSummary(p);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        logScreen();
    }

    private void logScreen() {
		/*
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.getClass().getSimpleName());
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.getClass().getSimpleName());
        FirebaseAnalytics.getInstance(this.getActivity()).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
   		*/
	}

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        MyAlarmsManager manager = new MyAlarmsManager(this.getActivity().getApplicationContext());
		manager.UpdateAllApplicableAlarms();
		Preference pref = findPreference(key);
		updatePrefSummary(pref);
        if (key.equalsIgnoreCase("language")){
            Intent intent=new Intent();
            intent.setClass(this.getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("FromPreferenceActivity",true);
            this.startActivity(intent);
        }
		if (key.contains("_reminder_type")){//athan type changed
			play_athan(key);
		}
		if (key.equalsIgnoreCase("foreground_athan_timer")){//athan type changed
			boolean isTimer=sharedPreferences.getBoolean("foreground_athan_timer",true);

			if(isTimer){
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					this.getActivity().startForegroundService(new Intent(this.getActivity(),AthanTimerService.class));
				} else {
					this.getActivity().startService(new Intent(this.getActivity(),AthanTimerService.class));
				}
			}else{
				this.getActivity().startService(new Intent(this.getActivity(),AthanTimerService.class));
			}

			//AthanTimerService.enqueueWork(this.getActivity(), new Intent(this.getActivity(),AthanTimerService.class));
		}
	}
	private void play_athan(String key) {
		Bundle data=new Bundle();
		data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAY);
		switch (key){
			case "fajr_reminder_type":
				data.putString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_ATHAN1);
				break;
			case "duhr_reminder_type":
				data.putString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_ATHAN2);
				break;
			case "asr_reminder_type":
				data.putString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_ATHAN3);
				break;
			case "maghrib_reminder_type":
				data.putString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_ATHAN4);
				break;
			case "isha_reminder_type":
				data.putString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_ATHAN5);
				break;
		}
		data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAY);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
		int file=Integer.parseInt(sharedPreferences.getString(key,"3"));
		data.putInt("FILE", file);
		sendActionToMediaService(data);
	}
	public void sendActionToMediaService(Bundle data){
		if (data!=null){
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				this.getActivity().startForegroundService(new Intent(this.getActivity(), ThikrService.class).putExtras(data));
			} else {
				this.getActivity().startService(new Intent(this.getActivity(), ThikrService.class).putExtras(data));
			}

		}

	}
}