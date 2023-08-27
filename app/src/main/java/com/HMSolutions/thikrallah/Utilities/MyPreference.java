package com.HMSolutions.thikrallah.Utilities;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.Preference;

/**
 * Created by hani on 2/13/16.
 */
public class MyPreference extends Preference {

    public MyPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyPreference(Context context) {
        super(context);
    }
}
