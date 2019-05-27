package com.HMSolutions.thikrallah.quran.labs.androidquran.ui.util;

import android.content.Context;
import android.graphics.Typeface;

import androidx.annotation.NonNull;

import com.HMSolutions.thikrallah.quran.labs.androidquran.data.QuranFileConstants;

public class TypefaceManager {
  public static final int TYPE_UTHMANI_HAFS = 1;
  public static final int TYPE_NOOR_HAYAH = 2;

  private static Typeface typeface;

  public static Typeface getUthmaniTypeface(@NonNull Context context) {
    if (typeface == null) {
      final String fontName;
      switch (QuranFileConstants.FONT_TYPE) {
        case TYPE_NOOR_HAYAH: {
          fontName = "noorehira.ttf";
          break;
        }
        case TYPE_UTHMANI_HAFS:
        default: {
          fontName = "uthmanic_hafs_ver12.otf";
        }
      }
      typeface = Typeface.createFromAsset(context.getAssets(), fontName);
    }
    return typeface;
  }
}
