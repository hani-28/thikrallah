package com.HMSolutions.thikrallah.quran.labs.androidquran.dao.translation

import com.squareup.moshi.Json

data class TranslationList(@field:Json(name = "data") val translations: List<Translation>)
