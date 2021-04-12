package com.HMSolutions.thikrallah.Utilities;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;

import androidx.preference.DialogPreference;

import com.HMSolutions.thikrallah.R;

public class TimePreference extends DialogPreference {
    private int lastHour = 0;
    private int lastMinute = 0;
    private String time = "";
    private int mDialogLayoutResId = R.layout.preference_dialog_time;

    public String getTime() {
        return time;
    }

    /**
     * Returns the layout resource that is used as the content View for the dialog
     */
    @Override
    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }

    public static int getHour(String time) {
        String[] pieces = time.split(":", 3);

        return (Integer.parseInt(pieces[0]));
    }

    public static int getMinute(String time) {
        String[] pieces = time.split(":", 3);

        return (Integer.parseInt(pieces[1]));
    }

    public TimePreference(Context ctxt) {
        this(ctxt, null);
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        setPositiveButtonText(R.string.dialog_ok);
        setNegativeButtonText(R.string.cancel);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return(a.getString(index));
    }
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        // If the value can be restored, do it. If not, use the default value.
        Log.d("TimePreference", "onSetInitialValue " + restoreValue + " " + defaultValue);
        String time = null;
        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString("00:00");
                Log.d("TimePreference", "time is " + time + " " + defaultValue);
            } else {
                time = getPersistedString(defaultValue.toString());
            }
        } else {
            time = defaultValue.toString();
        }
        this.time = time;
        this.lastHour = getHour(time);
        this.lastMinute = getMinute(time);
        setValue(time);
    }

    public void setValue(String time) {
        this.time = time;
        this.persistString(time);
        notifyChanged();
    }

}