package com.thikrallah.quran.labs.androidquran.dao.audio

import com.thikrallah.quran.labs.androidquran.data.SuraAyah

data class AudioPlaybackInfo(val currentAyah: SuraAyah,
                             val timesPlayed: Int = 1,
                             val rangePlayedTimes: Int = 1,
                             val shouldPlayBasmallah: Boolean = false)