package com.HMSolutions.thikrallah.hisnulmuslim;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.HMSolutions.thikrallah.R;

public class PreferencesActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preferences );

        toolbar = (Toolbar) findViewById(R.id.my_action_bar);
        View mToolbarShadow = findViewById(R.id.view_toolbar_shadow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.content_view, new SettingsFragment())
                .commit();

        if (Build.VERSION.SDK_INT >= 21) {
            mToolbarShadow.setVisibility(View.GONE);
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle paramBundle) {
            super.onCreate(paramBundle);
            addPreferencesFromResource(R.xml.preferences_hisn_almuslim);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return false;
    }
}