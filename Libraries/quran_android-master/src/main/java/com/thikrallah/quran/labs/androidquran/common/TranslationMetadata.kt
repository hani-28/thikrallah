package com.thikrallah.quran.labs.androidquran.common

import com.thikrallah.quran.labs.androidquran.data.SuraAyah

data class TranslationMetadata(val sura: Int,
                               val ayah: Int,
                               val text: CharSequence,
                               val link: SuraAyah? = null)
