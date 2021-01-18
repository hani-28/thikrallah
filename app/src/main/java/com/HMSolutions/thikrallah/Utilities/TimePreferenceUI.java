package com.HMSolutions.thikrallah.Utilities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;

public class TimePreferenceUI extends PreferenceDialogFragmentCompat {
    private int lastHour = 0;
    private int lastMinute = 0;
    private TimePicker picker = null;
    private TimePreference preference;

    public TimePreferenceUI(Preference preference) {
        this.preference = (TimePreference) preference;
        lastHour = getHour(this.preference.getTime());
        lastMinute = getMinute(this.preference.getTime());

        final Bundle b = new Bundle();
        b.putString(ARG_KEY, preference.getKey());
        this.setArguments(b);
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
        picker = new TimePicker(getContext());
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

            String time = String.valueOf(lastHour) + ":" + String.valueOf(lastMinute);

            if (preference.callChangeListener(time)) {
                preference.setValue(time);
            }
        }
    }
}