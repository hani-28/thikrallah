package com.HMSolutions.thikrallah.Utilities;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.HMSolutions.thikrallah.MainActivity;
import com.HMSolutions.thikrallah.Models.UserThikr;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.hisnulmuslim.database.HisnDatabaseInfo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class CitiesCoordinatesDbOpenHelper extends SQLiteOpenHelper {
    private static final String TAG="ExternalDbOpenHelper";
    private static final String TABLE_NAME ="cities" ;
    //Path to the device folder with databases
    public static String DB_PATH;

    //Database file name
    public static final String DB_NAME = "cities_coordinates.sqlite3";
    public static final int DB_VERSION = 1;

    private static CitiesCoordinatesDbOpenHelper sInstance;

    public SQLiteDatabase database;
    public Context context;

    public static CitiesCoordinatesDbOpenHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CitiesCoordinatesDbOpenHelper(
                    context.getApplicationContext(), DB_NAME);
        }
        return sInstance;
    }

    public SQLiteDatabase getDb() {
        return database;
    }

    private CitiesCoordinatesDbOpenHelper(Context context, String databaseName) {
        super(context, databaseName, null, 1);
        this.context = context;
        //Write a full path to the databases of your application
        DB_PATH = context.getDatabasePath(databaseName).toString();
        Log.d("CitiesDbOpenHelper","DB_PATH: "+DB_PATH);
        openDataBase();
    }

    public CitiesCoordinatesDbOpenHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    //This piece of code will create a com.HMSolutions.thikrallah.cities_coordinates.sqlite3 if it’s not yet created
    public void createDataBase() {
        boolean dbExist = checkDataBase();

        if (!dbExist) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDataBase();
                this.getWritableDatabase().execSQL("CREATE INDEX position ON cities (latitude, longitude)");
            } catch (IOException e) {
                Log.e(this.getClass().toString(), "Copying error");
                throw new Error("Error copying com.HMSolutions.thikrallah.cities_coordinates.sqlite3!");
            }
        } else {
            Log.i(this.getClass().toString(), "Database already exists");
        }
    }

    //Performing a com.HMSolutions.thikrallah.cities_coordinates.sqlite3 existence check
    public boolean checkDataBase() {
        SQLiteDatabase checkDb = null;
        try {
            String path = DB_PATH;
            checkDb = SQLiteDatabase.openDatabase(path, null,
                    SQLiteDatabase.OPEN_READONLY);
        } catch (SQLException e) {
            Log.e(this.getClass().toString(), "Error while checking db");
        }
        //Android doesn’t like resource leaks, everything should
        // be closed
        if (checkDb != null) {
            checkDb.close();
        }
        Log.d(TAG,"checdatabase results is checkdb = "+checkDb);
        Log.d(TAG,"checdatabase return value is = "+(checkDb != null));
        return checkDb != null;
    }

    //Method for copying the com.HMSolutions.thikrallah.cities_coordinates.sqlite3
    private void copyDataBase() throws IOException {
        //Open a stream for reading from our ready-made com.HMSolutions.thikrallah.hisnulmuslim.database
        //The stream source is located in the assets
        InputStream externalDbStream = context.getAssets().open(DB_NAME);
        Log.d(TAG,"copying database from asset to database directory");
        //Path to the created empty com.HMSolutions.thikrallah.cities_coordinates.sqlite3 on your Android device
        String outFileName = DB_PATH;

        //Now create a stream for writing the com.HMSolutions.thikrallah.cities_coordinates.sqlite3 byte by byte
        OutputStream localDbStream = new FileOutputStream(outFileName);

        //Copying the com.HMSolutions.thikrallah.cities_coordinates.sqlite3
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = externalDbStream.read(buffer)) > 0) {
            localDbStream.write(buffer, 0, bytesRead);
        }
        //Don’t forget to close the streams
        localDbStream.close();
        externalDbStream.close();
        Log.d(TAG,"finished copying database from asset to database directory");
    }

    public SQLiteDatabase openDataBase() throws SQLException {
        String path = DB_PATH;
        if (database == null) {
            createDataBase();
            database = SQLiteDatabase.openDatabase(path, null,
                    SQLiteDatabase.OPEN_READWRITE);
        }
        return database;
    }

    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {}
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public ArrayList<String> getUniqueCountries(){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> countries = new ArrayList<String>();
        Cursor cursor = db.query(true,TABLE_NAME, new String[]{"country"},null, null, null, null, "country ASC", null);
        if (cursor .moveToFirst()) {

            while (cursor.isAfterLast() == false) {
                @SuppressLint("Range") String country = cursor.getString(cursor.getColumnIndex("country"));
                countries.add(country);
                cursor.moveToNext();
            }
        }
        db.close();
        return countries;
    }
    public ArrayList<String> getCities(String country){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> cities = new ArrayList<String>();
        Cursor cursor = db.query(true,TABLE_NAME, new String[]{"city"},"country=?", new String[]{country}, null, null, "city ASC", null);
        if (cursor .moveToFirst()) {

            while (cursor.isAfterLast() == false) {
                @SuppressLint("Range") String city = cursor.getString(cursor.getColumnIndex("city"));
                cities.add(city);
                cursor.moveToNext();
            }
        }
        db.close();
        return cities;
    }

    @SuppressLint("Range")
    public double[] getCityCoordinates(String country, String city) {
        SQLiteDatabase db = this.getReadableDatabase();
        double[] coordinates = new double[2];
        Cursor cursor = db.query(true,TABLE_NAME, new String[]{"longitude","latitude"},"country=? and city=?", new String[]{country, city}, null, null, null, null);
        if (cursor .moveToFirst()) {
            coordinates[0]=cursor.getDouble(cursor.getColumnIndex("longitude"));
            coordinates[1]=cursor.getDouble(cursor.getColumnIndex("latitude"));

        }
        db.close();
        return coordinates;
    }
    @SuppressLint("Range")
    public String[] getClosestLocation(){
        String[] closestLocation = new String[2];
        String latitude = MainActivity.getLatitude(context);
        String longitude = MainActivity.getLongitude(context);
        String cos_lat_2 = String.valueOf(Math.pow(cos(Double.parseDouble(latitude) * PI / 180),2));

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT city, country FROM cities ORDER BY ((?-latitude)*(?-latitude)) + ((? - longitude)*(? - longitude)*?) ASC"
                ,new String[]{latitude,latitude,longitude,longitude,cos_lat_2});
        if (cursor .moveToFirst()) {
            closestLocation[0]=cursor.getString(cursor.getColumnIndex("country"));
            closestLocation[1]=cursor.getString(cursor.getColumnIndex("city"));

        }
        db.close();
        return closestLocation;
    }
}