package com.quran.labs.androidquran.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class TranslationsDBHelper extends SQLiteOpenHelper {

  private static final String DB_NAME = "translations.db";
  private static final int DB_VERSION = 4;
  private static final String CREATE_TRANSLATIONS_TABLE =
      "CREATE TABLE " + TranslationsTable.TABLE_NAME + "(" +
          TranslationsTable.ID + " integer primary key, " +
          TranslationsTable.NAME + " varchar not null, " +
          TranslationsTable.TRANSLATOR + " varchar, " +
          TranslationsTable.TRANSLATOR_FOREIGN + " varchar, " +
          TranslationsTable.FILENAME + " varchar not null, " +
          TranslationsTable.URL + " varchar, " +
          TranslationsTable.LANGUAGE_CODE + " varchar, " +
          TranslationsTable.VERSION + " integer not null default 0," +
          TranslationsTable.MINIMUM_REQUIRED_VERSION + " integer not null default 0);";

  @Inject
  TranslationsDBHelper(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(CREATE_TRANSLATIONS_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion < 4) {
      // a new column is added and columns are re-arranged
      final String BACKUP_TABLE = TranslationsTable.TABLE_NAME + "_backup";
      db.beginTransaction();
      try {
        db.execSQL("ALTER TABLE " + TranslationsTable.TABLE_NAME + " RENAME TO " + BACKUP_TABLE);
        db.execSQL(CREATE_TRANSLATIONS_TABLE);
        db.execSQL("INSERT INTO " + TranslationsTable.TABLE_NAME + " (" +
            TranslationsTable.ID + ", " +
            TranslationsTable.NAME + ", " +
            TranslationsTable.TRANSLATOR + ", " +
            (oldVersion < 2 ? "" : (TranslationsTable.TRANSLATOR_FOREIGN + ", ")) +
            TranslationsTable.FILENAME + ", " +
            TranslationsTable.URL + ", " +
            (oldVersion < 3 ? "" : (TranslationsTable.LANGUAGE_CODE + ",")) +
            TranslationsTable.VERSION + ") " +
            "SELECT " + TranslationsTable.ID + ", " +
            TranslationsTable.NAME + ", " +
            TranslationsTable.TRANSLATOR + ", " +
            (oldVersion < 2 ? "" : "translator_foreign, ") +
            TranslationsTable.FILENAME + ", " +
            TranslationsTable.URL + ", " +
            (oldVersion < 3 ? "" : (TranslationsTable.LANGUAGE_CODE + ",")) +
            TranslationsTable.VERSION +
            " FROM " + BACKUP_TABLE);
        db.execSQL("DROP TABLE " + BACKUP_TABLE);
        db.execSQL("UPDATE " + TranslationsTable.TABLE_NAME + " SET " +
            TranslationsTable.MINIMUM_REQUIRED_VERSION + " = 2");
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
    }
  }

  static class TranslationsTable {
    static final String TABLE_NAME = "translations";
    static final String ID = "id";
    static final String NAME = "name";
    static final String TRANSLATOR = "translator";
    static final String TRANSLATOR_FOREIGN = "translatorForeign";
    static final String FILENAME = "filename";
    static final String URL = "url";
    static final String LANGUAGE_CODE = "languageCode";
    static final String VERSION = "version";
    static final String MINIMUM_REQUIRED_VERSION = "minimumRequiredVersion";
  }
}
