package com.HMSolutions.thikrallah.Utilities;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.ThikrMediaPlayerService;

import java.util.Random;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {
    private SeekBar mSeekBar;
    private TextView summaryTV;
    private int mProgress;
    private TextView volumeValue;
    private String TAG="SeekBarPreference";
    private int uncommittedProgress;

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
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mSeekBar = (SeekBar) holder.findViewById(R.id.seekbar);
        volumeValue = (TextView) holder.findViewById(R.id.volumeValue);
        mSeekBar.setMax(100);
        mSeekBar.setProgress(mProgress);
        volumeValue.setText(String.valueOf(mProgress));
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d("1thikr1", "onProgressChanged called");
        this.uncommittedProgress=progress;
        if (!fromUser)
            return;

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        setValue(uncommittedProgress);
        playRandrom();
       //playUserFile();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        setValue(restoreValue ? getPersistedInt(mProgress) : (Integer) defaultValue);
//        volumeValue.setText(restoreValue ? getPersistedInt(mProgress) : (Integer) defaultValue);
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
        //this.setSummary(value);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    public void playRandrom() {
        Bundle data=new Bundle();
        int ThikrCount = this.getContext().getResources().getStringArray(R.array.GeneralThikr).length;
        int fileNumber=new Random().nextInt(ThikrCount) + 1;
        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAY);
        data.putInt("FILE", fileNumber);
        data.putString("com.HMSolutions.thikrallah.datatype", MainActivity.DATA_TYPE_GENERAL_THIKR);
        sendActionToMediaService(data);

    }
    public void playUserFile() {


    }
    public void sendActionToMediaService(Bundle data){
        if (data!=null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.getContext().startForegroundService(new Intent(this.getContext(), ThikrMediaPlayerService.class).putExtras(data));
            } else {
                this.getContext().startService(new Intent(this.getContext(), ThikrMediaPlayerService.class).putExtras(data));
            }
        }

    }
}
