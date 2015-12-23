package com.HMSolutions.thikrallah.Fragments;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.MainInterface;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class AthanFragment extends ListFragment implements OnClickListener {


	private String thikrType;
	private int currentPlaying;
	private String[] thickerArray;
	private Button play;
	private Button pause;
	private Button stop;
	private Button playNext;
	private Button playPrevious;
	public int counter=0;
	public int currentThikrCounter=0;

	private MainInterface mCallback;
	/**
	 * @return the currentPlaying
	 */
	public int getCurrentPlaying() {
		return currentPlaying;
	}

	/**
	 * @param currentPlaying the currentPlaying to set
	 */

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
		
		mCallback.setCurrentPlaying(1);
		thikrType=this.getArguments().getString("DataType");
		mCallback.setThikrType(thikrType);
		View view = inflater.inflate(R.layout.fragment_thikr, container,
				false);
		play = (Button) view.findViewById(R.id.button_play);
		pause = (Button) view.findViewById(R.id.button_pause);
		stop = (Button) view.findViewById(R.id.button_stop);
		play.setOnClickListener(this);
		pause.setOnClickListener(this);
		stop.setOnClickListener(this);
		playNext = (Button) view.findViewById(R.id.button_next);
		playNext.setOnClickListener(this);
		playPrevious = (Button) view.findViewById(R.id.button_previous);
		playPrevious.setOnClickListener(this);
		thickerArray=getThikrArray();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.row_format, R.id.toptext1);
		setListAdapter(adapter); 
		adapter.addAll(thickerArray);
	


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
	@Override  
	public void onListItemClick(ListView l, View v, int position, long id) {  
		if (this.mCallback.isPlaying()) {
			if (mCallback.getCurrentPlaying()==position+1){
				mCallback.pausePlayer();
			}else{
				mCallback.resetPlayer();
				mCallback.setCurrentPlaying(position+1);
				mCallback.play(mCallback.getCurrentPlaying());
			}
			mCallback.pausePlayer();
		} else {
			mCallback.play(position+1);
		}
	}
	
	private String[] getThikrArray(){
		String[] numbers_text = null;
		if (this.thikrType.equals(MainActivity.DATA_TYPE_DAY_THIKR)){
			numbers_text = getResources().getStringArray(R.array.MorningThikr);
		}
		if (this.thikrType.equals(MainActivity.DATA_TYPE_NIGHT_THIKR)){
			numbers_text = getResources().getStringArray(R.array.NightThikr); 
		}
		return numbers_text;
	}
	@Override
	public void onClick(View v) {
		if (v==this.stop){
			this.mCallback.resetPlayer();;
		}
		if (v==this.pause){
			//player.pause();
			this.mCallback.pausePlayer();
			
		}
		if (v==this.play && this.mCallback.isPlaying()==false){
			mCallback.playAll();
		}
		if (v==this.playNext){
			mCallback.incrementCurrentPlaying(1);
			mCallback.playAll();
		}
		if (v==this.playPrevious){
			mCallback.incrementCurrentPlaying(-1);
			mCallback.playAll();
		}



	}

	@Override
	public void onPause(){
		super.onPause();
		//player.pause();
	}
	@Override
	public void onDestroy(){
		super.onDestroy();
		//player.reset();
		//player.release();
	}
	
	

}
