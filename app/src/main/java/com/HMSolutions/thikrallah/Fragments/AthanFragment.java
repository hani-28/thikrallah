package com.HMSolutions.thikrallah.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.Models.Prayer;
import com.HMSolutions.thikrallah.Notification.MyAlarmsManager;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.CustomLocation;
import com.HMSolutions.thikrallah.Utilities.MainInterface;
import com.HMSolutions.thikrallah.Utilities.PrayTime;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Locale;

public class AthanFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {



	private Prayer[] prayers;

	private MainInterface mCallback;
    private ListView AthanList;
    private TextView prayer1_time;
    private TextView prayer2_time;
    private TextView prayer3_time;
    private TextView prayer4_time;
    private TextView prayer5_time;
    private TextView sunrise_time;
    private Switch fajr_switch;
    private Switch duhr_switch;
    private Switch asr_switch;
    private Switch maghrib_switch;
    private Switch ishaa_switch;
    private SharedPreferences mPrefs;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private CheckBox is_Manual_Location;
    private TextView currentLocation;


    //  private TextView locationDescription;


    public AthanFragment() {
	}
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase("latitude") || key.equalsIgnoreCase("longitude")
                || key.equalsIgnoreCase("isCustomLocation")||key.equalsIgnoreCase("c_latitude")
                ||key.equalsIgnoreCase("c_longitude")) {
            updateprayerTimes();
            currentLocation.setText(this.getContext().getResources().getString(R.string.current_location)+MainActivity.getLatitude(getContext())+", "+ MainActivity.getLongitude(getContext()));
            is_Manual_Location.setChecked(PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation",false));
        }

    }


       /* if (key.equalsIgnoreCase("location")){
            locationDescription.setText(PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("location",""));

        }*/



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity.setLocale(context);
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            prefListener = this;
            prefs.registerOnSharedPreferenceChangeListener(prefListener);

            mCallback = (MainInterface) context;
            mCallback.requestLocationUpdate();


		} catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement MainInterface");
        }
		 
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        MainActivity.setLocale(this.getContext());

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());

        ((AppCompatActivity) this.getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        this.setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.athan_fragment, container,
                false);

        //locationDescription=(TextView)  view.findViewById(R.id.textView_location);

        prayer1_time = (TextView) view.findViewById(R.id.athan_timing1);
        prayer2_time = (TextView) view.findViewById(R.id.athan_timing2);
        prayer3_time = (TextView) view.findViewById(R.id.athan_timing3);
        prayer4_time=(TextView) view.findViewById(R.id.athan_timing4);
        prayer5_time=(TextView) view.findViewById(R.id.athan_timing5);
        sunrise_time=(TextView) view.findViewById(R.id.sunrise_timing1);
        is_Manual_Location= (CheckBox) view.findViewById(R.id.is_manual_location);
        if (PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation", false)) {
            is_Manual_Location.setChecked(true);
        }else{
            is_Manual_Location.setChecked(false);
        }

        is_Manual_Location.setOnClickListener(this);
        currentLocation= view.findViewById(R.id.current_location);
        currentLocation.setText(this.getContext().getResources().getString(R.string.current_location)+MainActivity.getLatitude(getContext())+", "+ MainActivity.getLongitude(getContext()));
        fajr_switch=(Switch) view.findViewById(R.id.switch1);
        duhr_switch=(Switch) view.findViewById(R.id.switch2);
        asr_switch=(Switch) view.findViewById(R.id.switch3);
        maghrib_switch=(Switch) view.findViewById(R.id.switch4);
        ishaa_switch=(Switch) view.findViewById(R.id.switch5);

        fajr_switch.setChecked(mPrefs.getBoolean("isFajrReminder",true));
        duhr_switch.setChecked(mPrefs.getBoolean("isDuhrReminder",true));
        asr_switch.setChecked(mPrefs.getBoolean("isAsrReminder",true));
        maghrib_switch.setChecked(mPrefs.getBoolean("isMaghribReminder",true));
        ishaa_switch.setChecked(mPrefs.getBoolean("isIshaaReminder",true));




        fajr_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked==true){
                    mPrefs.edit().putBoolean("isFajrReminder",true).commit();
                }else{
                    mPrefs.edit().putBoolean("isFajrReminder",false).commit();
                }
                updateAthanAlarms();
            }
        });


        duhr_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked==true){
                    mPrefs.edit().putBoolean("isDuhrReminder",true).commit();
                }else{
                    mPrefs.edit().putBoolean("isDuhrReminder",false).commit();
                }
                updateAthanAlarms();

            }
        });


        asr_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked==true){
                    mPrefs.edit().putBoolean("isAsrReminder",true).commit();
                }else{
                    mPrefs.edit().putBoolean("isAsrReminder",false).commit();
                }
                updateAthanAlarms();
            }
        });


        maghrib_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked==true){
                    mPrefs.edit().putBoolean("isMaghribReminder",true).commit();
                }else{
                    mPrefs.edit().putBoolean("isMaghribReminder",false).commit();
                }
                updateAthanAlarms();

            }
        });


        ishaa_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked==true){
                    mPrefs.edit().putBoolean("isIshaaReminder",true).commit();
                }else{
                    mPrefs.edit().putBoolean("isIshaaReminder",false).commit();
                }
                updateAthanAlarms();
            }
        });

        this.updateprayerTimes();

		return view;
	}

    private void updateprayerTimes() {
            prayers=getPrayersArray();
            try{
                //locationDescription.setText(PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("location",""));
                prayer1_time.setText(prayers[0].getTime());
                sunrise_time.setText(prayers[1].getTime());
                prayer2_time.setText(prayers[2].getTime());
                prayer3_time.setText(prayers[3].getTime());

                prayer4_time.setText(prayers[5].getTime());
                prayer5_time.setText(prayers[6].getTime());

            }catch(NullPointerException e){

            }

            updateAthanAlarms();


    }
    private void updateAthanAlarms(){
        new MyAlarmsManager(this.getActivity().getApplicationContext()).UpdateAllApplicableAlarms();
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
		PrayTime prayersObject=PrayTime.instancePrayTime(this.getActivity().getApplicationContext());
        String[] times=prayersObject.getPrayerTimes(this.getActivity().getApplicationContext());
        String[] names=prayersObject.getTimeNames();
        Prayer[] prayers=new Prayer[7];
        for (int i=0;i<7;i++){
           prayers[i]=new Prayer(names[i],times[i]);

        }
		return prayers;
	}

	@Override
	public void onPause(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        logScreen();
        this.updateprayerTimes();
    }

    private void logScreen() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, this.getClass().getSimpleName());
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, this.getClass().getSimpleName());
        FirebaseAnalytics.getInstance(this.getActivity()).logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        if (is_Manual_Location.isChecked()){
            CustomLocation Customlocation=new CustomLocation(this.getActivity());
            Customlocation.show();
        }else{
            PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit().putBoolean("isCustomLocation", false).commit();
        }
        currentLocation.setText(this.getContext().getResources().getString(R.string.current_location)+MainActivity.getLatitude(getContext())+", "+ MainActivity.getLongitude(getContext()));
        updateprayerTimes();
        updateAthanAlarms();
    }
}
