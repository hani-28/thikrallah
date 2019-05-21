package com.HMSolutions.thikrallah.Utilities;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.HMSolutions.thikrallah.R;

public class CustumThickerAdapter extends ArrayAdapter<String> {

	View view;
	Context context;

    public void setCurrentPlaying(int currentPlaying) {
        this.currentPlaying = currentPlaying;
    }

    int currentPlaying=-1;
    String[] items;

	public CustumThickerAdapter(Context context, int textViewResourceId, String[] items) {
		super(context, textViewResourceId, items);
        this.items=items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		view = convertView;
		if (view == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(R.layout.row_format, null);

		}
        TextView tv=(TextView) view.findViewById(R.id.toptext1);
        tv.setText(items[position]);
        tv.setTag(position);

        if (position==currentPlaying){
            tv.setBackgroundColor(this.getContext().getResources().getColor(R.color.transperentwhite));

            //tv.setTextColor(Color.RED);
        }else{
            //tv.setTextColor(Color.BLACK);
            tv.setBackgroundColor(Color.TRANSPARENT);
        }
        Log.d("testing123","getview called position="+position+"currentPlaying="+currentPlaying);
		return view;
	}
}