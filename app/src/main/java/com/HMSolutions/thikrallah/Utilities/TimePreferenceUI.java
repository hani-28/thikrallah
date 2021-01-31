package com.HMSolutions.thikrallah.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceManager;

import com.HMSolutions.thikrallah.R;

import java.util.Locale;

public class TimePreferenceUI extends PreferenceDialogFragmentCompat {
    private int lastHour = 0;
    private int lastMinute = 0;
    private TimePicker picker = null;
    private View view = null;
    private TextView minutes_text;
    private TextView hour_text;
    private boolean isPickerSupported;
    private String TAG = "TimePreferenceUI";
    private RadioGroup am_pm_selection;
    private RadioButton am;
    private RadioButton pm;

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
        isPickerSupported = true;
        if ((android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) && Build.MANUFACTURER.toLowerCase(Locale.ENGLISH).contains("samsung")) {
            isPickerSupported = false;
        }

        lastHour = TimePreferenceUI.getHour(time);
        lastMinute = TimePreferenceUI.getMinute(time);
        if (!isPickerSupported) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.time_picker_alternative, null);
            hour_text = view.findViewById(R.id.hour_text);
            minutes_text = view.findViewById(R.id.minute_text);
            am_pm_selection = view.findViewById(R.id.am_pm);
            am = view.findViewById(R.id.am);
            pm = view.findViewById(R.id.pm);

            return view;
        } else {

            //samsung devices SDK 23 and below crash with built-in TimePicker
            picker = new TimePicker(mContext);
            return picker;
        }
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        if (!isPickerSupported) {
            am_pm_selection.clearCheck();
            if (lastHour > 12) {
                pm.setChecked(true);
                hour_text.setText(String.valueOf(lastHour - 12));
            } else {
                am.setChecked(true);
                hour_text.setText(String.valueOf(lastHour));
            }

            minutes_text.setText(String.valueOf(lastMinute));

        } else {
            picker = (TimePicker) v;
            picker.setCurrentHour(lastHour);
            picker.setCurrentMinute(lastMinute);
        }

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        // super.onDialogClosed(positiveResult);
        if (positiveResult) {
            if (!isPickerSupported) {

                //hour_text.setText(String.valueOf(lastHour));
                //minutes_text.setText(String.valueOf(lastMinute));
                try {
                    int hour = Integer.parseInt(hour_text.getText().toString());
                    if (hour > 0 && hour < 13) {
                        if (pm.isChecked()) {
                            hour = hour + 12;
                        }
                        lastHour = hour;
                        int minutes = Integer.parseInt(minutes_text.getText().toString());
                        if (minutes >= 0 && minutes < 60) {
                            lastMinute = minutes;
                            Log.d(TAG, "hour: " + lastHour + " minutes " + lastMinute);
                            recordPref();
                        } else {
                            Toast.makeText(this.getActivity().getApplicationContext(), R.string.invalid_input,
                                    Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(this.getActivity().getApplicationContext(), "Invalid input",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.getMessage());
                }

            } else {
                lastHour = picker.getCurrentHour();
                lastMinute = picker.getCurrentMinute();
                recordPref();
            }
        }
    }

    private void recordPref() {
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