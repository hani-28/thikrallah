package com.quran.labs.androidquran.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import com.quran.labs.androidquran.common.LocalTranslation;
import com.quran.labs.androidquran.dao.translation.Translation;
import com.quran.labs.androidquran.dao.translation.TranslationItem;
import com.quran.labs.androidquran.util.QuranFileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import timber.log.Timber;

import static com.quran.labs.androidquran.database.TranslationsDBHelper.TranslationsTable;

@Singleton
public class TranslationsDBAdapter {

  private final Context context;
  private final SQLiteDatabase db;
  private final QuranFileUtils quranFileUtils;
  private volatile List<LocalTranslation> cachedTranslations;
  private long lastWriteTime = 0;

  @Inject
  public TranslationsDBAdapter(Context context,
                               TranslationsDBHelper adapter,
                               QuranFileUtils quranFileUtils) {
    this.context = context;
    this.db = adapter.getWritableDatabase();
    this.quranFileUtils = quranFileUtils;
  }

  public SparseArray<LocalTranslation> getTranslationsHash() {
    List<LocalTranslation> items = getTranslations();

    SparseArray<LocalTranslation> result = new SparseArray<>();
    for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
      LocalTranslation item = items.get(i);
      result.put(item.getId(), item);
    }
    return result;
  }

  public long getLastWriteTime() {
    return this.lastWriteTime;
  }

  @WorkerThread
  @NonNull
  public List<LocalTranslation> getTranslations() {
    // intentional, since cachedTranslations can be replaced by another thread, causing the check
    // to be true, but the cached object returned to be null (or to change).
    List<LocalTranslation> cached = cachedTranslations;
    if (cached != null && cached.size() > 0) {
      return cached;
    }

    List<LocalTranslation> items = new ArrayList<>();
    Cursor cursor = db.query(TranslationsTable.TABLE_NAME,
        null, null, null, null, null,
        TranslationsTable.ID + " ASC");
    if (cursor != null) {
      while (cursor.moveToNext()) {
        int id = cursor.getInt(0);
        String name = cursor.getString(1);
        String translator = cursor.getString(2);
        String translatorForeign = cursor.getString(3);
        String filename = cursor.getString(4);
        String url = cursor.getString(5);
        String languageCode = cursor.getString(6);
        int version = cursor.getInt(7);
        int minimumVersion = cursor.getInt(8);

        if (quranFileUtils.hasTranslation(context, filename)) {
          items.add(new LocalTranslation(id, filename, name, translator,
              translatorForeign, url, languageCode, version, minimumVersion));
        }
      }
      cursor.close();
    }
    items = Collections.unmodifiableList(items);
    if (items.size() > 0) {
      cachedTranslations = items;
    }
    return items;
  }

  public void deleteTranslationByFile(String filename) {
    db.execSQL("DELETE FROM " + TranslationsTable.TABLE_NAME + " WHERE " +
        TranslationsTable.FILENAME + " = ?", new Object[] { filename });
  }

  public boolean writeTranslationUpdates(List<TranslationItem> updates) {
    boolean result = true;
    db.beginTransaction();
    try {
      for (int i = 0, updatesSize = updates.size(); i < updatesSize; i++) {
        TranslationItem item = updates.get(i);
        if (item.exists()) {
          final Translation translation = item.getTranslation();
          ContentValues values = new ContentValues();
          values.put(TranslationsTable.ID, translation.getId());
          values.put(TranslationsTable.NAME, translation.getDisplayName());
          values.put(TranslationsTable.TRANSLATOR, translation.getTranslator());
          values.put(TranslationsTable.TRANSLATOR_FOREIGN,
              translation.getTranslatorNameLocalized());
          values.put(TranslationsTable.FILENAME, translation.getFileName());
          values.put(TranslationsTable.URL, translation.getFileUrl());
          values.put(TranslationsTable.LANGUAGE_CODE, translation.getLanguageCode());
          values.put(TranslationsTable.VERSION, item.getLocalVersion());
          values.put(TranslationsTable.MINIMUM_REQUIRED_VERSION, translation.getMinimumVersion());

          db.replace(TranslationsTable.TABLE_NAME, null, values);
        } else {
          db.delete(TranslationsTable.TABLE_NAME,
              TranslationsTable.ID + " = " + item.getTranslation().getId(), null);
        }
      }
      db.setTransactionSuccessful();

      this.lastWriteTime = System.currentTimeMillis();
      // clear the cached translations
      this.cachedTranslations = null;
    } catch (Exception e) {
      result = false;
      Timber.d(e, "error writing translation updates");
    } finally {
      db.endTransaction();
    }

    return result;
  }
}
