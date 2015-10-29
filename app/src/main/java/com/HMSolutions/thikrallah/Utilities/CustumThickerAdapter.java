package com.HMSolutions.thikrallah.Utilities;

import java.util.ArrayList;

import com.HMSolutions.thikrallah.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustumThickerAdapter extends ArrayAdapter<String> {

	View view;
	int pos;
	Context context;
	public CustumThickerAdapter(Context context, int textViewResourceId, ArrayList<String> items) {
		super(context, textViewResourceId, items);
	}
	public CustumThickerAdapter(Context context, int textViewResourceId, String[] items) {
		super(context, textViewResourceId, items);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		view = convertView;
		if (view == null) {
			LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate(R.layout.row_format, null);
			TextView tv=(TextView) view.findViewById(R.id.toptext1);
			tv.setText(this.getItem(position));
		}
		return view;
	}
}