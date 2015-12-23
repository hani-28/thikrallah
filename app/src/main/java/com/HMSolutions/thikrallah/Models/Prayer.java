package com.HMSolutions.thikrallah.Models;

/**
 * Created by hani on 12/17/15.
 */
public class Prayer {
    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    String name;
    String time;
    public Prayer(String name, String time) {
        this.name = name;
        this.time = time;
    }



}
