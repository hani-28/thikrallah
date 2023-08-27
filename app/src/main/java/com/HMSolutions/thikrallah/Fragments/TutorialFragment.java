package com.HMSolutions.thikrallah.Fragments;

import static android.content.Context.POWER_SERVICE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.MainInterface;

public class TutorialFragment extends Fragment {
    private static final String TAG ="TutorialFragment" ;
    private MainInterface mCallback;
    private Context mContext;
    private TextView Tutorial_description;
    private int count;
    final private int final_count = 4;


    public TutorialFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        logScreen();
    }

    private void logScreen() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity.setLocale(context);
        this.mContext = context;
        try {
            mCallback = (MainInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement MainInterface");
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        MainActivity.setLocale(this.getContext());
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((AppCompatActivity) this.getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        View view = inflater.inflate(R.layout.tutorial, container,
                false);
        Button button_next = (Button) view.findViewById(R.id.next);
        button_next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextScreen(count);
            }
        });
        Tutorial_description = (TextView) view.findViewById(R.id.tutorial_description);
        Tutorial_description.setText(1+" / "+this.final_count);
        count=0;
        showNextScreen(count);


        return view;
	}

    private void showNextScreen(int i){
        FragmentTransaction ft = null;
        ft = this.getChildFragmentManager()
                .beginTransaction();

        PrefsThikrFragmentTutorial fragment = new PrefsThikrFragmentTutorial();
        Log.d("testing321", "" + i);
        Bundle data = new Bundle();
        switch (i) {
            case 0:
                data.putInt(PrefsThikrFragmentTutorial.PREF_XML_FILE, R.xml.prefs_tutorial1);
                break;
            case 1:
                data.putInt(PrefsThikrFragmentTutorial.PREF_XML_FILE, R.xml.prefs_tutorial2);
                break;
            case 2:
                requestBatteryExclusion(mContext) ;
                data.putInt(PrefsThikrFragmentTutorial.PREF_XML_FILE,R.xml.prefs_tutorial3);
                break;
            case 3:
                data.putInt(PrefsThikrFragmentTutorial.PREF_XML_FILE,R.xml.prefs_tutorial4);
                break;
            default:
                data=null;
                break;
        }
        Tutorial_description.setText((i+1)+" / "+final_count);
        if (data != null) {
            fragment.setArguments(data);
            ft.replace(R.id.preference_container, fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commitAllowingStateLoss();
        }else{
            PreferenceManager.getDefaultSharedPreferences(this.getActivity()).edit().putBoolean("isFirstLaunch", false).apply();
            this.getActivity().onBackPressed();
            fragment.setArguments(data);
            ft.replace(R.id.preference_container, new MainFragment());
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commitAllowingStateLoss();
            count++;
            return;
        }
        count++;
        if (count>final_count){
			PreferenceManager.getDefaultSharedPreferences(this.getActivity()).edit().putBoolean("isFirstLaunch", false).apply();
			this.getActivity().onBackPressed();
			//mCallback.launchFragment(new MainFragment(),null,"MainFragment");
		}
    }

    private void requestBatteryExclusion(Context mContext) {
        Log.d(TAG,"requestBatteryExclusion");
        PowerManager powerManager = (PowerManager) mContext.getSystemService(POWER_SERVICE);
        String packageName = "com.HMSolutions.thikrallah";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(this.getResources().getString(R.string.power_Exclusion)).setMessage(this.getResources().getString(R.string.power_Exclusion_message))
                        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                intent.setData(Uri.parse("package:" + mContext.getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setCancelable(false)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create().show();
            }
        }
    }

}
