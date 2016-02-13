package com.HMSolutions.thikrallah.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.HMSolutions.thikrallah.Models.Prayer;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.MainInterface;

import com.HMSolutions.thikrallah.Utilities.PrayTime;

public class AthanFragment extends Fragment {



	private Prayer[] prayers;

	private MainInterface mCallback;
    private ListView AthanList;
    private TextView prayer1_time;
    private TextView prayer2_time;
    private TextView prayer3_time;
    private TextView prayer4_time;
    private TextView prayer5_time;
    private TextView sunrise_time;


    public AthanFragment() {
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mCallback = (MainInterface) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement MainInterface");
		}
		 
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		this.getActivity().getActionBar().setDisplayShowHomeEnabled(true);
		this.setHasOptionsMenu(true);
		
		View view = inflater.inflate(R.layout.athan_fragment, container,
				false);
        prayer1_time=(TextView) view.findViewById(R.id.athan_timing1);
        prayer2_time=(TextView) view.findViewById(R.id.athan_timing2);
        prayer3_time=(TextView) view.findViewById(R.id.athan_timing3);
        prayer4_time=(TextView) view.findViewById(R.id.athan_timing4);
        prayer5_time=(TextView) view.findViewById(R.id.athan_timing5);
        sunrise_time=(TextView) view.findViewById(R.id.sunrise_timing1);

        this.updateprayerTimes();

		return view;
	}

    private void updateprayerTimes() {
        prayers=getPrayersArray();
        prayer1_time.setText(prayers[0].getTime());
        sunrise_time.setText(prayers[1].getTime());
        sunrise_time.setText(prayers[1].getTime());
        prayer2_time.setText(prayers[2].getTime());
        prayer3_time.setText(prayers[3].getTime());
        prayer4_time.setText(prayers[5].getTime());
        prayer5_time.setText(prayers[6].getTime());

    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if (item.getItemId()==android.R.id.home) {
	    // Respond to the action bar's Up/Home button
	        this.getActivity().onBackPressed();
	        return true;
	    }
	    return false;
	}

	
	private Prayer[] getPrayersArray(){
		PrayTime prayersObject=PrayTime.instancePrayTime(this.getActivity());
        String[] times=prayersObject.getPrayerTimes(this.getActivity());
        String[] names=prayersObject.getTimeNames();
        Prayer[] prayers=new Prayer[7];
        for (int i=0;i<7;i++){
            prayers[i]=new Prayer(names[i],times[i]);
        }
		return prayers;
	}

	@Override
	public void onPause(){
		super.onPause();
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
	}


}
