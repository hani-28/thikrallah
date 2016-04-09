package com.HMSolutions.thikrallah;


import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import com.HMSolutions.thikrallah.Fragments.PrefsFragment;

import java.util.Locale;

public class PreferenceActivity extends Activity implements TabHost.OnTabChangeListener {


    private static final String TAG ="PreferenceActivity" ;
    private TabHost mTabHost;
    public static final String TAB_PREF1 = "words";
    public static final String TAB_PREF2 = "numbers";
    private int mCurrentTab;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang=mPrefs.getString("language",null);

        if (lang!=null){
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);

        this.setContentView(R.layout.preference_tab_layout);
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        setupTabs();



        mTabHost.setOnTabChangedListener(this);
        mTabHost.setCurrentTab(mCurrentTab);
        // manually start loading stuff in the first tab
        updateTab(TAB_PREF1, R.id.pref_tab1);
		//getFragmentManager().beginTransaction().replace(android.R.id.content,new PrefsFragment()).commit();
	}
    private void setupTabs() {
        mTabHost.setup(); // you must call this before adding your tabs!
        mTabHost.addTab(newTab(TAB_PREF1, R.string.pref_tab1, R.id.pref_tab1));
        mTabHost.addTab(newTab(TAB_PREF2, R.string.pref_tab2, R.id.pref_tab2));
    }
    private TabHost.TabSpec newTab(String tag, int labelId, int tabContentId) {
        Log.d(TAG, "buildTab(): tag=" + tag);

        View indicator = LayoutInflater.from(this).inflate(R.layout.row_format, (ViewGroup) findViewById(android.R.id.tabs), false);
        ((TextView) indicator.findViewById(R.id.toptext1)).setText(labelId);

        TabHost.TabSpec tabSpec = mTabHost.newTabSpec(tag);
        tabSpec.setIndicator(indicator);
        tabSpec.setContent(tabContentId);
        return tabSpec;
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

    @Override
    public void onTabChanged(String tabId) {
        Log.d(TAG, "onTabChanged(): tabId=" + tabId);
        if (TAB_PREF1.equals(tabId)) {
            updateTab(tabId, R.id.pref_tab1);
            mCurrentTab = 0;
            return;
        }
        if (TAB_PREF2.equals(tabId)) {
            updateTab(tabId, R.id.pref_tab2);
            mCurrentTab = 1;
            return;
        }
    }
    private void updateTab(String tabId, int placeholder) {
        FragmentManager fm = getFragmentManager();
        if (fm.findFragmentByTag(tabId) == null) {
            fm.beginTransaction()
                    .replace(placeholder, new PrefsFragment(), tabId)
                    .commit();
        }
    }
}
