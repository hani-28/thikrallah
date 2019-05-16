package com.thikrallah.quran.labs.androidquran.data;

import com.thikrallah.quran.data.source.PageProvider;
import com.thikrallah.quran.labs.androidquran.util.QuranSettings;

import java.util.Map;

import dagger.Module;
import dagger.Provides;

@Module
public class QuranDataModule {

  @Provides
  static PageProvider provideQuranPageProvider(Map<String, PageProvider> providers,
                                               QuranSettings quranSettings) {
    final String key = quranSettings.getPageType();
    final String fallbackType = "madani";
    if (key == null) {
      quranSettings.setPageType(fallbackType);
    }
    return providers.get(key == null ? fallbackType : key);
  }
}
