package com.HMSolutions.thikrallah.quran.labs.androidquran.common

import com.HMSolutions.thikrallah.quran.labs.androidquran.data.SuraAyah

data class TranslationMetadata(val sura: Int,
                               val ayah: Int,
                               val text: CharSequence,
                               val link: SuraAyah? = null)
