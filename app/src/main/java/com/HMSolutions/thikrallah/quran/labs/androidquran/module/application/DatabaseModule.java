package com.HMSolutions.thikrallah.quran.labs.androidquran.module.application;

import android.content.Context;

import com.HMSolutions.thikrallah.quran.labs.androidquran.data.QuranInfo;
import com.HMSolutions.thikrallah.quran.labs.androidquran.database.BookmarksDBAdapter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DatabaseModule {

  @Provides
  @Singleton
  static BookmarksDBAdapter provideBookmarkDatabaseAdapter(Context context, QuranInfo quranInfo) {
    return new BookmarksDBAdapter(context, quranInfo.getNumberOfPages());
  }
}
