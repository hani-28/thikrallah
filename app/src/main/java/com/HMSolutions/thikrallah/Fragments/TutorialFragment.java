package com.HMSolutions.thikrallah.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
    public void onAttach(Context context) {
        super.onAttach(context);
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            ft = this.getChildFragmentManager()
                    .beginTransaction();
        }else{
            ft = this.getFragmentManager()
                    .beginTransaction();
        }
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
        Tutorial_description.setText((i+1)+" / "+final_count);
        if (data != null) {
            fragment.setArguments(data);
            ft.replace(R.id.preference_container, fragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commitAllowingStateLoss();
        }else{
            PreferenceManager.getDefaultSharedPreferences(this.getActivity()).edit().putBoolean("isFirstLaunch", false).commit();
            this.getActivity().onBackPressed();
            count++;
            return;
        }
        count++;
		if (count>final_count){
			PreferenceManager.getDefaultSharedPreferences(this.getActivity()).edit().putBoolean("isFirstLaunch", false).commit();
			this.getActivity().onBackPressed();
			//mCallback.launchFragment(new MainFragment(),null,"MainFragment");
		}
    }
	
}
