package com.HMSolutions.thikrallah.hisnulmuslim.loader;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.HMSolutions.thikrallah.hisnulmuslim.database.HisnDatabaseInfo;
import com.HMSolutions.thikrallah.hisnulmuslim.model.Dua;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DuaGroupLoader extends AbstractQueryLoader<List<Dua>> {
    private static final String TAG = "DuaGroupLoader";
    Context mcontext;
    public DuaGroupLoader(Context context) {
        super(context);
        mcontext=context;
    }
    public Locale deviceLocale;
    public String groupTitleLanguage;

    @Override
    public List<Dua> loadInBackground() {
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mcontext);
        String lang = mPrefs.getString("language", null);
        if (lang != null) {
            if(lang.equals("en")){
                groupTitleLanguage = HisnDatabaseInfo.DuaGroupTable.ENGLISH_TITLE;
            }else{
                groupTitleLanguage = HisnDatabaseInfo.DuaGroupTable.ARABIC_TITLE;
            }
        }else{
            deviceLocale = mcontext.getResources().getSystem().getConfiguration().locale;
            if (deviceLocale.equals(Locale.ENGLISH))
                groupTitleLanguage = HisnDatabaseInfo.DuaGroupTable.ENGLISH_TITLE;
            else
                groupTitleLanguage = HisnDatabaseInfo.DuaGroupTable.ARABIC_TITLE;
        }


        List<Dua> results = null;
        Cursor duaGroupCursor = null;
        try {
            final SQLiteDatabase database = mDbHelper.getDb();
            duaGroupCursor = database.query(HisnDatabaseInfo.DuaGroupTable.TABLE_NAME,
                    new String[]{HisnDatabaseInfo.DuaGroupTable._ID,
                            groupTitleLanguage},
                    null,
                    null,
                    null,
                    null,
                    HisnDatabaseInfo.DuaGroupTable._ID);

            if (duaGroupCursor != null && duaGroupCursor.moveToFirst()) {
                results = new ArrayList<>();
                do {
                    int dua_group_id = duaGroupCursor.getInt(0);
                    String dua_group_title = duaGroupCursor.getString(1);
                    results.add(new Dua(dua_group_id, dua_group_title));
                } while (duaGroupCursor.moveToNext());
            }
        } finally {
            if (duaGroupCursor != null) {
                duaGroupCursor.close();
            }
        }

        return results;
    }
}