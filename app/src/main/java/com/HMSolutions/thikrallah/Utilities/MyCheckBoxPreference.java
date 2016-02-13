package com.HMSolutions.thikrallah.Utilities;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by hani on 2/13/16.
 */
public class MyCheckBoxPreference extends CheckBoxPreference {
    public MyCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyCheckBoxPreference(Context context) {
        super(context);
    }
    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        RelativeLayout layout = (RelativeLayout) ((LinearLayout) view).getChildAt(1);
        layout.setGravity(Gravity.RIGHT);
        return view;
    }
}
