package com.HMSolutions.thikrallah.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceManager;

public class TimePreferenceUI extends PreferenceDialogFragmentCompat {
    private int lastHour = 0;
    private int lastMinute = 0;
    private TimePicker picker = null;

    public static TimePreferenceUI newInstance(Preference preference) {
        final TimePreferenceUI
                fragment = new TimePreferenceUI();


        final Bundle b = new Bundle();
        b.putString(ARG_KEY, preference.getKey());
        fragment.setArguments(b);
        return fragment;
    }

    public static int getHour(String time) {
        String[] pieces = time.split(":", 3);
        return (Integer.parseInt(pieces[0]));
    }

    public static int getMinute(String time) {
        String[] pieces = time.split(":", 3);
        return (Integer.parseInt(pieces[1]));
    }


    @Override
    protected View onCreateDialogView(Context mContext) {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String time = mPrefs.getString(getPreference().getKey(), "00:00");

        lastHour = TimePreferenceUI.getHour(time);
        lastMinute = TimePreferenceUI.getMinute(time);
        picker = new TimePicker(mContext);
        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker = (TimePicker) v;
        picker.setCurrentHour(lastHour);
        picker.setCurrentMinute(lastMinute);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        // super.onDialogClosed(positiveResult);
        if (positiveResult) {
            lastHour = picker.getCurrentHour();
            lastMinute = picker.getCurrentMinute();

            String time = lastHour + ":" + lastMinute;

            // Save the value
            DialogPreference preference = getPreference();
            if (preference instanceof TimePreference) {
                TimePreference timePreference = ((TimePreference) preference);
                // This allows the client to ignore the user value.
                timePreference.setValue(time);
                if (timePreference.callChangeListener(time)) {
                    // Save the value
                    timePreference.setValue(time);
                }
            }
        }
    }
}