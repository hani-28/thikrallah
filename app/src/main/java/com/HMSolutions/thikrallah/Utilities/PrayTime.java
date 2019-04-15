package com.HMSolutions.thikrallah.Utilities;
//--------------------- Copyright Block ----------------------
/* 

PrayTime.java: Prayer Times Calculator (ver 1.0)
Copyright (C) 2007-2010 PrayTimes.org

Java Code By: Hussain Ali Khan
Original JS Code By: Hamid Zarrabi-Zadeh

License: GNU LGPL v3.0

TERMS OF USE:
	Permission is granted to use this code, with or 
	without modification, in any website or application 
	provided that credit is given to the original work 
	with a link back to PrayTimes.org.

This program is distributed in the hope that it will 
be useful, but WITHOUT ANY WARRANTY. 

PLEASE DO NOT REMOVE THIS COPYRIGHT BLOCK.

*/

import android.content.Context;
import android.location.Address;
//import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.Utilities.reversegeocode.GeocodeListBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PrayTime {
    private static final String TAG="PrayTime";
    // ---------------------- Global Variables --------------------
    private int calcMethod; // caculation method
    private int asrJuristic; // Juristic method for Asr
    private int dhuhrMinutes; // minutes after mid-day for Dhuhr
    private int adjustHighLats; // adjusting method for higher latitudes
    private int timeFormat; // time format
    private double lat; // latitude
    private double lng; // longitude
    private double timeZone; // time-zone
    private double JDate; // Julian date
    // ------------------------------------------------------------
    // Calculation Methods
    public static final int CALC_METHOD_Jafari=0; // Ithna Ashari
    public static final int CALC_METHOD_Karachi=1; // University of Islamic Sciences, Karachi
    public static final int CALC_METHOD_ISNA=2; // Islamic Society of North America (ISNA)
    public static final int CALC_METHOD_MWL=3; // Muslim World League (MWL)
    public static final int CALC_METHOD_Makkah=4; // Umm al-Qura, Makkah
    public static final int CALC_METHOD_Egypt=5; // Egyptian General Authority of Survey
    public static final int CALC_METHOD_Custom=7; // Custom Setting
    public static final int CALC_METHOD_Tehran=6; // Institute of Geophysics, University of Tehran
    public static final int CALC_METHOD_OMAN=8; //same as Karachi except +5 minutes for duhr/asr/maghrib
    // Juristic Methods
    public static final int JURISTIC_METHOD_Shafii=0; // Shafii (standard)
    public static final int JURISTIC_METHOD_Hanafi=1; // Hanafi
    // Adjusting Methods for Higher Latitudes
    public static final int ADJUST_HIGHER_LATITUDE_None=0; // No adjustment
    public static final int ADJUST_HIGHER_LATITUDE_MidNight=1; // middle of night
    public static final int ADJUST_HIGHER_LATITUDE_OneSeventh=2; // 1/7th of night
    public static final int ADJUST_HIGHER_LATITUDE_AngleBased=3; // angle/60th of night
    // Time Formats
    public static final int TIME_FORMAT_Time24=0; // 24-hour format
    public static final int TIME_FORMAT_Time12=1; // 12-hour format
    public static final int TIME_FORMAT_Time12NS=2; // 12-hour format with no suffix
    public static final int TIME_FORMAT_Floating=3; // floating point number
    // Time Names
    private String[] timeNames;
    private String InvalidTime; // The string used for invalid times
    // --------------------- Technical Settings --------------------
    private int numIterations; // number of iterations needed to compute times
    // ------------------- Calc Method Parameters --------------------
    private HashMap<Integer, double[]> methodParams;




    /*
     * this.methodParams[methodNum] = new Array(fa, ms, mv, is, iv);
     *
     * fa : fajr angle ms : maghrib selector (0 = angle; 1 = minutes after
     * sunset) mv : maghrib parameter value (in angle or minutes) is : isha
     * selector (0 = angle; 1 = minutes after maghrib) iv : isha parameter value
     * (in angle or minutes)
     */
    public String getInvalidTime(){
        return InvalidTime;
    }
    private double[] prayerTimesCurrent;
    private int[] offsets;

    public static PrayTime instancePrayTime(Context context){
        //Log.d("prayerTimes", "testing");
        PrayTime prayers = new PrayTime();

        prayers.setTimeFormat(PrayTime.TIME_FORMAT_Time12);
        int calc_method=PrayTime.getCalculationMethod(context);
       // Log.d(TAG,"calc_method="+calc_method);
        prayers.setCalcMethod(calc_method);
        int asr_calc_method=PrayTime.getJuristicMethod(context);
        prayers.setAsrJuristic(asr_calc_method);
        int high_latitude_adjustment=Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("adjustment_method","0"));
        prayers.setAdjustHighLats(high_latitude_adjustment);
        int[] offsets = {0, 0, 0, 0, 0, 0, 0}; // {Fajr,Sunrise,Dhuhr,Asr,Sunset,Maghrib,Isha}

        prayers.tune(offsets);
        return prayers;
    }
    public static int getCalculationMethod(Context context){
      //  Log.d(TAG,"getCalculationMethod");
        String user_option=PreferenceManager.getDefaultSharedPreferences(context).getString("calc_method",null);
        if (user_option!=null){
            Log.d(TAG,"user option is already made. It is "+Integer.parseInt(user_option));
            return Integer.parseInt(user_option);
        }

        double latitude =  Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(context).getString("latitude", "0.0"));
        double longitude = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(context).getString("longitude","0.0"));
        if(latitude==0&&longitude==0){
            return CALC_METHOD_MWL;
        }

        String countrycode = GeocodeListBuilder.getCountryId(context, latitude, longitude);
        //List<Address> addresses = new Geocoder(context, Locale.ENGLISH).getFromLocation(latitude,longitude,1);
        Log.d(TAG,"calc method: addresses received. code is"+countrycode);
        if (countrycode!=null) {
            int default_method=-100;
            //String countrycode = addresses.get(0).getCountryCode();
            Log.d(TAG,"country code is"+countrycode);
            if (countrycode.equalsIgnoreCase("CAN")) default_method= CALC_METHOD_ISNA;
            if (countrycode.equalsIgnoreCase("USA")) default_method= CALC_METHOD_ISNA;
            if (countrycode.equalsIgnoreCase("UMI")) default_method= CALC_METHOD_ISNA;
            if (countrycode.equalsIgnoreCase("BGD")) default_method= CALC_METHOD_Karachi;
            if (countrycode.equalsIgnoreCase("IND")) default_method= CALC_METHOD_Karachi;
            if (countrycode.equalsIgnoreCase("PAK")) default_method= CALC_METHOD_Karachi;
            if (countrycode.equalsIgnoreCase("AFG")) default_method= CALC_METHOD_Karachi;
            if (countrycode.equalsIgnoreCase("BHR")) default_method= CALC_METHOD_Makkah;
            if (countrycode.equalsIgnoreCase("IRQ")) default_method= CALC_METHOD_Makkah;
            if (countrycode.equalsIgnoreCase("JOR")) default_method= CALC_METHOD_Makkah;
            if (countrycode.equalsIgnoreCase("KWT")) default_method= CALC_METHOD_Makkah;
            if (countrycode.equalsIgnoreCase("OMN")) default_method= CALC_METHOD_OMAN;
            if (countrycode.equalsIgnoreCase("QAT")) default_method= CALC_METHOD_Makkah;
            if (countrycode.equalsIgnoreCase("SAU")) default_method= CALC_METHOD_Makkah;
            if (countrycode.equalsIgnoreCase("ARE")) default_method= CALC_METHOD_Makkah;
            if (countrycode.equalsIgnoreCase("YEM")) default_method= CALC_METHOD_Makkah;
            if (countrycode.equalsIgnoreCase("IRN")) default_method= CALC_METHOD_Tehran;
            if (countrycode.equalsIgnoreCase("EGY")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("DZA")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("AGO")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("BEN")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("BWA")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("BFA")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("BDI")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("CMR")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("CPV")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("CAF")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("TCD")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("COM")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("COG")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("COD")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("CIV")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("DJI")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("GNQ")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("ERI")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("ETH")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("GAB")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("GMB")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("GHA")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("GIN")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("GNB")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("ISR")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("KEN")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("LBN")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("LSO")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("LBR")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("LBY")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("MDG")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("MWI")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("MLI")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("MRT")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("MUS")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("MAR")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("MOZ")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("NAM")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("NER")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("NGA")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("PSE")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("RWA")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("STP")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("SEN")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("SYC")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("SLE")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("SOM")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("SSD")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("SDN")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("SWZ")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("SYR")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("TZA")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("TGO")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("TUN")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("UGA")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("ESH")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("ZMB")) default_method= CALC_METHOD_Egypt;
            if (countrycode.equalsIgnoreCase("ZWE")) default_method= CALC_METHOD_Egypt;



            if (default_method!=-100){
              //  Log.d(TAG,"default method returned is"+default_method);
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("calc_method",Integer.toString(default_method)).commit();
                Log.d(TAG,"calc method: committed");
                return default_method;
            }else{
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("calc_method",Integer.toString(CALC_METHOD_MWL)).commit();
                Log.d(TAG,"calc method: committed");
                return CALC_METHOD_MWL;
            }

        }

       // Log.d(TAG,"0 addresses receieved.returned");
        //PreferenceManager.getDefaultSharedPreferences(context).edit().putString("calc_method",Integer.toString(CALC_METHOD_MWL)).commit();
        return CALC_METHOD_MWL;
    }

    public static int getJuristicMethod(Context context){
      //  Log.d(TAG,"getJuristicMethod");
        String user_option=PreferenceManager.getDefaultSharedPreferences(context).getString("asr_calc_method",null);
        if (user_option!=null){
          //  Log.d(TAG,"user option is already made. It is "+Integer.parseInt(user_option));
            return Integer.parseInt(user_option);
        }

        double latitude =  Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(context).getString("latitude", "0.0"));
        double longitude = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(context).getString("longitude","0.0"));

        if(latitude==0&&longitude==0){
            return JURISTIC_METHOD_Shafii;
        }

        String countrycode = GeocodeListBuilder.getCountryId(context, latitude, longitude);


       // List<Address> addresses = new Geocoder(context, Locale.ENGLISH).getFromLocation(latitude,longitude,1);
        Log.d(TAG,"jurist method: addresses received. code is"+countrycode);
        if (countrycode !=null) {
            int default_juristic_method=-100;
         //   Log.d(TAG,"country code is"+countrycode);
            if (countrycode.equalsIgnoreCase("AFG")) default_juristic_method= JURISTIC_METHOD_Hanafi;
            if (countrycode.equalsIgnoreCase("AZE")) default_juristic_method= JURISTIC_METHOD_Hanafi;
            if (countrycode.equalsIgnoreCase("BGD")) default_juristic_method= JURISTIC_METHOD_Hanafi;
            if (countrycode.equalsIgnoreCase("EGY")) default_juristic_method= JURISTIC_METHOD_Hanafi;
            if (countrycode.equalsIgnoreCase("JOR")) default_juristic_method= JURISTIC_METHOD_Hanafi;
            if (countrycode.equalsIgnoreCase("KAZ")) default_juristic_method= JURISTIC_METHOD_Hanafi;
            if (countrycode.equalsIgnoreCase("KGZ")) default_juristic_method= JURISTIC_METHOD_Hanafi;
            if (countrycode.equalsIgnoreCase("MDV")) default_juristic_method= JURISTIC_METHOD_Hanafi;
            if (countrycode.equalsIgnoreCase("PAK")) default_juristic_method= JURISTIC_METHOD_Hanafi;
            if (countrycode.equalsIgnoreCase("SYR")) default_juristic_method= JURISTIC_METHOD_Hanafi;
            if (countrycode.equalsIgnoreCase("TJK")) default_juristic_method= JURISTIC_METHOD_Hanafi;
            if (countrycode.equalsIgnoreCase("TUR")) default_juristic_method= JURISTIC_METHOD_Hanafi;
            if (countrycode.equalsIgnoreCase("TKM")) default_juristic_method= JURISTIC_METHOD_Hanafi;
            if (countrycode.equalsIgnoreCase("UZB")) default_juristic_method= JURISTIC_METHOD_Hanafi;




            if (default_juristic_method!=-100){
                Log.d(TAG,"default juristic method returned is"+default_juristic_method);
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("asr_calc_method",Integer.toString(default_juristic_method)).commit();
                return default_juristic_method;
            }else{
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("asr_calc_method",Integer.toString(JURISTIC_METHOD_Shafii)).commit();
                return JURISTIC_METHOD_Shafii;
            }

        }

       // Log.d(TAG,"0 addresses receieved.returned");
        //PreferenceManager.getDefaultSharedPreferences(context).edit().putString("calc_method",Integer.toString(CALC_METHOD_MWL)).commit();
        return JURISTIC_METHOD_Shafii;
    }
    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;
    public int calculateDistanceInKilometer(double userLat, double userLng,
                                            double venueLat, double venueLng) {

        double latDistance = Math.toRadians(userLat - venueLat);
        double lngDistance = Math.toRadians(userLng - venueLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c));
    }
    public String[] getPrayerTimes(Context context){
        double latitude =  Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(context).getString("latitude", "0.0"));
        double longitude = Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(context).getString("longitude","0.0"));
        double timezone = getCurrentTimezoneOffset();
        int adjustment=PreferenceManager.getDefaultSharedPreferences(context).getInt("time_adjustment",0);
        offsets=new int[]{adjustment,adjustment,adjustment,adjustment,adjustment,adjustment,adjustment};
        if(getCalcMethod()==CALC_METHOD_OMAN){
            offsets[2]= offsets[2]+5;
            offsets[3]= offsets[3]+5;
            offsets[5]= offsets[5]+5;
        }
        Log.d(TAG,"detectDaylightSaving() "+detectDaylightSaving());
        //timezone=timezone+detectDaylightSaving();
        Log.d(TAG,"DLS time is "+timezone);
        Log.d(TAG,"Time zone is "+TimeZone.getDefault().getDisplayName());
      //  Log.d(TAG,"longitude is"+longitude);
        Log.d(TAG,"latitude is"+latitude);
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);

        String[] prayerTimes = this.getPrayerTimes(cal,
                latitude, longitude, timezone);

        //check for invalid isha or fajr times
        if (prayerTimes[0].equalsIgnoreCase(getInvalidTime())){
            String high_latitude_adjustment=(PreferenceManager.getDefaultSharedPreferences(context).getString("adjustment_method",null));
            if (high_latitude_adjustment == null ||high_latitude_adjustment.equalsIgnoreCase("0")) {

                PreferenceManager.getDefaultSharedPreferences(context).edit().putString("adjustment_method","3").commit();
               // Toast.makeText(context, R.string.latitude_adjustment_method_chosen,Toast.LENGTH_LONG).show();
                this.setAdjustHighLats(3);
                prayerTimes = this.getPrayerTimes(cal,
                        latitude, longitude, timezone);
            }


        }
        return prayerTimes;
    }
    public PrayTime() {
        // Initialize vars

        this.setCalcMethod(0);
        this.setAsrJuristic(0);
        this.setDhuhrMinutes(0);
        this.setAdjustHighLats(1);
        this.setTimeFormat(0);

        // Time Names
        timeNames = new String[7];
        timeNames[0]=("Fajr");
        timeNames[1]=("Sunrise");
        timeNames[2]=("Dhuhr");
        timeNames[3]=("Asr");
        timeNames[4]=("Sunset");
        timeNames[5]=("Maghrib");
        timeNames[6]=("Isha");

        InvalidTime = "-----"; // The string used for invalid times

        // --------------------- Technical Settings --------------------

        this.setNumIterations(1); // number of iterations needed to compute
        // times

        // ------------------- Calc Method Parameters --------------------

        // Tuning offsets {fajr, sunrise, dhuhr, asr, sunset, maghrib, isha}
        offsets = new int[7];
        offsets[0] = 0;
        offsets[1] = 0;
        offsets[2] = 0;
        offsets[3] = 0;
        offsets[4] = 0;
        offsets[5] = 0;
        offsets[6] = 0;

        /*
         *
         * fa : fajr angle ms : maghrib selector (0 = angle; 1 = minutes after
         * sunset) mv : maghrib parameter value (in angle or minutes) is : isha
         * selector (0 = angle; 1 = minutes after maghrib) iv : isha parameter
         * value (in angle or minutes)
         */
        methodParams = new HashMap<Integer, double[]>();

        // Jafari
        double[] Jvalues = {16,0,4,0,14};
        methodParams.put(Integer.valueOf(this.getJafari()), Jvalues);

        // Karachi
        double[] Kvalues = {18,1,0,0,18};
        methodParams.put(Integer.valueOf(this.getKarachi()), Kvalues);

        // Karachi
        double[] Ovalues = {18,1,0,0,18};
        methodParams.put(Integer.valueOf(this.getOman()), Ovalues);

        // ISNA
        double[] Ivalues = {15,1,0,0,15};
        methodParams.put(Integer.valueOf(this.getISNA()), Ivalues);

        // MWL
        double[] MWvalues = {18,1,0,0,17};
        methodParams.put(Integer.valueOf(this.getMWL()), MWvalues);

        // Makkah
        double[] MKvalues = {18.5,1,0,1,90};
        methodParams.put(Integer.valueOf(this.getMakkah()), MKvalues);

        // Egypt
        double[] Evalues = {19.5,1,0,0,17.5};
        methodParams.put(Integer.valueOf(this.getEgypt()), Evalues);

        // Tehran
        double[] Tvalues = {17.7,0,4.5,0,14};
        methodParams.put(Integer.valueOf(this.getTehran()), Tvalues);

        // Custom
        double[] Cvalues = {18,1,0,0,17};
        methodParams.put(Integer.valueOf(this.getCustom()), Cvalues);

    }

    // ---------------------- Trigonometric Functions -----------------------
    // range reduce angle in degrees.
    private double fixangle(double a) {

        a = a - (360 * (Math.floor(a / 360.0)));

        a = a < 0 ? (a + 360) : a;

        return a;
    }

    // range reduce hours to 0..23
    private double fixhour(double a) {
        a = a - 24.0 * Math.floor(a / 24.0);
        a = a < 0 ? (a + 24) : a;
        return a;
    }

    // radian to degree
    private double radiansToDegrees(double alpha) {
        return ((alpha * 180.0) / Math.PI);
    }

    // deree to radian
    private double DegreesToRadians(double alpha) {
        return ((alpha * Math.PI) / 180.0);
    }

    // degree sin
    private double dsin(double d) {
        return (Math.sin(DegreesToRadians(d)));
    }

    // degree cos
    private double dcos(double d) {
        return (Math.cos(DegreesToRadians(d)));
    }

    // degree tan
    private double dtan(double d) {
        return (Math.tan(DegreesToRadians(d)));
    }

    // degree arcsin
    private double darcsin(double x) {
        double val = Math.asin(x);
        return radiansToDegrees(val);
    }

    // degree arccos
    private double darccos(double x) {
        double val = Math.acos(x);
        return radiansToDegrees(val);
    }

    // degree arctan
    private double darctan(double x) {
        double val = Math.atan(x);
        return radiansToDegrees(val);
    }

    // degree arctan2
    private double darctan2(double y, double x) {
        double val = Math.atan2(y, x);
        return radiansToDegrees(val);
    }

    // degree arccot
    private double darccot(double x) {
        double val = Math.atan2(1.0, x);
        return radiansToDegrees(val);
    }

    // ---------------------- Time-Zone Functions -----------------------
    // compute local time-zone for a specific date
    private double getTimeZone1() {
        TimeZone timez =TimeZone.getDefault();
        double hoursDiff = (timez.getRawOffset() / 1000.0) / 3600;
        return hoursDiff;
    }

    // compute base time-zone of the system
    private double getBaseTimeZone() {
        TimeZone timez = TimeZone.getDefault();
        double hoursDiff = (timez.getRawOffset() / 1000.0) / 3600;
        return hoursDiff;

    }

    // detect daylight saving in a given date
    private double detectDaylightSaving() {
        TimeZone timez = TimeZone.getDefault();
        double hoursDiff = timez.getDSTSavings();
        return hoursDiff;
    }

    // ---------------------- Julian Date Functions -----------------------
    // calculate julian date from a calendar date
    private double julianDate(int year, int month, int day) {

        if (month <= 2) {
            year -= 1;
            month += 12;
        }
        double A = Math.floor(year / 100.0);

        double B = 2 - A + Math.floor(A / 4.0);

        double JD = Math.floor(365.25 * (year + 4716))
                + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;

        return JD;
    }

    // convert a calendar date to julian date (second method)
    private double calcJD(int year, int month, int day) {
        double J1970 = 2440588.0;
        Date date = new Date(year, month - 1, day);

        double ms = date.getTime(); // # of milliseconds since midnight Jan 1,
        // 1970
        double days = Math.floor(ms / (1000.0 * 60.0 * 60.0 * 24.0));
        return J1970 + days - 0.5;

    }

    // ---------------------- Calculation Functions -----------------------
    // References:
    // http://www.ummah.net/astronomy/saltime
    // http://aa.usno.navy.mil/faq/docs/SunApprox.html
    // compute declination angle of sun and equation of time
    private double[] sunPosition(double jd) {

        double D = jd - 2451545;
        double g = fixangle(357.529 + 0.98560028 * D);
        double q = fixangle(280.459 + 0.98564736 * D);
        double L = fixangle(q + (1.915 * dsin(g)) + (0.020 * dsin(2 * g)));

        // double R = 1.00014 - 0.01671 * [self dcos:g] - 0.00014 * [self dcos:
        // (2*g)];
        double e = 23.439 - (0.00000036 * D);
        double d = darcsin(dsin(e) * dsin(L));
        double RA = (darctan2((dcos(e) * dsin(L)), (dcos(L))))/ 15.0;
        RA = fixhour(RA);
        double EqT = q/15.0 - RA;
        double[] sPosition = new double[2];
        sPosition[0] = d;
        sPosition[1] = EqT;

        return sPosition;
    }

    // compute equation of time
    private double equationOfTime(double jd) {
        double eq = sunPosition(jd)[1];
        return eq;
    }

    // compute declination angle of sun
    private double sunDeclination(double jd) {
        double d = sunPosition(jd)[0];
        return d;
    }

    // compute mid-day (Dhuhr, Zawal) time
    private double computeMidDay(double t) {
        double T = equationOfTime(this.getJDate() + t);
        double Z = fixhour(12 - T);
        return Z;
    }

    // compute time for a given angle G
    private double computeTime(double G, double t) {

        double D = sunDeclination(this.getJDate() + t);
        double Z = computeMidDay(t);
        double Beg = -dsin(G) - dsin(D) * dsin(this.getLat());
        double Mid = dcos(D) * dcos(this.getLat());
        double V = darccos(Beg/Mid)/15.0;

        return Z + (G > 90 ? -V : V);
    }

    // compute the time of Asr
    // Shafii: step=1, Hanafi: step=2
    private double computeAsr(double step, double t) {
        double D = sunDeclination(this.getJDate() + t);
        double G = -darccot(step + dtan(Math.abs(this.getLat() - D)));
        return computeTime(G, t);
    }

    // ---------------------- Misc Functions -----------------------
    // compute the difference between two times
    private double timeDiff(double time1, double time2) {
        return fixhour(time2 - time1);
    }

    // -------------------- Interface Functions --------------------
    // return prayer times for a given date
    private String[] getDatePrayerTimes(int year, int month, int day,
                                                 double latitude, double longitude, double tZone) {
        this.setLat(latitude);
        this.setLng(longitude);
        this.setTimeZone(tZone);
        this.setJDate(julianDate(year, month, day));
        double lonDiff = longitude / (15.0 * 24.0);
        this.setJDate(this.getJDate() - lonDiff);
        return computeDayTimes();
    }

    // return prayer times for a given date
    public String[] getPrayerTimes(Calendar date, double latitude,
                                            double longitude, double tZone) {

        int year = date.get(Calendar.YEAR);
        int month = date.get(Calendar.MONTH);
        int day = date.get(Calendar.DATE);

         return getDatePrayerTimes(year, month + 1, day, latitude, longitude, tZone);
    }

    // set custom values for calculation parameters
    private void setCustomParams(double[] params) {

        for (int i = 0; i < 5; i++) {
            if (params[i] == -1) {
                params[i] = methodParams.get(this.getCalcMethod())[i];
                methodParams.put(this.getCustom(), params);
            } else {
                methodParams.get(this.getCustom())[i] = params[i];
            }
        }
        this.setCalcMethod(this.getCustom());
    }

    // set the angle for calculating Fajr
    public void setFajrAngle(double angle) {
        double[] params = {angle, -1, -1, -1, -1};
        setCustomParams(params);
    }

    // set the angle for calculating Maghrib
    public void setMaghribAngle(double angle) {
        double[] params = {-1, 0, angle, -1, -1};
        setCustomParams(params);

    }

    // set the angle for calculating Isha
    public void setIshaAngle(double angle) {
        double[] params = {-1, -1, -1, 0, angle};
        setCustomParams(params);

    }

    // set the minutes after Sunset for calculating Maghrib
    public void setMaghribMinutes(double minutes) {
        double[] params = {-1, 1, minutes, -1, -1};
        setCustomParams(params);

    }

    // set the minutes after Maghrib for calculating Isha
    public void setIshaMinutes(double minutes) {
        double[] params = {-1, -1, -1, 1, minutes};
        setCustomParams(params);

    }

    // convert double hours to 24h format
    public String floatToTime24(double time) {

        String result;

        if (Double.isNaN(time)) {
            return InvalidTime;
        }

        time = fixhour(time + 0.5 / 60.0); // add 0.5 minutes to round
        int hours = (int)Math.floor(time);
        double minutes = Math.floor((time - hours) * 60.0);

        if ((hours >= 0 && hours <= 9) && (minutes >= 0 && minutes <= 9)) {
            result = "0" + hours + ":0" + Math.round(minutes);
        } else if ((hours >= 0 && hours <= 9)) {
            result = "0" + hours + ":" + Math.round(minutes);
        } else if ((minutes >= 0 && minutes <= 9)) {
            result = hours + ":0" + Math.round(minutes);
        } else {
            result = hours + ":" + Math.round(minutes);
        }
        return result;
    }

    // convert double hours to 12h format
    public String floatToTime12(double time, boolean noSuffix) {

        if (Double.isNaN(time)) {
            return InvalidTime;
        }

        time = fixhour(time + 0.5 / 60); // add 0.5 minutes to round
        int hours = (int)Math.floor(time);
        double minutes = Math.floor((time - hours) * 60);
        String suffix, result;
        if (hours >= 12) {
            suffix = "pm";
        } else {
            suffix = "am";
        }
        hours = ((((hours+ 12) -1) % (12))+ 1);
        /*hours = (hours + 12) - 1;
        int hrs = (int) hours % 12;
        hrs += 1;*/
        if (noSuffix == false) {
            if ((hours >= 0 && hours <= 9) && (minutes >= 0 && minutes <= 9)) {
                result = "0" + hours + ":0" + Math.round(minutes) + " "
                        + suffix;
            } else if ((hours >= 0 && hours <= 9)) {
                result = "0" + hours + ":" + Math.round(minutes) + " " + suffix;
            } else if ((minutes >= 0 && minutes <= 9)) {
                result = hours + ":0" + Math.round(minutes) + " " + suffix;
            } else {
                result = hours + ":" + Math.round(minutes) + " " + suffix;
            }

        } else {
            if ((hours >= 0 && hours <= 9) && (minutes >= 0 && minutes <= 9)) {
                result = "0" + hours + ":0" + Math.round(minutes);
            } else if ((hours >= 0 && hours <= 9)) {
                result = "0" + hours + ":" + Math.round(minutes);
            } else if ((minutes >= 0 && minutes <= 9)) {
                result = hours + ":0" + Math.round(minutes);
            } else {
                result = hours + ":" + Math.round(minutes);
            }
        }
        return result;

    }

    // convert double hours to 12h format with no suffix
    public String floatToTime12NS(double time) {
        return floatToTime12(time, true);
    }

    // ---------------------- Compute Prayer Times -----------------------
    // compute prayer times at given julian date
    private double[] computeTimes(double[] times) {

        double[] t = dayPortion(times);

        double Fajr = this.computeTime(
                180 - methodParams.get(this.getCalcMethod())[0], t[0]);

        double Sunrise = this.computeTime(180 - 0.833, t[1]);

        double Dhuhr = this.computeMidDay(t[2]);
        double Asr = this.computeAsr(1 + this.getAsrJuristic(), t[3]);
        double Sunset = this.computeTime(0.833, t[4]);

        double Maghrib = this.computeTime(
                methodParams.get(this.getCalcMethod())[2], t[5]);
        double Isha = this.computeTime(
                methodParams.get(this.getCalcMethod())[4], t[6]);

        double[] CTimes = {Fajr, Sunrise, Dhuhr, Asr, Sunset, Maghrib, Isha};

        return CTimes;

    }

    // compute prayer times at given julian date
    private String[] computeDayTimes() {
        double[] times = {5, 6, 12, 13, 18, 18, 18}; // default times

        for (int i = 1; i <= this.getNumIterations(); i++) {
            times = computeTimes(times);
        }

        times = adjustTimes(times);
        times = tuneTimes(times);

        return adjustTimesFormat(times);
    }
    private static double getCurrentTimezoneOffset() {


        TimeZone tz = TimeZone.getDefault();
        double offsetInMillis = tz.getOffset(GregorianCalendar.getInstance(tz).getTimeInMillis());

        double offset = offsetInMillis / 3600000;
      //  Log.d(TAG,"offset is"+offset);
        return offset;
    }
    // adjust times in a prayer time array
    private double[] adjustTimes(double[] times) {
        for (int i = 0; i < times.length; i++) {
            times[i] += this.getTimeZone() - this.getLng() / 15;
        }

        times[2] += this.getDhuhrMinutes() / 60; // Dhuhr
        if (methodParams.get(this.getCalcMethod())[1] == 1) // Maghrib
        {
            times[5] = times[4] + methodParams.get(this.getCalcMethod())[2]/ 60;
        }
        if (methodParams.get(this.getCalcMethod())[3] == 1) // Isha
        {
            times[6] = times[5] + methodParams.get(this.getCalcMethod())[4]/ 60;
        }

        if (this.getAdjustHighLats() != this.getNone()) {
            times = adjustHighLatTimes(times);
        }

        return times;
    }

    // convert times array to given time format
    private String[] adjustTimesFormat(double[] times) {

        String[] result = new String[7];

        if (this.getTimeFormat() == this.getFloating()) {
            for (int i=0;i<7;i++) {
                result[i]=(String.valueOf(times[i]));
            }
            return result;
        }

        for (int i = 0; i < 7; i++) {
            if (this.getTimeFormat() == this.getTime12()) {
                result[i]=(floatToTime12(times[i], false));
            } else if (this.getTimeFormat() == this.getTime12NS()) {
                result[i]=(floatToTime12(times[i], true));
            } else {
                result[i]=(floatToTime24(times[i]));
            }
        }
        return result;
    }

    // adjust Fajr, Isha and Maghrib for locations in higher latitudes
    private double[] adjustHighLatTimes(double[] times) {
        double nightTime = timeDiff(times[4], times[1]); // sunset to sunrise

        // Adjust Fajr
        double FajrDiff = nightPortion(methodParams.get(this.getCalcMethod())[0]) * nightTime;

        if (Double.isNaN(times[0]) || timeDiff(times[0], times[1]) > FajrDiff) {
            times[0] = times[1] - FajrDiff;
        }

        // Adjust Isha
        double IshaAngle = (methodParams.get(this.getCalcMethod())[3] == 0) ? methodParams.get(this.getCalcMethod())[4] : 18;
        double IshaDiff = this.nightPortion(IshaAngle) * nightTime;
        if (Double.isNaN(times[6]) || this.timeDiff(times[4], times[6]) > IshaDiff) {
            times[6] = times[4] + IshaDiff;
        }

        // Adjust Maghrib
        double MaghribAngle = (methodParams.get(this.getCalcMethod())[1] == 0) ? methodParams.get(this.getCalcMethod())[2] : 4;
        double MaghribDiff = nightPortion(MaghribAngle) * nightTime;
        if (Double.isNaN(times[5]) || this.timeDiff(times[4], times[5]) > MaghribDiff) {
            times[5] = times[4] + MaghribDiff;
        }

        return times;
    }

    // the night portion used for adjusting times in higher latitudes
    private double nightPortion(double angle) {
        double calc = 0;

        if (adjustHighLats == ADJUST_HIGHER_LATITUDE_AngleBased)
            calc = (angle)/60.0;
        else if (adjustHighLats == ADJUST_HIGHER_LATITUDE_MidNight)
            calc = 0.5;
        else if (adjustHighLats == ADJUST_HIGHER_LATITUDE_OneSeventh)
            calc = 0.14286;

        return calc;
    }

    // convert hours to day portions
    private double[] dayPortion(double[] times) {
        for (int i = 0; i < 7; i++) {
            times[i] /= 24;
        }
        return times;
    }

    // Tune timings for adjustments
    // Set time offsets
    public void tune(int[] offsetTimes) {

        for (int i = 0; i < offsetTimes.length; i++) { // offsetTimes length
            // should be 7 in order
            // of Fajr, Sunrise,
            // Dhuhr, Asr, Sunset,
            // Maghrib, Isha
            this.offsets[i] = offsetTimes[i];
        }
    }

    private double[] tuneTimes(double[] times) {
        for (int i = 0; i < times.length; i++) {
            times[i] = times[i] + this.offsets[i] / 60.0;
        }

        return times;
    }



    public int getCalcMethod() {
        return calcMethod;
    }

    public void setCalcMethod(int calcMethod) {
        this.calcMethod = calcMethod;
    }

    public int getAsrJuristic() {
        return asrJuristic;
    }

    public void setAsrJuristic(int asrJuristic) {
        this.asrJuristic = asrJuristic;
    }

    public int getDhuhrMinutes() {
        return dhuhrMinutes;
    }

    public void setDhuhrMinutes(int dhuhrMinutes) {
        this.dhuhrMinutes = dhuhrMinutes;
    }

    public int getAdjustHighLats() {
        return adjustHighLats;
    }

    public void setAdjustHighLats(int adjustHighLats) {
        this.adjustHighLats = adjustHighLats;
    }

    public int getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(int timeFormat) {
        this.timeFormat = timeFormat;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(double timeZone) {
        this.timeZone = timeZone;
    }

    public double getJDate() {
        return JDate;
    }

    public void setJDate(double jDate) {
        JDate = jDate;
    }

    private int getJafari() {
        return CALC_METHOD_Jafari;
    }



    private int getKarachi() {
        return CALC_METHOD_Karachi;
    }

    private int getOman(){
        return CALC_METHOD_OMAN;
    }


    private int getISNA() {
        return CALC_METHOD_ISNA;
    }



    private int getMWL() {
        return CALC_METHOD_MWL;
    }



    private int getMakkah() {
        return CALC_METHOD_Makkah;
    }



    private int getEgypt() {
        return CALC_METHOD_Egypt;
    }



    private int getCustom() {
        return CALC_METHOD_Custom;
    }



    private int getTehran() {
        return CALC_METHOD_Tehran;
    }



    private int getShafii() {
        return JURISTIC_METHOD_Shafii;
    }


    private int getHanafi() {
        return JURISTIC_METHOD_Hanafi;
    }



    private int getNone() {
        return ADJUST_HIGHER_LATITUDE_None;
    }



    private int getMidNight() {
        return ADJUST_HIGHER_LATITUDE_MidNight;
    }



    private int getOneSeventh() {
        return ADJUST_HIGHER_LATITUDE_OneSeventh;
    }



    private int getAngleBased() {
        return ADJUST_HIGHER_LATITUDE_AngleBased;
    }



    private int getTime24() {
        return TIME_FORMAT_Time24;
    }



    private int getTime12() {
        return TIME_FORMAT_Time12;
    }



    private int getTime12NS() {
        return TIME_FORMAT_Time12NS;
    }



    private int getFloating() {
        return TIME_FORMAT_Floating;
    }



    private int getNumIterations() {
        return numIterations;
    }

    private void setNumIterations(int numIterations) {
        this.numIterations = numIterations;
    }

    public String[] getTimeNames() {
        return timeNames;
    }
}