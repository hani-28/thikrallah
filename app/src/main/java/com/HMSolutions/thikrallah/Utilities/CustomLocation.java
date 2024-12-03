package com.HMSolutions.thikrallah.Utilities;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.HMSolutions.thikrallah.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

public class CustomLocation extends Dialog implements
        View.OnClickListener, AdapterView.OnItemSelectedListener {

    public Activity c;
    public Dialog d;
    public Button yes, no;
    private Spinner countriesInput;
    private Spinner citiesInput;
    private ArrayList<String> countries;
    private ArrayList<String> cities;
    private ArrayAdapter citiesArrayAdapter;
    private ArrayAdapter countriesArrayAdapter;

    public CustomLocation(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_location_dialog);
        countriesInput = (Spinner) findViewById(R.id.countries_spinner);
        citiesInput = (Spinner) findViewById(R.id.cities_spinner);
        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

        CitiesCoordinatesDbOpenHelper db = CitiesCoordinatesDbOpenHelper.getInstance(this.getContext());
        countries = db.getUniqueCountries();
        countriesArrayAdapter = new ArrayAdapter(this.getContext(),android.R.layout.simple_spinner_item, countries);
        countriesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countriesInput.setAdapter(countriesArrayAdapter);
        countriesArrayAdapter.notifyDataSetChanged();

        cities= new ArrayList<String>();
        citiesArrayAdapter = new ArrayAdapter(this.getContext(),android.R.layout.simple_spinner_item, cities);
        citiesArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citiesInput.setAdapter(citiesArrayAdapter);
        citiesArrayAdapter.notifyDataSetChanged();


        countriesInput.setOnItemSelectedListener(this);
        ArrayList<String> cities = db.getCities(countries.get(countries.size()-4));


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_yes) {
            String country = countriesInput.getSelectedItem().toString();
            String city = citiesInput.getSelectedItem().toString();
            double[] coordinates = CitiesCoordinatesDbOpenHelper.getInstance(this.getContext()).getCityCoordinates(country, city);
            double longitude = coordinates[0];
            double latitude = coordinates[1];
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit();
            editor.putString("c_latitude", Double.toString(latitude));
            editor.putString("c_longitude", Double.toString(longitude));
            editor.putString("city", city);
            editor.putString("country", country);
            editor.putBoolean("isCustomLocation", true);
            editor.commit();
            dismiss();
        } else if (id == R.id.btn_no) {
            //boolean isCustomLocationSet = PreferenceManager.getDefaultSharedPreferences(this.getContext()).getBoolean("isCustomLocation", false);
            //PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit().putBoolean("isCustomLocation", false).commit();
            //PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit().putBoolean("isCustomLocation", isCustomLocationSet).commit();
            dismiss();
        }
        dismiss();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        CitiesCoordinatesDbOpenHelper db = CitiesCoordinatesDbOpenHelper.getInstance(this.getContext());
        cities.clear();
        cities.addAll(db.getCities(countries.get(i)));
        citiesArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}