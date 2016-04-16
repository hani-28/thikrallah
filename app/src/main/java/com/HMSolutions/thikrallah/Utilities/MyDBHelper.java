package com.HMSolutions.thikrallah.Utilities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.HMSolutions.thikrallah.Models.UserThikr;
import com.HMSolutions.thikrallah.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by hani on 3/25/16.
 */
public class MyDBHelper  extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "MyAthkar.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String TABLE_NAME="myAthkar";
    private static final String ID_COLUMN="_id";
    private static final String THIKR_COLUMN="thikr";
    private static final String ENABLED_COLUMN="enabled";
    private static final String IS_BUILTIN_COLUMN="isbuiltin";
    private static final String FILE_PATH="filepath";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " ("+ID_COLUMN+" INTEGER PRIMARY KEY NOT NULL," +
                    THIKR_COLUMN + TEXT_TYPE +COMMA_SEP+
                    ENABLED_COLUMN+" INTEGER DEFAULT 1"+COMMA_SEP+
                    IS_BUILTIN_COLUMN+" INTEGER DEFAULT 0"+COMMA_SEP+
                    FILE_PATH+TEXT_TYPE+" DEFAULT 1"+
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
    private Context context;
    private ArrayList<UserThikr> backupUserThikrs;

    public MyDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
    }


    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void populateInitialThikr() {
        String[] generalThikrs = this.context.getResources().getStringArray(R.array.GeneralThikr);
        for (int i=0;i<generalThikrs.length;i++){
            this.addThikr(generalThikrs[i],1,String.valueOf(i+1));
        }
        if (backupUserThikrs != null) {
            for (int i=0;i<backupUserThikrs.size();i++){
                this.addThikr(backupUserThikrs.get(i).getThikrText(),0,"1");
            }
        }

    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("testing123","on upgrade called");
        backupUserThikrs = getAllUserThikrs(db);
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public long addThikr(String thikr){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(THIKR_COLUMN, thikr);
        values.put(ENABLED_COLUMN, true);

// Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                TABLE_NAME,
                null,
                values);
        db.close();
        return newRowId;
    }
    public long addThikr(String thikr,int isBuiltIn,String filePath){
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(THIKR_COLUMN, thikr);
        values.put(IS_BUILTIN_COLUMN,isBuiltIn);
        values.put(ENABLED_COLUMN, 1);
        values.put(FILE_PATH, filePath);

// Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                TABLE_NAME,
                null,
                values);
        db.close();
        return newRowId;
    }
    public UserThikr getThikr(long id){
        SQLiteDatabase db = this.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {THIKR_COLUMN,ID_COLUMN,ENABLED_COLUMN,IS_BUILTIN_COLUMN,FILE_PATH};
// Define 'where' part of query.
        String selection = ID_COLUMN+" LIKE ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(id) };
// Issue SQL statement.
        Cursor cursor = db.query(
                TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );
        if (cursor .moveToFirst()) {
            String thikr = cursor.getString(cursor.getColumnIndex(THIKR_COLUMN));
            boolean isEnabled = cursor.getInt(cursor.getColumnIndex(ENABLED_COLUMN))==1;
            boolean isBuiltIn = cursor.getInt(cursor.getColumnIndex(IS_BUILTIN_COLUMN)) == 1;
            id = cursor.getLong(cursor.getColumnIndex(ID_COLUMN));
            String  file=cursor.getString(cursor.getColumnIndex(FILE_PATH));
            if(isBuiltIn==true){
                thikr=this.context.getResources().getStringArray(R.array.GeneralThikr)[Integer.parseInt(file)-1];
            }
            UserThikr requested_thikr = new UserThikr(id, thikr, isEnabled, isBuiltIn, file);
            db.close();
            return requested_thikr;


        }
        db.close();
        return null;
    }
    public void deleteThikr(long id){
        SQLiteDatabase db = this.getWritableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
// Define 'where' part of query.
        String selection = ID_COLUMN+" LIKE ?";
// Specify arguments in placeholder order.
        String[] selectionArgs = { String.valueOf(id) };
// Issue SQL statement.
        db.delete(TABLE_NAME, selection, selectionArgs);
        db.close();
    }
    public ArrayList<UserThikr> getAllThikrs(){
        String thikr="";
        boolean isEnabled=true;
        boolean isBuiltIn;
        long id=-1;
        String file="";
        ArrayList<UserThikr> list=new ArrayList<UserThikr>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor  cursor = db.rawQuery("select * from " + TABLE_NAME, null);
        if (cursor .moveToFirst()) {

            while (cursor.isAfterLast() == false) {
                thikr = cursor.getString(cursor
                        .getColumnIndex(THIKR_COLUMN));
                isEnabled = cursor.getInt(cursor.getColumnIndex(ENABLED_COLUMN))==1;
                isBuiltIn = cursor.getInt(cursor.getColumnIndex(IS_BUILTIN_COLUMN)) == 1;
                id = cursor.getLong(cursor.getColumnIndex(ID_COLUMN));
                file=cursor.getString(cursor.getColumnIndex(FILE_PATH));
                if(isBuiltIn==true){
                    thikr=this.context.getResources().getStringArray(R.array.GeneralThikr)[Integer.parseInt(file)-1];
                }
                list.add(new UserThikr(id,thikr,isEnabled,isBuiltIn,file));
                cursor.moveToNext();
            }
        }
        db.close();
        return list;
    }
    public ArrayList<UserThikr> getAllBuiltinEnabledThikrs(){
        String thikr="";
        boolean isEnabled=true;
        boolean isBuiltIn;
        long id=-1;
        String file="";
        ArrayList<UserThikr> list=new ArrayList<UserThikr>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, ENABLED_COLUMN + " LIKE '%" + 1 + "%'" + " AND " + IS_BUILTIN_COLUMN + " LIKE '%" + 1 + "%'", null, null, null, null);

        if (cursor .moveToFirst()) {

            while (cursor.isAfterLast() == false) {
                thikr = cursor.getString(cursor
                        .getColumnIndex(THIKR_COLUMN));
                isEnabled = cursor.getInt(cursor.getColumnIndex(ENABLED_COLUMN))==1;
                isBuiltIn = cursor.getInt(cursor.getColumnIndex(IS_BUILTIN_COLUMN)) == 1;
                id = cursor.getLong(cursor.getColumnIndex(ID_COLUMN));
                file=cursor.getString(cursor.getColumnIndex(FILE_PATH));
                if(isBuiltIn==true){
                    thikr=this.context.getResources().getStringArray(R.array.GeneralThikr)[Integer.parseInt(file)-1];
                }
                list.add(new UserThikr(id,thikr,isEnabled,isBuiltIn,file));
                cursor.moveToNext();
            }
            Log.d("testing123","enabledThikrs count is "+list.size());
        }
        db.close();
        return list;
    }

    public ArrayList<UserThikr> getAllEnabledThikrs(){
        String thikr="";
        boolean isEnabled=true;
        boolean isBuiltIn;
        long id=-1;
        String file="";
        ArrayList<UserThikr> list=new ArrayList<UserThikr>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, ENABLED_COLUMN + " LIKE '%" + 1 + "%'", null, null, null, null);

        if (cursor .moveToFirst()) {

            while (cursor.isAfterLast() == false) {
                thikr = cursor.getString(cursor
                        .getColumnIndex(THIKR_COLUMN));

                isEnabled = cursor.getInt(cursor.getColumnIndex(ENABLED_COLUMN))==1;
                isBuiltIn = cursor.getInt(cursor.getColumnIndex(IS_BUILTIN_COLUMN)) == 1;

                id = cursor.getLong(cursor.getColumnIndex(ID_COLUMN));
                file=cursor.getString(cursor.getColumnIndex(FILE_PATH));
                if(isBuiltIn==true){
                    thikr=this.context.getResources().getStringArray(R.array.GeneralThikr)[Integer.parseInt(file)-1];
                }
                list.add(new UserThikr(id,thikr,isEnabled,isBuiltIn,file));
                cursor.moveToNext();
            }
            Log.d("testing123", "enabledThikrs count is " + list.size());
        }
        db.close();
        return list;
    }
    public ArrayList<UserThikr> getAllUserThikrs(SQLiteDatabase db){
        String thikr="";
        boolean isEnabled=true;
        boolean isBuiltIn;
        long id=-1;
        String file="";
        ArrayList<UserThikr> list=new ArrayList<UserThikr>();

        Cursor cursor = db.query(TABLE_NAME, null, IS_BUILTIN_COLUMN + " LIKE '%" + 0 + "%'", null, null, null, null);

        if (cursor .moveToFirst()) {

            while (cursor.isAfterLast() == false) {
                thikr = cursor.getString(cursor
                        .getColumnIndex(THIKR_COLUMN));
                isEnabled = cursor.getInt(cursor.getColumnIndex(ENABLED_COLUMN))==1;
                isBuiltIn = cursor.getInt(cursor.getColumnIndex(IS_BUILTIN_COLUMN)) == 1;
                id = cursor.getLong(cursor.getColumnIndex(ID_COLUMN));
                file=cursor.getString(cursor.getColumnIndex(FILE_PATH));
                list.add(new UserThikr(id,thikr,isEnabled,isBuiltIn,file));
                cursor.moveToNext();
            }
            Log.d("testing123","enabledThikrs count is "+list.size());
        }

        return list;
    }
    public ArrayList<UserThikr> getAllBuiltinThikrs(){
        String thikr="";
        boolean isEnabled=true;
        boolean isBuiltIn;
        long id=-1;
        String file="";
        ArrayList<UserThikr> list=new ArrayList<UserThikr>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, IS_BUILTIN_COLUMN + " LIKE '%" + 1 + "%'", null, null, null, null);

        if (cursor .moveToFirst()) {

            while (cursor.isAfterLast() == false) {
                thikr = cursor.getString(cursor
                        .getColumnIndex(THIKR_COLUMN));

                isEnabled = cursor.getInt(cursor.getColumnIndex(ENABLED_COLUMN))==1;
                isBuiltIn = cursor.getInt(cursor.getColumnIndex(IS_BUILTIN_COLUMN)) == 1;
                id = cursor.getLong(cursor.getColumnIndex(ID_COLUMN));
                file=cursor.getString(cursor.getColumnIndex(FILE_PATH));
                if(isBuiltIn==true){
                    thikr=this.context.getResources().getStringArray(R.array.GeneralThikr)[Integer.parseInt(file)-1];
                }
                list.add(new UserThikr(id,thikr,isEnabled,isBuiltIn,file));
                cursor.moveToNext();
            }
            Log.d("testing123", "enabledThikrs count is " + list.size());
        }
        db.close();
        return list;
    }
    public UserThikr getRandomThikr(){
        ArrayList<UserThikr> allEnabledThikrs = getAllEnabledThikrs();
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        if(allEnabledThikrs.size()!=0){
            int randomThikr = rand.nextInt(allEnabledThikrs.size());
            return allEnabledThikrs.get(randomThikr);
        }
        return null;
    }
    public void updateIsEnabled(long id,boolean enabled){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (enabled){
            values.put(ENABLED_COLUMN, 1);
        }else{
            values.put(ENABLED_COLUMN, 0);
        }

        db.update(TABLE_NAME, values, ID_COLUMN + "=" + id, null);
        db.close();
    }
}
