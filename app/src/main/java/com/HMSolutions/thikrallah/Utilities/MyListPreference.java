package com.HMSolutions.thikrallah.Utilities;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by hani on 2/13/16.
 */
public class MyListPreference extends ListPreference {


    public MyListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyListPreference(Context context) {
        super(context);
    }
    @Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);

        RelativeLayout layout = (RelativeLayout) ((LinearLayout) view).getChildAt(1);
        layout.setGravity(Gravity.START);
        return view;
    }
}
