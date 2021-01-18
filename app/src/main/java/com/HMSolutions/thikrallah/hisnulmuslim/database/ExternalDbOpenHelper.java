package com.HMSolutions.thikrallah.hisnulmuslim.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ExternalDbOpenHelper extends SQLiteOpenHelper {
    private static final String TAG="ExternalDbOpenHelper";
    //Path to the device folder with databases
    public static String DB_PATH;

    //Database file name
    public static final String DB_NAME = HisnDatabaseInfo.DB_NAME;
    public static final int DB_VERSION = HisnDatabaseInfo.DB_VERSION;

    private static ExternalDbOpenHelper sInstance;

    public SQLiteDatabase database;
    public Context context;

    public static ExternalDbOpenHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ExternalDbOpenHelper(
                    context.getApplicationContext(), DB_NAME);
        }
        return sInstance;
    }

    public SQLiteDatabase getDb() {
        return database;
    }

    private ExternalDbOpenHelper(Context context, String databaseName) {
        super(context, databaseName, null, 1);
        this.context = context;
        //Write a full path to the databases of your application
        DB_PATH = context.getDatabasePath(databaseName).toString();
        //String packageName = context.getPackageName();
        //DB_PATH = String.format("//data//data//%s//databases//%s", packageName,databaseName);

        // Log.d(TAG,"alternate DB_PATH is "+context.getDatabasePath(databaseName));
        openDataBase();
    }

    public ExternalDbOpenHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    //This piece of code will create a com.HMSolutions.thikrallah.hisnulmuslim.database if it’s not yet created
    public void createDataBase() {
        boolean dbExist = checkDataBase();

        if (!dbExist) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDataBase();
            } catch (IOException e) {
                Log.e(this.getClass().toString(), "Copying error");
                throw new Error("Error copying com.HMSolutions.thikrallah.hisnulmuslim.database!");
            }
        } else {
            Log.i(this.getClass().toString(), "Database already exists");
        }
    }

    //Performing a com.HMSolutions.thikrallah.hisnulmuslim.database existence check
    private boolean checkDataBase() {
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

    //Method for copying the com.HMSolutions.thikrallah.hisnulmuslim.database
    private void copyDataBase() throws IOException {
        //Open a stream for reading from our ready-made com.HMSolutions.thikrallah.hisnulmuslim.database
        //The stream source is located in the assets
        InputStream externalDbStream = context.getAssets().open(DB_NAME);
        Log.d(TAG,"copying database from asset to database directory");
        //Path to the created empty com.HMSolutions.thikrallah.hisnulmuslim.database on your Android device
        String outFileName = DB_PATH;

        //Now create a stream for writing the com.HMSolutions.thikrallah.hisnulmuslim.database byte by byte
        OutputStream localDbStream = new FileOutputStream(outFileName);

        //Copying the com.HMSolutions.thikrallah.hisnulmuslim.database
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
}