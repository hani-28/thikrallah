package com.thikrallah.quran.labs.androidquran.data;

import android.os.Build;

import com.thikrallah.quran.labs.androidquran.database.DatabaseHandler;
import com.thikrallah.quran.labs.androidquran.ui.util.TypefaceManager;

public class QuranFileConstants {
  // server urls
  public static final int FONT_TYPE = TypefaceManager.TYPE_NOOR_HAYAH;

  // arabic database
  public static final String ARABIC_DATABASE =
      Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1 ?
          "quran.ar_naskh.db" : "quran.ar.db";
  public static final String ARABIC_SHARE_TABLE = DatabaseHandler.ARABIC_TEXT_TABLE;

  public static final boolean ARABIC_SHARE_TEXT_HAS_BASMALLAH = false;
}
