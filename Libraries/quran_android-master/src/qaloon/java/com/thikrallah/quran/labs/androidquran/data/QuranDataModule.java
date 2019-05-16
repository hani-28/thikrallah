package com.thikrallah.quran.labs.androidquran.data;

import com.thikrallah.quran.data.source.PageProvider;

import java.util.Map;

import dagger.Module;
import dagger.Provides;

@Module
public class QuranDataModule {

  @Provides
  static PageProvider provideQuranPageProvider(Map<String, PageProvider> providers) {
    return providers.get("qaloon");
  }
}
