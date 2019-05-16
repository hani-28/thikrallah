package com.thikrallah.quran.labs.androidquran.component.activity;

import com.thikrallah.quran.labs.androidquran.component.fragment.QuranPageComponent;
import com.thikrallah.quran.labs.androidquran.di.ActivityScope;
import com.thikrallah.quran.labs.androidquran.module.activity.PagerActivityModule;
import com.thikrallah.quran.labs.androidquran.ui.PagerActivity;
import com.thikrallah.quran.labs.androidquran.ui.fragment.AyahTranslationFragment;

import dagger.Subcomponent;

@ActivityScope
@Subcomponent(modules = PagerActivityModule.class)
public interface PagerActivityComponent {
  // subcomponents
  QuranPageComponent.Builder quranPageComponentBuilder();

  void inject(PagerActivity pagerActivity);
  void inject(AyahTranslationFragment ayahTranslationFragment);

  @Subcomponent.Builder interface Builder {
    Builder withPagerActivityModule(PagerActivityModule pagerModule);
    PagerActivityComponent build();
  }
}
