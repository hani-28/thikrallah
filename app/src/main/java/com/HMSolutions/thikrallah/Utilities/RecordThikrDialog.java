package com.HMSolutions.thikrallah.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.MediaBrowser;
import com.HMSolutions.thikrallah.PreferenceActivity;
import com.HMSolutions.thikrallah.R;

import java.io.File;
import java.util.Random;

/**
 * Created by hani on 5/14/16.
 */
public class RecordThikrDialog extends DialogFragment {
    private EditText thikr_text_edittext_view;
    private Context context;
    private Button record_button;
    private String TAG="RecordThikrDialog";
    private int file_id=100;
    private Button browse;
    private String file="";
    private MainInterface mCallback;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (MainInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MainInterface");
        }

    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        context=this.getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater= (LayoutInflater)this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.add_thikr, null);
        builder.setView(view);
        final MyThikrDialogInterface my_interface = (MyThikrDialogInterface)getTargetFragment();
        thikr_text_edittext_view=(EditText)view.findViewById(R.id.thikr_text);
        record_button=(Button)view.findViewById(R.id.record);
        record_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordAudio();
            }
        });
        builder.setMessage(R.string.add_thikr)
                .setPositiveButton(R.string.add_thikr, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String newThikr = thikr_text_edittext_view.getText().toString();

                        if (newThikr.equalsIgnoreCase("")==false){
                            mCallback.resetPlayer(MainActivity.DATA_TYPE_GENERAL_THIKR);
                            MyDBHelper db = new MyDBHelper(context);
                            db.addThikr(newThikr,0, file);
                            Log.d(TAG,"filename is "+context.getFilesDir().getPath() + File.separator  +"user"+file_id+".mp3");
                            my_interface.updateList();
                        }else{
                            Toast.makeText(context,R.string.thikr_text_required,Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mCallback.resetPlayer(MainActivity.DATA_TYPE_GENERAL_THIKR);
                        RecordThikrDialog.this.getDialog().cancel();
                    }
                });
        browse = (Button) view.findViewById(R.id.select_audio_file);
        browse.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MediaBrowser.class);
                startActivityForResult(intent, 0);
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }
    public void recordAudio() {
        file_id=new Random().nextInt(100000);
        String fileName="user"+file_id;
        final MediaRecorder recorder = new MediaRecorder();
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.MediaColumns.TITLE, fileName);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(context.getFilesDir().getPath()  +File.separator+fileName+".mp3");
        Log.d(TAG,context.getFilesDir().getPath() + File.separator  +fileName+".mp3");
        try {
            recorder.prepare();
        } catch (Exception e){
            e.printStackTrace();
        }

        final ProgressDialog mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle(R.string.recording);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mProgressDialog.setButton(this.getResources().getString(R.string.stop_recording), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                file=context.getFilesDir().getPath()  +File.separator+"user"+file_id+".mp3";
                mProgressDialog.dismiss();
                recorder.stop();
                recorder.release();
                mCallback.play(file);



            }
        });

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            public void onCancel(DialogInterface p1) {
                recorder.stop();
                recorder.release();
            }
        });
        recorder.start();

        mProgressDialog.show();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                file = data.getExtras().getString("FILE");
                mCallback.play(file);
                Log.d(TAG,"returned results: "+ file);
            }
        }
    }

}