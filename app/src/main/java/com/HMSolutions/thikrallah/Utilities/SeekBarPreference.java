package com.HMSolutions.thikrallah.Utilities;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.ThikrMediaPlayerService;

import java.util.Random;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {
    private SeekBar mSeekBar;
    private TextView summaryTV;
    private int mProgress;

    public SeekBarPreference(Context context) {

        this(context, null, 0);
        Log.d("1thikr1", "SeekBarPreference called 3rd constructor");
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
        Log.d("1thikr1", "SeekBarPreference called 3rd constructor");
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.d("1thikr1", "SeekBarPreference called 3rd constructor");
        setLayoutResource(R.layout.preference_seekbar);

    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        Log.d("1thikr1", "onBindView called");
        //summaryTV=(TextView) view.findViewById(R.id.sum);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        mSeekBar.setMax(100);
        mSeekBar.setProgress(mProgress);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d("1thikr1", "onProgressChanged called");
        if (!fromUser)
            return;
       // this.summaryTV.setText(progress);
        setValue(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        playRandrom();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        setValue(restoreValue ? getPersistedInt(mProgress) : (Integer) defaultValue);
    }

    public void setValue(int value) {
        if (shouldPersist()) {
            persistInt(value);
        }

        if (value != mProgress) {
            mProgress = value;
            notifyChanged();
        }
        Log.d("1thikr1", "summary is " + value);
       // this.setSummary(value);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    public void playRandrom() {
        Bundle data=new Bundle();
        int fileNumber=new Random().nextInt(5) + 1;
        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAY);
        data.putInt("FILE", fileNumber);
        data.putString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_GENERAL_THIKR);
        sendActionToMediaService(data);

    }
    public void sendActionToMediaService(Bundle data){
        if (data!=null){
            this.getContext().startService(new Intent(this.getContext(), ThikrMediaPlayerService.class).putExtras(data));
        }

    }
}