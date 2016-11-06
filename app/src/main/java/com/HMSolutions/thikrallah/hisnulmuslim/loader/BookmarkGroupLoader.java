package com.HMSolutions.thikrallah.hisnulmuslim.loader;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.HMSolutions.thikrallah.hisnulmuslim.database.HisnDatabaseInfo;
import com.HMSolutions.thikrallah.hisnulmuslim.model.Dua;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Khalid on 01 أغسطس.
 */
public class BookmarkGroupLoader extends AbstractQueryLoader<List<Dua>> {
    Context mcontext;
    String groupTitleLanguage;
    Locale deviceLocale;
    public BookmarkGroupLoader(Context context) {
        super(context);
        mcontext=context;
    }

    @Override
    public List<Dua> loadInBackground() {
        List<Dua> results = null;
        Cursor duaGroupCursor = null;
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
        try {
            final SQLiteDatabase database = mDbHelper.getDb();

            duaGroupCursor = database.rawQuery("SELECT _id, " +groupTitleLanguage+
                    " FROM dua_group " +
                    "WHERE _id " +
                    "IN " +
                    "(SELECT group_id " +
                    "FROM dua " +
                    "WHERE fav=?)",new String[]{"1"});

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