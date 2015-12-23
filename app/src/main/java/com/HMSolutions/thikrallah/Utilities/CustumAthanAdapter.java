package com.HMSolutions.thikrallah.Utilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Models.Prayer;
import java.util.ArrayList;

public class CustumAthanAdapter<Prayer> extends ArrayAdapter<Prayer> {

    View view;
    int pos;
    Context context;
    Prayer[] prayers;
    private String[] PrayTimes;
    private String[] PrayerNames;


    public CustumAthanAdapter(Context context, int textViewResourceId, ArrayList<Prayer> items) {
        super(context, textViewResourceId, items);
    }

    public CustumAthanAdapter(Context context, int textViewResourceId, Prayer[] items) {
        super(context, textViewResourceId, items);
        prayers=items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.row_format_athan, null);
            TextView athanName = (TextView) view.findViewById(R.id.athan_name);
            TextView athanTime = (TextView) view.findViewById(R.id.athan_time);
           String name = ((com.HMSolutions.thikrallah.Models.Prayer)this.prayers[position]).getName();
            String time=((com.HMSolutions.thikrallah.Models.Prayer)this.prayers[position]).getTime();
           athanName.setText(name);
           athanTime.setText(time);
        }
        return view;
    }
}

