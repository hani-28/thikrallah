package com.HMSolutions.thikrallah.Fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.icu.util.IslamicCalendar;
import android.icu.util.ULocale;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import net.time4j.*;
import net.time4j.calendar.HijriCalendar;
import net.time4j.format.expert.ChronoFormatter;
import net.time4j.format.expert.PatternType;

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


public class AthanFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener,  DialogInterface.OnDismissListener {



	private Prayer[] prayers;

	private MainInterface mCallback;
    private ListView AthanList;
    private TextView prayer1_time;
    private TextView HijriDate;
    private TextView prayer2_time;
    private TextView prayer3_time;
    private TextView prayer4_time;
    private TextView prayer5_time;
    private TextView sunrise_time;
    private SwitchCompat fajr_switch;
    private SwitchCompat duhr_switch;
    private SwitchCompat asr_switch;
    private SwitchCompat maghrib_switch;
    private SwitchCompat ishaa_switch;
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
            if (this.getView()!=null){
                updateprayerTimes();
                boolean isLocationManual = PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation", false);
                is_Manual_Location.setChecked(isLocationManual);
                if (isLocationManual){
                    currentLocation.setText(
                            PreferenceManager.getDefaultSharedPreferences(this.getContext()).getString("city", "")+", "+
                                    PreferenceManager.getDefaultSharedPreferences(this.getContext()).getString("country", ""));
                }else{
                    currentLocation.setText(this.getContext().getResources().getString(R.string.current_location)+MainActivity.getLatitude(getContext())+", "+ MainActivity.getLongitude(getContext()));
                }

            }

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
            prefListener = this;
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
        HijriDate= (TextView) view.findViewById(R.id.Hijri_date);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            IslamicCalendar islamic_cal = (IslamicCalendar) IslamicCalendar
                    .getInstance(new ULocale("ar_SA@calendar=islamic"));
            islamic_cal.setCalculationType(IslamicCalendar.CalculationType.ISLAMIC);
            HijriDate.setText(getHijriDate());
        }

        prayer1_time = (TextView) view.findViewById(R.id.athan_timing1);
        prayer2_time = (TextView) view.findViewById(R.id.athan_timing2);
        prayer3_time = (TextView) view.findViewById(R.id.athan_timing3);
        prayer4_time=(TextView) view.findViewById(R.id.athan_timing4);
        prayer5_time=(TextView) view.findViewById(R.id.athan_timing5);
        sunrise_time=(TextView) view.findViewById(R.id.sunrise_timing1);
        is_Manual_Location= (CheckBox) view.findViewById(R.id.is_manual_location);
        currentLocation= view.findViewById(R.id.current_location);
        if (PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation", false)) {
            is_Manual_Location.setChecked(true);
            currentLocation.setText(
                    PreferenceManager.getDefaultSharedPreferences(this.getContext()).getString("city", "")+", "+
                            PreferenceManager.getDefaultSharedPreferences(this.getContext()).getString("country", ""));
        }else{
            is_Manual_Location.setChecked(false);
            currentLocation.setText(this.getContext().getResources().getString(R.string.current_location)+MainActivity.getLatitude(getContext())+", "+ MainActivity.getLongitude(getContext()));

        }
        currentLocation.setOnClickListener(this);
        is_Manual_Location.setOnClickListener(this);

        fajr_switch=(SwitchCompat) view.findViewById(R.id.switch1);
        duhr_switch=(SwitchCompat) view.findViewById(R.id.switch2);
        asr_switch=(SwitchCompat) view.findViewById(R.id.switch3);
        maghrib_switch=(SwitchCompat) view.findViewById(R.id.switch4);
        ishaa_switch=(SwitchCompat) view.findViewById(R.id.switch5);

        fajr_switch.setChecked(mPrefs.getBoolean("isFajrReminder",true));
        duhr_switch.setChecked(mPrefs.getBoolean("isDuhrReminder",true));
        asr_switch.setChecked(mPrefs.getBoolean("isAsrReminder",true));
        maghrib_switch.setChecked(mPrefs.getBoolean("isMaghribReminder",true));
        ishaa_switch.setChecked(mPrefs.getBoolean("isIshaaReminder",true));




        fajr_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked==true){
                mPrefs.edit().putBoolean("isFajrReminder",true).apply();
            }else{
                mPrefs.edit().putBoolean("isFajrReminder",false).apply();
            }
            updateAthanAlarms();
        });


        duhr_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked==true){
                mPrefs.edit().putBoolean("isDuhrReminder",true).apply();
            }else{
                mPrefs.edit().putBoolean("isDuhrReminder",false).apply();
            }
            updateAthanAlarms();

        });


        asr_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked==true){
                mPrefs.edit().putBoolean("isAsrReminder",true).apply();
            }else{
                mPrefs.edit().putBoolean("isAsrReminder",false).apply();
            }
            updateAthanAlarms();
        });


        maghrib_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked==true){
                mPrefs.edit().putBoolean("isMaghribReminder",true).apply();
            }else{
                mPrefs.edit().putBoolean("isMaghribReminder",false).apply();
            }
            updateAthanAlarms();

        });


        ishaa_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked==true){
                mPrefs.edit().putBoolean("isIshaaReminder",true).apply();
            }else{
                mPrefs.edit().putBoolean("isIshaaReminder",false).apply();
            }
            updateAthanAlarms();
        });
        PreferenceManager.getDefaultSharedPreferences(this.getContext()).registerOnSharedPreferenceChangeListener(prefListener);
        this.updateprayerTimes();
        return view;
	}

    private void updateprayerTimes() {
        double latitude =  Double.parseDouble(MainActivity.getLatitude(this.getContext()));
        double longitude = Double.parseDouble(MainActivity.getLongitude(this.getContext()));
        if (latitude==0.0 && longitude==0.0){
            prayer1_time.setText("NA:NA");
            sunrise_time.setText("NA:NA");
            prayer2_time.setText("NA:NA");
            prayer3_time.setText("NA:NA");

            prayer4_time.setText("NA:NA");
            prayer5_time.setText("NA:NA");
            return;
        }
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
        new MyAlarmsManager(this.getActivity()).UpdateAllApplicableAlarms();
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
        PreferenceManager.getDefaultSharedPreferences(this.getContext()).registerOnSharedPreferenceChangeListener(prefListener);
        logScreen();
        this.updateprayerTimes();
    }

    private void logScreen() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.is_manual_location:
                if (is_Manual_Location.isChecked()){
                    CustomLocation Customlocation=new CustomLocation(this.getActivity());
                    Customlocation.setCanceledOnTouchOutside(true);
                    Customlocation.setOnDismissListener(this);
                    Customlocation.show();
                }else{
                    PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit().putBoolean("isCustomLocation", false).apply();
                }
                currentLocation.setText(this.getContext().getResources().getString(R.string.current_location)+MainActivity.getLatitude(getContext())+", "+ MainActivity.getLongitude(getContext()));
                updateprayerTimes();
                updateAthanAlarms();
                break;
            case R.id.current_location:
                boolean isLocationManual = PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation", false);
                if (isLocationManual){
                    CustomLocation Customlocation=new CustomLocation(this.getActivity());
                    Customlocation.setCanceledOnTouchOutside(true);
                    Customlocation.setOnDismissListener(this);
                    Customlocation.show();
                }
                break;
        }
    }
    private String getHijriDate(){

        ChronoFormatter<HijriCalendar> hijriFormat =
                ChronoFormatter.setUp(HijriCalendar.family(), this.getResources().getConfiguration().locale)
                        //.addEnglishOrdinal(HijriCalendar.DAY_OF_MONTH)
                        .addPattern(" dd MMMM yyyy", PatternType.CLDR)
                        .build()
                        .withCalendarVariant(HijriCalendar.VARIANT_UMALQURA);

// conversion from gregorian to hijri-umalqura valid at noon
// (not really valid in the evening when next islamic day starts)
        HijriCalendar today =
                SystemClock.inLocalView().today().transform(
                        HijriCalendar.class,
                        HijriCalendar.VARIANT_UMALQURA
                );
        return hijriFormat.format(today); // 22nd Rajab 1438

    }



    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        Log.d("AthanFragment","onDismiss called. isLocationManual:");
        //called when location dialog is cancelled
        boolean isLocationManual = PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation", false);
        is_Manual_Location.setChecked(isLocationManual);
    }
}
