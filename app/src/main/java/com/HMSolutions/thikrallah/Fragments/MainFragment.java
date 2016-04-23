package com.HMSolutions.thikrallah.Fragments;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.SetPreferenceActivity;
import com.HMSolutions.thikrallah.Utilities.MainInterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class MainFragment extends Fragment {
	private MainInterface mCallback;
    private Context mContext;

	public MainFragment() {
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        mContext=activity;
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

		this.getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
		this.getActivity().getActionBar().setDisplayShowHomeEnabled(true);
		
		View view = inflater.inflate(R.layout.fragment_main, container,
				false);
		Button button_remind_me_settings = (Button) view.findViewById(R.id.button_settings);

        Button button_morning_thikr = (Button) view.findViewById(R.id.button_morning_thikr);
		Button button_night_thikr = (Button) view.findViewById(R.id.button_night_thikr);
		Button button_donate = (Button) view.findViewById(R.id.button_support_us);
        Button button_my_athkar = (Button) view.findViewById(R.id.button_my_athkar);
        Button button_sadaqa= (Button) view.findViewById(R.id.button_sadaqa);
        Button button_quran= (Button) view.findViewById(R.id.button_quran);

        Button button_athan = (Button) view.findViewById(R.id.button_athan);
        button_athan.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
                mCallback.launchFragment(new AthanFragment(), new Bundle(),"AthanFragment");	;
				
			}
			
		});


        button_quran.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder b = new AlertDialog.Builder(mContext);
                b.setTitle(R.string.choosesura);
                String[] types = mContext.getResources().getStringArray(R.array.surat_list);
                b.setItems(types, new AlertDialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int choice) {
                        dialog.dismiss();
                        Bundle data=new Bundle();
                        data.putString("DataType", MainActivity.DATA_TYPE_QURAN);
                        data.putInt("surat", mContext.getResources().getIntArray(R.array.surat_values)[choice]);
                        mCallback.launchFragment(new QuranFragment(), data, "QuranFragment");
                    }

                });

                b.show();
            }
        });
        button_sadaqa.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.share();
            }
        });
        button_donate.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                mCallback.upgrade();

            }

        });
		
		button_remind_me_settings.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(v.getContext(), SetPreferenceActivity.class);
				startActivityForResult(intent, 0); 
			}});
		
		button_morning_thikr.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle data=new Bundle();
				data.putString("DataType", MainActivity.DATA_TYPE_DAY_THIKR);
				mCallback.launchFragment(new ThikrFragment(), data,"ThikrFragment");
			}});
		
		button_night_thikr.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Bundle data=new Bundle();
				data.putString("DataType", MainActivity.DATA_TYPE_NIGHT_THIKR);
				mCallback.launchFragment(new ThikrFragment(),data,"ThikrFragment");
			}});
        button_my_athkar.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                Bundle data=new Bundle();
                mCallback.launchFragment(new MyAthkarFragment(),data,"MyAthkarFragment");
            }});
		return view;
	}
	
}
