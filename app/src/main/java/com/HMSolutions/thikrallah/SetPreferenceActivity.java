package com.HMSolutions.thikrallah;



import com.HMSolutions.thikrallah.Fragments.PrefsFragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import java.util.Locale;

public class SetPreferenceActivity extends Activity{


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Locale locale = new Locale(mPrefs.getString("language","ar"));
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		getFragmentManager().beginTransaction().replace(android.R.id.content,
				new PrefsFragment()).commit();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	    	Log.d("actionbar","onoptionselected called");
	        this.onBackPressed();
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}

}
