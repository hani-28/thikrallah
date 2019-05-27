package com.HMSolutions.thikrallah.quran.labs.androidquran.dao.bookmark

import com.HMSolutions.thikrallah.quran.labs.androidquran.dao.RecentPage
import com.HMSolutions.thikrallah.quran.labs.androidquran.dao.Tag

data class BookmarkData(val tags: List<Tag> = emptyList(),
                        val bookmarks: List<Bookmark> = emptyList(),
                        val recentPages: List<RecentPage> = emptyList())
