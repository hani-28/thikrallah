package com.HMSolutions.thikrallah.Fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.CustumThickerAdapter;
import com.HMSolutions.thikrallah.Utilities.MainInterface;

public class ThikrFragment extends ListFragment implements OnClickListener {
	

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
    private CustumThickerAdapter adapter;
    private int surat=0;

    /**
	 * @return the currentPlaying
	 */
	public int getCurrentPlaying() {
		return currentPlaying;
	}


	public ThikrFragment() {
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
		
		mCallback.setCurrentPlaying(this.thikrType,1);
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
        adapter = new CustumThickerAdapter(getActivity(), R.layout.row_format,thickerArray );
		setListAdapter(adapter); 
		//adapter.addAll(thickerArray);
	


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
				mCallback.pausePlayer(this.thikrType);
			}else{
				mCallback.resetPlayer(this.thikrType);
				mCallback.setCurrentPlaying(this.thikrType,position + 1);
                setCurrentlyPlaying(position+1);
				mCallback.play(this.thikrType,mCallback.getCurrentPlaying());
			}
			mCallback.pausePlayer(this.thikrType);
		} else {
			mCallback.play(this.thikrType,position + 1);
            setCurrentlyPlaying(position+1);
		}
	}

	@Override
	public void onResume() {
		mCallback.requestMediaServiceStatus();
		super.onResume();
	}

	private String[] getThikrArray(){
		String[] numbers_text = null;
		if (this.thikrType.equals(MainActivity.DATA_TYPE_DAY_THIKR)){
			return getResources().getStringArray(R.array.MorningThikr);
		}
		if (this.thikrType.equals(MainActivity.DATA_TYPE_NIGHT_THIKR)){
			return getResources().getStringArray(R.array.NightThikr);
		}
        if (this.thikrType.equals(MainActivity.DATA_TYPE_QURAN)){
         //   surat=this.getArguments().getInt("surat");
           // return new String[]{this.getActivity().getResources().getStringArray(R.array.surat_text)[surat]};
        }
		return numbers_text;
	}
	@Override
	public void onClick(View v) {
		if (v==this.stop){
			this.mCallback.resetPlayer(this.thikrType);;
		}
		if (v==this.pause){
			//player.pause();
			this.mCallback.pausePlayer(this.thikrType);
			
		}
		if (v==this.play && this.mCallback.isPlaying()==false){
            setCurrentlyPlaying(this.getCurrentPlaying());
            mCallback.playAll(this.thikrType);

		}
		if (v==this.playNext){
			mCallback.incrementCurrentPlaying(this.thikrType,1);
			mCallback.playAll(this.thikrType);
		}
		if (v==this.playPrevious){
			mCallback.incrementCurrentPlaying(this.thikrType,-1);
			mCallback.playAll(this.thikrType);
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
    private int smoothpositionOn=-1;
    public void setCurrentlyPlaying(int position) {
        if(smoothpositionOn!=position){
            if(position>0){
                getListView().smoothScrollToPosition(position-1);
                Log.d("testing321","smoothTo"+position);
            }
            //  getListView().smoothScrollToPosition(position-1);
            adapter.setCurrentPlaying(position-1);
            this.adapter.notifyDataSetChanged();
            smoothpositionOn=position;
        }


    }
}
