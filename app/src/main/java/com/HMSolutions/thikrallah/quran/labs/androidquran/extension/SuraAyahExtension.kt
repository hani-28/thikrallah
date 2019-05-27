package com.HMSolutions.thikrallah.quran.labs.androidquran.extension

import com.HMSolutions.thikrallah.quran.labs.androidquran.data.SuraAyah

fun SuraAyah.requiresBasmallah(): Boolean {
  return ayah == 1 && sura != 1 && sura != 9
}
