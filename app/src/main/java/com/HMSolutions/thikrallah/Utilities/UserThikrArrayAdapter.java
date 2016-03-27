package com.HMSolutions.thikrallah.Utilities;


import java.util.ArrayList;
import java.util.Collection;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.HMSolutions.thikrallah.Models.UserThikr;
import com.HMSolutions.thikrallah.R;

/**
 * Created by hani on 3/26/16.
 */
public class UserThikrArrayAdapter extends ArrayAdapter<UserThikr> implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    // declaring our ArrayList of items
    private ArrayList<UserThikr> objects;
    private Context context;

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<Item> objects,
    * because it is the list of objects we want to display.
    */
    public UserThikrArrayAdapter(Context context, int resource, ArrayList<UserThikr> objects) {

        super(context, resource, objects);
        this.objects=objects;
        this.context=context;
    }

    /*
     * we are overriding the getView method here - this is what defines how each
     * list item will look.
     */
    public View getView(int position, View convertView, ViewGroup parent){

        // assign the view we are converting to a local variable
        View v = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.my_athkar_row_format, null);
        }

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 *
		 * Therefore, i refers to the current Item object.
		 */
        UserThikr i = objects.get(position);

        if (i != null) {

            // This is how you obtain a reference to the TextViews.
            // These TextViews are created in the XML files we defined.

            TextView thikr = (TextView) v.findViewById(R.id.my_thikr_textview);
            CheckBox enabled = (CheckBox) v.findViewById(R.id.isThikrEnabledCheckbox);
            ImageButton delete = (ImageButton) v.findViewById(R.id.deleteButton);


            // check to see if each individual view is null.
            // if not, assign some text!
            if (thikr != null){
                thikr.setText(i.getThikrText());
            }
            if (enabled != null){
                enabled.setChecked(i.isEnabled());
                enabled.setTag(i.getId());
                enabled.setOnCheckedChangeListener(this);
            }
            if (delete != null) {
                delete.setTag(i.getId());
                delete.setOnClickListener(this);
                if(i.isBuiltIn()){
                    delete.setVisibility(View.INVISIBLE);
                }
            }

        }

        // the view must be returned to our activity
        return v;

    }
    @Override
    public void add(UserThikr object) {
     //   objects.add(object);
        super.add(object);

    }
    @Override
    public void addAll(Collection<? extends UserThikr> collection) {
       // objects.addAll(collection);
        super.addAll(collection);
    }
    @Override
    public void clear() {
       // objects.clear();
        super.clear();
    }

    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param buttonView The compound button view whose state has changed.
     * @param isChecked  The new checked state of buttonView.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        MyDBHelper db=new MyDBHelper(this.getContext());
        if(db.getAllEnabledThikrs().size()>1|isChecked==true){
            db.updateIsEnabled((long) buttonView.getTag(), isChecked);
            updateData();
            this.notifyDataSetChanged();
        }else{
            Toast.makeText(this.getContext(),R.string.one_thikr_warning,Toast.LENGTH_LONG).show();
            buttonView.setChecked(true);
        }

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        //delete button clicked
        MyDBHelper db=new MyDBHelper(this.getContext());
        db.deleteThikr((Long) v.getTag());
        updateData();
        this.notifyDataSetChanged();
    }

    private void updateData() {
        this.objects.clear();
        MyDBHelper db=new MyDBHelper(this.getContext());
        this.addAll(db.getAllThikrs());
    }
}
