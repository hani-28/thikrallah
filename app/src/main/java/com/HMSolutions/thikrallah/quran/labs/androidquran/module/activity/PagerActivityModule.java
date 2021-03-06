package com.HMSolutions.thikrallah.quran.labs.androidquran.module.activity;

import android.content.Context;

import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.quran.labs.androidquran.data.QuranInfo;
import com.HMSolutions.thikrallah.quran.labs.androidquran.di.ActivityScope;
import com.HMSolutions.thikrallah.quran.labs.androidquran.ui.PagerActivity;
import com.HMSolutions.thikrallah.quran.labs.androidquran.ui.helpers.AyahSelectedListener;
import com.HMSolutions.thikrallah.quran.labs.androidquran.util.QuranScreenInfo;
import com.HMSolutions.thikrallah.quran.labs.androidquran.util.QuranUtils;
import com.HMSolutions.thikrallah.quran.labs.androidquran.util.TranslationUtil;

import dagger.Module;
import dagger.Provides;

@Module
public class PagerActivityModule {
  private final PagerActivity pagerActivity;

  public PagerActivityModule(PagerActivity pagerActivity) {
    this.pagerActivity = pagerActivity;
  }

  @Provides
  AyahSelectedListener provideAyahSelectedListener() {
    return this.pagerActivity;
  }

  @Provides
  @ActivityScope
  String provideImageWidth(QuranScreenInfo screenInfo) {
    return QuranUtils.isDualPages(pagerActivity, screenInfo) ?
        screenInfo.getTabletWidthParam() : screenInfo.getWidthParam();
  }

  @Provides
  @ActivityScope
  TranslationUtil provideTranslationUtil(Context context, QuranInfo quranInfo) {
    return new TranslationUtil(
        context.getResources().getColor(R.color.translation_translator_color),
        quranInfo);
  }
}
