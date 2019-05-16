package com.thikrallah.quran.labs.androidquran.data;

import com.thikrallah.quran.labs.androidquran.database.DatabaseHandler;
import com.thikrallah.quran.labs.androidquran.ui.util.TypefaceManager;

public class QuranFileConstants {
  // server urls
  public static final int FONT_TYPE = TypefaceManager.TYPE_UTHMANI_HAFS;

  // arabic database
  public static final String ARABIC_DATABASE = "quran.ar.db";
  public static final String ARABIC_SHARE_TABLE = DatabaseHandler.ARABIC_TEXT_TABLE;
  public static final boolean ARABIC_SHARE_TEXT_HAS_BASMALLAH = true;
}
