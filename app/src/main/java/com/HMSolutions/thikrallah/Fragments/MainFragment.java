package com.HMSolutions.thikrallah.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.PreferenceActivity;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.MainInterface;
import com.HMSolutions.thikrallah.hisnulmuslim.DuaGroupActivity;
import com.HMSolutions.thikrallah.quran.labs.androidquran.QuranDataActivity;

import java.util.Locale;

public class MainFragment extends Fragment {
    private MainInterface mCallback;
    private Context mContext;
    SharedPreferences mPrefs;


    public MainFragment() {
    }

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		Log.d("MainFragment","onattach called");

		MainActivity.setLocale(context);

		mContext = context;
		try {
			mCallback = (MainInterface) mContext;
		} catch (ClassCastException e) {
			throw new ClassCastException(mContext.toString()
					+ " must implement MainInterface");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		MainActivity.setLocale(this.getContext());
		((AppCompatActivity) this.getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		((AppCompatActivity) this.getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

		View view = inflater.inflate(R.layout.fragment_main, container,
				false);
		Button button_remind_me_settings = (Button) view.findViewById(R.id.button_settings);

		Button button_morning_thikr = (Button) view.findViewById(R.id.button_morning_thikr);
		Button button_night_thikr = (Button) view.findViewById(R.id.button_night_thikr);
		//Button button_donate = (Button) view.findViewById(R.id.butt);
		Button button_my_athkar = (Button) view.findViewById(R.id.button_my_athkar);
		Button button_sadaqa = (Button) view.findViewById(R.id.button_sadaqa);
		Button button_quran = (Button) view.findViewById(R.id.button_quran);
		Button button_hisn_almuslim = (Button) view.findViewById(R.id.hisn_almuslim);
		Button button_athan = (Button) view.findViewById(R.id.button_athan);
		button_athan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mCallback.launchFragment(new AthanFragment(), new Bundle(), "AthanFragment");
				
			}
			
		});
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());



        button_quran.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


				Intent intent = new Intent();
				intent.setClass(v.getContext(), QuranDataActivity.class);
				startActivityForResult(intent, 0);


            }
        });
        button_sadaqa.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.share();
            }
        });
        /*
        button_donate.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                mCallback.upgrade();

            }

        });
		*/
		button_remind_me_settings.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(v.getContext(), PreferenceActivity.class);
				startActivityForResult(intent, 0); 
			}});

        button_hisn_almuslim.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(v.getContext(), DuaGroupActivity.class);
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
                mCallback.launchFragment(new ThikrFragment(), data, "ThikrFragment");
            }
        });
        button_my_athkar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle data = new Bundle();
                mCallback.launchFragment(new MyAthkarFragment(), data, "MyAthkarFragment");
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        logScreen();
    }

    private void logScreen() {
        /*
    	Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.getClass().getSimpleName());
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.getClass().getSimpleName());
        FirebaseAnalytics.getInstance(this.getActivity()).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
   */
    }

}
