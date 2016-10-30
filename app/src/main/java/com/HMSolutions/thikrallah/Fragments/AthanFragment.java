package com.HMSolutions.thikrallah.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.Models.Prayer;
import com.HMSolutions.thikrallah.Notification.MyAlarmsManager;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.MainInterface;

import com.HMSolutions.thikrallah.Utilities.PrayTime;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class AthanFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, NumberPicker.OnValueChangeListener {



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
    private NumberPicker adjuster_sign;
    private NumberPicker minutes_adjustments;
    private NumberPicker hours_adjustments;

    //  private TextView locationDescription;


    public AthanFragment() {
	}
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase("latitude") || key.equalsIgnoreCase("longitude")) {
            updateprayerTimes();
        }
       /* if (key.equalsIgnoreCase("location")){
            locationDescription.setText(PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("location",""));

        }*/
    }


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);

            prefListener = this;
            prefs.registerOnSharedPreferenceChangeListener(prefListener);

            mCallback = (MainInterface) activity;
            mCallback.requestLocationUpdate();


		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement MainInterface");
		}
		 
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());

		this.getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		this.getActivity().getActionBar().setDisplayShowHomeEnabled(true);
		this.setHasOptionsMenu(true);
		
		View view = inflater.inflate(R.layout.athan_fragment, container,
				false);

        //locationDescription=(TextView)  view.findViewById(R.id.textView_location);

        prayer1_time=(TextView) view.findViewById(R.id.athan_timing1);
        prayer2_time=(TextView) view.findViewById(R.id.athan_timing2);
        prayer3_time=(TextView) view.findViewById(R.id.athan_timing3);
        prayer4_time=(TextView) view.findViewById(R.id.athan_timing4);
        prayer5_time=(TextView) view.findViewById(R.id.athan_timing5);
        sunrise_time=(TextView) view.findViewById(R.id.sunrise_timing1);

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


        adjuster_sign=(NumberPicker) view.findViewById(R.id.adjuster_sign);
        hours_adjustments=(NumberPicker) view.findViewById(R.id.hourPicker);
        minutes_adjustments=(NumberPicker) view.findViewById(R.id.minutePicker);
        hours_adjustments.setMinValue(0);
        hours_adjustments.setMaxValue(1);
        minutes_adjustments.setMinValue(0);
        minutes_adjustments.setMaxValue(59);
        adjuster_sign.setMinValue(0);
        adjuster_sign.setMaxValue(1);

        adjuster_sign.setDisplayedValues( new String[] { "+", "-" } );
       int adjustments=mPrefs.getInt("time_adjustment",0);
        if(adjustments==0){
            hours_adjustments.setValue(0);
            minutes_adjustments.setValue(0);
            adjuster_sign.setValue(0);
        }else{
            hours_adjustments.setValue(adjustments/3600);
            minutes_adjustments.setValue(adjustments-adjustments/3600);

        }

        adjuster_sign.setOnValueChangedListener(this);
        hours_adjustments.setOnValueChangedListener(this);
        minutes_adjustments.setOnValueChangedListener(this);




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
    public void onResume(){
        super.onResume();
        this.updateprayerTimes();
    }
	@Override
	public void onDestroy(){
		super.onDestroy();
	}


    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        int hours=hours_adjustments.getValue();
        int minutes=minutes_adjustments.getValue();
        int sign=adjuster_sign.getValue();
        int multiplier=1;
        if (sign==0){
            multiplier=1;
        }else{
            multiplier=-1;
        }
        int adjustment_in_minutes=(hours*60+minutes)*multiplier;
        mPrefs.edit().putInt("time_adjustment",adjustment_in_minutes).commit();
        Log.d("adjustment","adjustment is "+adjustment_in_minutes);
        updateprayerTimes();

    }
}
