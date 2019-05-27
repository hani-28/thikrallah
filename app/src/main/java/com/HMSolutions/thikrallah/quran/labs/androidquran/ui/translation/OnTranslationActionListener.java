package com.HMSolutions.thikrallah.quran.labs.androidquran.ui.translation;

import com.HMSolutions.thikrallah.quran.labs.androidquran.common.LocalTranslation;
import com.HMSolutions.thikrallah.quran.labs.androidquran.common.QuranAyahInfo;

public interface OnTranslationActionListener {
  void onTranslationAction(QuranAyahInfo ayah, LocalTranslation[] translations, int actionId);
}
