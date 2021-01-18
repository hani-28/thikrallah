package com.HMSolutions.thikrallah.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.HMSolutions.thikrallah.Models.UserThikr;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.MainInterface;
import com.HMSolutions.thikrallah.Utilities.MyDBHelper;
import com.HMSolutions.thikrallah.Utilities.MyThikrDialogInterface;
import com.HMSolutions.thikrallah.Utilities.RecordThikrDialog;
import com.HMSolutions.thikrallah.Utilities.UserThikrArrayAdapter;

import java.util.ArrayList;

public class MyAthkarFragment extends Fragment implements MyThikrDialogInterface {
    private MainInterface mCallback;
    private Button addThikrButton;
    private ListView myAthkarListView;
    private MyDBHelper db;


    UserThikrArrayAdapter adapter;
    ArrayList<UserThikr> thickerArray;
    Activity context;
    RecordThikrDialog dialog;
    String TAG="MyAthkarFragment";

    public MyAthkarFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        try {
            mCallback = (MainInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement MainInterface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        this.getActivity().getActionBar().setDisplayShowHomeEnabled(true);
        this.setHasOptionsMenu(true);
        db = new MyDBHelper(this.getActivity());
        View view = inflater.inflate(R.layout.my_athkar, container,
                false);
        addThikrButton = (Button) view.findViewById(R.id.add_thikr);

        myAthkarListView = (ListView) view.findViewById(R.id.my_athkar_listview);
        thickerArray = db.getAllThikrs();
        adapter = new UserThikrArrayAdapter(getActivity(), R.layout.my_athkar_row_format, thickerArray);
        myAthkarListView.setAdapter(adapter);
        dialog = new RecordThikrDialog();
        dialog.setTargetFragment(this,0);

        addThikrButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                dialog.show(getActivity().getSupportFragmentManager(), "RecordThikrDialog");




            }

            //RecordThikrDialog dialog=new RecordThikrDialog();

                /*
                String newThikr = new_thikr_Edittext.getText().toString();
                if (newThikr.equalsIgnoreCase("")==false){
                    db.addThikr(newThikr);
                    new_thikr_Edittext.setText("");
                    adapter.clear();
                    thickerArray = db.getAllThikrs();
                    adapter.addAll(thickerArray);
                }
                hideKeyboard(context);
                */
        });


        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Respond to the action bar's Up/Home button
            this.getActivity().onBackPressed();
            return true;
        }
        return false;
    }

    private void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * This method will be invoked when the dialog is dismissed.
     */
    @Override
    public void updateList() {
        Log.d(TAG,"updatelist called");


        if (this.getView()!=null){
            hideKeyboard(this.getActivity());
            thickerArray = db.getAllThikrs();
            adapter.clear();
            adapter.addAll(thickerArray);
            this.adapter.notifyDataSetChanged();

        }


    }
}
