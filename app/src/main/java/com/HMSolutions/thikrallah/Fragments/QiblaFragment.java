package com.HMSolutions.thikrallah.Fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.CustomLocation;
import com.HMSolutions.thikrallah.Utilities.MainInterface;
import com.HMSolutions.thikrallah.compass.Compass;
import com.HMSolutions.thikrallah.compass.SOTWFormatter;



public class QiblaFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener, DialogInterface.OnDismissListener {
	private MainInterface mCallback;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private CheckBox is_Manual_Location;
    private TextView currentLocation;
    private static final String TAG = "CompassActivity";
    private Compass compass;
    private ImageView arrowView;
    private ImageView dialView;
   // private TextView sotwLabel;  // SOTW is for "side of the world"
    private Context mContext;
    private float currentAzimuth;
    private float currentQibla;
    private SOTWFormatter sotwFormatter;

    //  private TextView locationDescription;


    public QiblaFragment() {
	}
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase("latitude") || key.equalsIgnoreCase("longitude")
                || key.equalsIgnoreCase("isCustomLocation")||key.equalsIgnoreCase("c_latitude")
                ||key.equalsIgnoreCase("c_longitude")) {
            if (this.getView()!=null){
                updateQiblaDirection();
                is_Manual_Location.setChecked(PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation",false));
            }

        }

    }

    private void updateQiblaDirection() {
        this.calculateQiblaDirection();
        boolean isLocationManual = PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation", false);
        if (isLocationManual){
            currentLocation.setText(
                    PreferenceManager.getDefaultSharedPreferences(this.getContext()).getString("city", "")+", "+
                            PreferenceManager.getDefaultSharedPreferences(this.getContext()).getString("country", ""));
        }else{
            currentLocation.setText(this.getContext().getResources().getString(R.string.current_location)+MainActivity.getLatitude(getContext())+", "+ MainActivity.getLongitude(getContext()));
        }
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext=context;
        MainActivity.setLocale(context);
        try {
            prefListener = this;
            mCallback = (MainInterface) context;
            mCallback.requestLocationUpdate();


		} catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement MainInterface");
        }
        Log.d(TAG, "start compass");

		 
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        mContext=this.getActivity();
        MainActivity.setLocale(this.getContext());
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        this.setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.compass_fragment, container,
                false);

        sotwFormatter = new SOTWFormatter(this.getActivity().getApplicationContext());

        arrowView = view.findViewById(R.id.main_image_hands);
        dialView = view.findViewById(R.id.main_image_dial);
        setupCompass();
        //locationDescription=(TextView)  view.findViewById(R.id.textView_location);

        is_Manual_Location= (CheckBox) view.findViewById(R.id.is_manual_location);
        if (PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation", false)) {
            is_Manual_Location.setChecked(true);
        }else{
            is_Manual_Location.setChecked(false);
        }

        is_Manual_Location.setOnClickListener(this);
        currentLocation= view.findViewById(R.id.current_location);
        currentLocation.setOnClickListener(this);
        this.updateQiblaDirection();
        PreferenceManager.getDefaultSharedPreferences(this.getContext()).registerOnSharedPreferenceChangeListener(prefListener);
        return view;
	}

    private void setupCompass() {
        compass = new Compass(this.getActivity().getApplicationContext());
        Compass.CompassListener cl = getCompassListener();
        compass.setListener(cl);
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
    private Compass.CompassListener getCompassListener() {
        return new Compass.CompassListener() {
            @Override
            public void onNewAzimuth(final float azimuth) {
                // UI updates only in UI thread
                // https://stackoverflow.com/q/11140285/444966

                ((MainActivity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adjustArrow(azimuth);
                        adjustSotwLabel(azimuth);
                    }
                });
            }
        };
    }

    private void adjustArrow(float azimuth) {
        Animation an = new RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        float newQibla=(float)this.calculateQiblaDirection();
        Animation an2 = new RotateAnimation(currentQibla-currentAzimuth, newQibla-azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);

        currentQibla=newQibla;
        currentAzimuth = azimuth;

        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);
        dialView.startAnimation(an);


        an2.setDuration(500);
        an2.setRepeatCount(0);
        an2.setFillAfter(true);

        arrowView.startAnimation(an2);
    }

    private void adjustSotwLabel(float azimuth) {
        //sotwLabel.setText(sotwFormatter.format(azimuth));
    }

	@Override
	public void onPause(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
        compass.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this.getContext()).registerOnSharedPreferenceChangeListener(prefListener);
        logScreen();
        this.updateQiblaDirection();
        Log.d(TAG, "start compass");
        compass.start();
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
                this.updateQiblaDirection();
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

    /**
     * qibla direction in degrees from the north (clock-wise)
     * @return number - 0 means north, 90 means east, 270 means west, etc
     *
     */
    public double calculateQiblaDirection(){
        double latitude = Double.parseDouble(MainActivity.getLatitude(getContext()));
        double longitude = Double.parseDouble(MainActivity.getLongitude(getContext()));
        double lng_a = 39.82616111;
        double lat_a = 21.42250833;
        double deg = Math.toDegrees(Math.atan2(Math.sin(Math.toRadians(lng_a-longitude)),
                Math.cos(Math.toRadians(latitude))*Math.tan(Math.toRadians(lat_a))
                    -Math.sin(Math.toRadians(latitude))*Math.cos(Math.toRadians(lng_a-longitude))));
        if (deg>=0){
            return deg;
        }else{
            return deg+360;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        //called when location dialog is cancelled
        boolean isLocationManual = PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation", false);
        is_Manual_Location.setChecked(isLocationManual);
    }
}
