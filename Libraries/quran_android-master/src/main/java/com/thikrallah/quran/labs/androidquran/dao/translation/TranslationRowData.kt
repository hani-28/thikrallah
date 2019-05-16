package com.thikrallah.quran.labs.androidquran.dao.translation

interface TranslationRowData {
  fun isSeparator(): Boolean
  fun name(): String
  fun needsUpgrade(): Boolean
}
