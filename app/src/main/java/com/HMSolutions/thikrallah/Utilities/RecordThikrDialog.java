package com.HMSolutions.thikrallah.Utilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
                recordAudio("user");
            }
        });
        builder.setMessage(R.string.add_thikr)
                .setPositiveButton(R.string.add_thikr, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String newThikr = thikr_text_edittext_view.getText().toString();
                        if (newThikr.equalsIgnoreCase("")==false){
                            MyDBHelper db = new MyDBHelper(context);
                            db.addThikr(newThikr,0,context.getExternalCacheDir() + File.separator  +"user"+file_id+".mp3");
                            Log.d(TAG,"filename is "+context.getExternalCacheDir() + File.separator  +"user"+file_id+".mp3");
                            my_interface.updateList();

                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RecordThikrDialog.this.getDialog().cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
    public void recordAudio(String fileName) {
        file_id=new Random().nextInt(100000);
        fileName=fileName+file_id;
        final MediaRecorder recorder = new MediaRecorder();
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.MediaColumns.TITLE, fileName);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(context.getExternalCacheDir()   +File.separator+fileName+".mp3");
        Log.d(TAG,context.getExternalCacheDir() + File.separator  +fileName+".mp3");
        try {
            recorder.prepare();
        } catch (Exception e){
            e.printStackTrace();
        }

        final ProgressDialog mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle("recording");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setButton("Stop recording", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mProgressDialog.dismiss();
                recorder.stop();
                recorder.release();
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

}