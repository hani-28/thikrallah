package com.HMSolutions.thikrallah.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.Models.Prayer;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.MainInterface;

import com.HMSolutions.thikrallah.Utilities.PrayTime;
import com.HMSolutions.thikrallah.Utilities.CustumAthanAdapter;

public class AthanFragment extends Fragment implements OnClickListener, AdapterView.OnItemClickListener {



	private Prayer[] prayers;

	private MainInterface mCallback;
    private ListView AthanList;


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
		
		View view = inflater.inflate(R.layout.athan, container,
				false);
		AthanList = (ListView) view.findViewById(R.id.listViewPrayers);

		prayers=getPrayersArray();
        CustumAthanAdapter<Prayer> adapter = new CustumAthanAdapter<Prayer>(getActivity(), R.layout.row_format_athan, prayers);
        AthanList.setAdapter(adapter);
        AthanList.setOnItemClickListener(this);
		return view;
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
	public void onClick(View v) {

	}

	@Override
	public void onPause(){
		super.onPause();
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
	}


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
