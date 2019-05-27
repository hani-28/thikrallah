package com.HMSolutions.thikrallah.quran.labs.androidquran.component.fragment;

import com.HMSolutions.thikrallah.quran.labs.androidquran.di.QuranPageScope;
import com.HMSolutions.thikrallah.quran.labs.androidquran.module.fragment.QuranPageModule;
import com.HMSolutions.thikrallah.quran.labs.androidquran.ui.fragment.QuranPageFragment;
import com.HMSolutions.thikrallah.quran.labs.androidquran.ui.fragment.TabletFragment;
import com.HMSolutions.thikrallah.quran.labs.androidquran.ui.fragment.TranslationFragment;

import dagger.Subcomponent;

@QuranPageScope
@Subcomponent(modules = QuranPageModule.class)
public interface QuranPageComponent {
  void inject(QuranPageFragment quranPageFragment);
  void inject(TabletFragment tabletFragment);
  void inject(TranslationFragment translationFragment);

  @Subcomponent.Builder interface Builder {
    Builder withQuranPageModule(QuranPageModule quranPageModule);
    QuranPageComponent build();
  }
}
