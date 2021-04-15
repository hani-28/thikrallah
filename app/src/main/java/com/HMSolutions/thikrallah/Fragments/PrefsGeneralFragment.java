package com.HMSolutions.thikrallah.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.Notification.MyAlarmsManager;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.TimePreference;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Locale;

public class PrefsGeneralFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences_general);
		initSummary(getPreferenceScreen());
		//findPreference()
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

	}
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		MainActivity.setLocale(context);
	}

	private void updatePrefSummary(Preference pref) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
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
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        logScreen();
    }

    private void logScreen() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.getClass().getSimpleName());
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.getClass().getSimpleName());
        FirebaseAnalytics.getInstance(this.getActivity()).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
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
		MyAlarmsManager manager=new MyAlarmsManager(this.getActivity().getApplicationContext());
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

	}
}