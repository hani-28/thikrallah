package com.HMSolutions.thikrallah.Models;

import android.util.Log;

/**
 * Created by hani on 3/26/16.
 */
public class UserThikr {
    String ThikrText="";
    boolean isEnabled=true;
    boolean isBuiltIn;



    long id=-1;

    public UserThikr(Long _id,String thikrText, boolean isEnabled,boolean isBuiltin,String file) {
        ThikrText = thikrText;
        this.isEnabled = isEnabled;
        this.isBuiltIn=isBuiltin;
        Log.d("testing123","isEnabled: "+isEnabled);
        this.id=_id;
        this.file=file;
    }

    public String getThikrText() {
        return ThikrText;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isBuiltIn() {
        return isBuiltIn;
    }
    public String getFile() {
        return file;
    }

    String file;

    public long getId() {
        return id;
    }
    public String toString(){
        return "thikr text="+this.getThikrText()+ "file= "+this.getFile()+"id="+this.getId()+"isenabled="+this.isEnabled()+"isbuiltin"+this.isBuiltIn();
    }
}
