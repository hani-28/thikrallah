package com.HMSolutions.thikrallah.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.Notification.MyAlarmsManager;
import com.HMSolutions.thikrallah.ThikrMediaPlayerService;
import com.HMSolutions.thikrallah.Utilities.TimePreference;
import com.HMSolutions.thikrallah.Utilities.TimePreferenceUI;

public class PrefsThikrFragmentTutorial extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {
	public static String PREF_XML_FILE = "PREF_XML_FILE";
	Context mcontext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		Bundle data = this.getArguments();
		int xml_file = data.getInt(PREF_XML_FILE);
		addPreferencesFromResource(xml_file);
		initSummary(getPreferenceScreen());
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

	}

	private void updatePrefSummary(Preference pref) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mcontext);
		if (pref instanceof ListPreference) {
			Log.d("prefs", "pref is instance of listpreference");
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		}
		if (pref instanceof TimePreference) {
			Log.d("prefs", "pref is instance of TimePreference");
			String time = sharedPreferences.getString(pref.getKey(), "00:00");
			String AMPM = "AM";
			int hour = TimePreference.getHour(time);
			if (hour > 12) {
				hour = hour - 12;
				AMPM = "PM";
			}
			if (hour == 0) {
				hour = 12;
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
	public void onAttach(Context context) {
		super.onAttach(context);
		this.mcontext = context;
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equalsIgnoreCase("volume")) {
			return;
		}
		MyAlarmsManager manager = new MyAlarmsManager(mcontext);
		manager.UpdateAllApplicableAlarms();
		Preference pref = this.findPreference((CharSequence) key);
		updatePrefSummary(pref);
		if (key.equalsIgnoreCase("language")) {
			Intent intent = new Intent();
			intent.setClass(this.getActivity(), MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			// intent.putExtra("FromPreferenceActivity",true);
			this.startActivity(intent);
		}
		if (key.contains("_reminder_type")) {//athan type changed
			play_athan(key);
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
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mcontext);
		int file=Integer.parseInt(sharedPreferences.getString(key,"3"));
		data.putInt("FILE", file);
		sendActionToMediaService(data);
	}

	public void sendActionToMediaService(Bundle data) {
		if (data != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				this.getActivity().startForegroundService(new Intent(this.getActivity(), ThikrMediaPlayerService.class).putExtras(data));
			} else {
				this.getActivity().startService(new Intent(this.getActivity(), ThikrMediaPlayerService.class).putExtras(data));
			}
		}

	}

	@Override
	public void onDisplayPreferenceDialog(Preference preference) {
		DialogFragment dialogFragment = null;
		if (preference instanceof TimePreference) {
			dialogFragment = new TimePreferenceUI(preference);
		}
		if (dialogFragment != null && isAdded()) {
			dialogFragment.setTargetFragment(this, 0);
			dialogFragment.show(this.getParentFragmentManager(), "android.support.v7.preference" +
					".PreferenceFragment.DIALOG");
		} else {
			super.onDisplayPreferenceDialog(preference);
		}
	}
}