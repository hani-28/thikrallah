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

public class QuranFragment extends ListFragment implements OnClickListener {


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


	public QuranFragment() {
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
        if(this.getArguments().getString("DataType").contains("/")){
            thikrType=this.getArguments().getString("DataType");
        }else{
            thikrType=this.getArguments().getString("DataType")+"/"+this.getArguments().getInt("surat");;
        }

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
        playNext.setVisibility(View.INVISIBLE);
        playPrevious.setVisibility(View.INVISIBLE);
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

	
	private String[] getThikrArray(){
		String[] numbers_text = null;

        surat=this.getArguments().getInt("surat");
        String SuratText="";

        SuratText=this.getActivity().getResources().getStringArray(R.array.surat_text)[surat];

        numbers_text=SuratText.split("\\(\\uFEFF*\\d+\\uFEFF*\\)");
        for (int i=1;i<=numbers_text.length;i++){
            numbers_text[i-1]=numbers_text[i-1]+"("+i+")";
        }
        return numbers_text;//new String[]{};


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
        /*
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
        */


    }
}
