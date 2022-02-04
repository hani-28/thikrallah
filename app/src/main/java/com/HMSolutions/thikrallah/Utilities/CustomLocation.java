package com.HMSolutions.thikrallah.Utilities;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.HMSolutions.thikrallah.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class CustomLocation extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Button yes, no;
    private EditText latitudeInput;
    private EditText longitudeInput;

    public CustomLocation(Activity a) {
        super(a);
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_location_dialog);
        longitudeInput = (EditText) findViewById(R.id.longitude_text);
        latitudeInput = (EditText) findViewById(R.id.latitude_text);
        double longitude = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getContext()).getString("longitude", "0.0"));
        double latitude = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this.getContext()).getString("latitude", "0.0"));
        DecimalFormat df = new DecimalFormat("###.###");

        longitudeInput.setText(df.format(longitude));
        latitudeInput.setText(df.format(latitude));

        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:

                try {
                    NumberFormat nf= NumberFormat.getInstance();
                    double longitude = nf.parse(longitudeInput.getText().toString().replace(',', '.')).doubleValue();
                    double latitude = nf.parse(latitudeInput.getText().toString().replace(',', '.')).doubleValue();
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit();
                    editor.putString("c_latitude", Double.toString(latitude));
                    editor.putString("c_longitude", Double.toString(longitude));
                    editor.putBoolean("isCustomLocation", true);
                    editor.commit();

                } catch (ParseException e) {
                    //
                    Toast.makeText(this.getContext(), R.string.number_format_error,Toast.LENGTH_LONG).show();
                }
                dismiss();
                break;
            case R.id.btn_no:
                PreferenceManager.getDefaultSharedPreferences(this.getContext()).edit().putBoolean("isCustomLocation", false).commit();
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}