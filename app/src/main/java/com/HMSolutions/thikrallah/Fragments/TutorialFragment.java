package com.HMSolutions.thikrallah.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.PreferenceActivity;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.MainInterface;

public class TutorialFragment extends Fragment {
	private MainInterface mCallback;
    private Context mContext;
	private TextView Tutorial_description;
    private int count;
    final private int final_count=4;


    public TutorialFragment() {
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
		Tutorial_description.setText(1+" / "+(this.final_count));
        count=0;
        showNextScreen(count);


        return view;
	}

    private void showNextScreen(int i){
        FragmentTransaction ft = getFragmentManager()
                .beginTransaction();
        PrefsThikrFragmentTutorial fragment = new PrefsThikrFragmentTutorial();
        Log.d("testing321",""+i);
        Bundle data = new Bundle();
        switch (i){
            case 0:
                data.putInt(PrefsThikrFragmentTutorial.PREF_XML_FILE,R.xml.prefs_tutorial1);
                break;
            case 1:
                data.putInt(PrefsThikrFragmentTutorial.PREF_XML_FILE,R.xml.prefs_tutorial2);
                break;
            case 2:
                data.putInt(PrefsThikrFragmentTutorial.PREF_XML_FILE,R.xml.prefs_tutorial3);
                break;
            case 3:
                data.putInt(PrefsThikrFragmentTutorial.PREF_XML_FILE,R.xml.prefs_tutorial4);
                break;
            default:
                data=null;
                break;
        }
        Tutorial_description.setText((i+1)+" / "+(final_count));
        if (data != null) {
            fragment.setArguments(data);
            ft.replace(R.id.preference_container, fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }
        count++;
		if (count>final_count){
			PreferenceManager.getDefaultSharedPreferences(this.getActivity()).edit().putBoolean("isFirstLaunch", false).commit();
			this.getActivity().onBackPressed();
			//mCallback.launchFragment(new MainFragment(),null,"MainFragment");
		}
    }
	
}
