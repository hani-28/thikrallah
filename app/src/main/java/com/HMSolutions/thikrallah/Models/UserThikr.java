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

    public UserThikr(Long _id,String mythikrText, boolean isEnabled,boolean myisBuiltin,String file) {
        ThikrText = mythikrText;
        this.isEnabled = isEnabled;
        this.isBuiltIn=myisBuiltin;
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
    @Override
    public String toString(){
        return "thikr text="+this.getThikrText()+ "file= "+this.getFile()+"id="+this.getId()+"isenabled="+this.isEnabled()+"isbuiltin"+this.isBuiltIn();
    }
}
