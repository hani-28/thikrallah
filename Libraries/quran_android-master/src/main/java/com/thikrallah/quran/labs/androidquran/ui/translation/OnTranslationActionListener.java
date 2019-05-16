package com.thikrallah.quran.labs.androidquran.ui.translation;

import com.thikrallah.quran.labs.androidquran.common.LocalTranslation;
import com.thikrallah.quran.labs.androidquran.common.QuranAyahInfo;

public interface OnTranslationActionListener {
  void onTranslationAction(QuranAyahInfo ayah, LocalTranslation[] translations, int actionId);
}
