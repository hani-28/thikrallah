package com.HMSolutions.thikrallah.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
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
        button_next.setOnClickListener(v -> showNextScreen(count));
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
        Bundle data = new Bundle();
        switch (i) {
            case 0:
                data.putInt(PrefsThikrFragmentTutorial.PREF_XML_FILE, R.xml.prefs_tutorial1);
                break;
            case 1:
                mCallback.requestOverLayPermission();
                mCallback.requestNotificationPermission();
                data.putInt(PrefsThikrFragmentTutorial.PREF_XML_FILE, R.xml.prefs_tutorial2);
                break;
            case 2:
                mCallback.requestBatteryExclusion() ;
                data.putInt(PrefsThikrFragmentTutorial.PREF_XML_FILE,R.xml.prefs_tutorial3);
                break;
            case 3:
                mCallback.requestLocationUpdate();
                data.putInt(PrefsThikrFragmentTutorial.PREF_XML_FILE,R.xml.prefs_tutorial4);
                break;
            default:
                mCallback.requestExactAlarmPermission();
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


}
