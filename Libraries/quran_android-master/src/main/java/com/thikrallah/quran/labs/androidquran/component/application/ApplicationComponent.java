package com.thikrallah.quran.labs.androidquran.component.application;

import com.thikrallah.quran.data.page.provider.QuranPageModule;
import com.thikrallah.quran.labs.androidquran.QuranDataActivity;
import com.thikrallah.quran.labs.androidquran.QuranForwarderActivity;
import com.thikrallah.quran.labs.androidquran.QuranImportActivity;
import com.thikrallah.quran.labs.androidquran.SearchActivity;
import com.thikrallah.quran.labs.androidquran.component.activity.PagerActivityComponent;
import com.thikrallah.quran.labs.androidquran.data.QuranDataModule;
import com.thikrallah.quran.labs.androidquran.data.QuranDataProvider;
import com.thikrallah.quran.labs.androidquran.module.application.ApplicationModule;
import com.thikrallah.quran.labs.androidquran.module.application.DatabaseModule;
import com.thikrallah.quran.common.networking.NetworkModule;
import com.thikrallah.quran.labs.androidquran.pageselect.PageSelectActivity;
import com.thikrallah.quran.labs.androidquran.service.AudioService;
import com.thikrallah.quran.labs.androidquran.service.QuranDownloadService;
import com.thikrallah.quran.labs.androidquran.ui.AudioManagerActivity;
import com.thikrallah.quran.labs.androidquran.ui.QuranActivity;
import com.thikrallah.quran.labs.androidquran.ui.TranslationManagerActivity;
import com.thikrallah.quran.labs.androidquran.ui.fragment.AddTagDialog;
import com.thikrallah.quran.labs.androidquran.ui.fragment.AyahPlaybackFragment;
import com.thikrallah.quran.labs.androidquran.ui.fragment.BookmarksFragment;
import com.thikrallah.quran.labs.androidquran.ui.fragment.JumpFragment;
import com.thikrallah.quran.labs.androidquran.ui.fragment.JuzListFragment;
import com.thikrallah.quran.labs.androidquran.ui.fragment.QuranAdvancedSettingsFragment;
import com.thikrallah.quran.labs.androidquran.ui.fragment.QuranSettingsFragment;
import com.thikrallah.quran.labs.androidquran.ui.fragment.SuraListFragment;
import com.thikrallah.quran.labs.androidquran.ui.fragment.TagBookmarkDialog;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
    ApplicationModule.class,
    DatabaseModule.class,
    NetworkModule.class,
    QuranDataModule.class,
    QuranPageModule.class } )
public interface ApplicationComponent {
  // subcomponents
  PagerActivityComponent.Builder pagerActivityComponentBuilder();

  // content provider
  void inject(QuranDataProvider quranDataProvider);

  // services
  void inject(AudioService audioService);
  void inject(QuranDownloadService quranDownloadService);

  // activities
  void inject(QuranActivity quranActivity);
  void inject(QuranDataActivity quranDataActivity);
  void inject(QuranImportActivity quranImportActivity);
  void inject(AudioManagerActivity audioManagerActivity);
  void inject(QuranForwarderActivity quranForwarderActivity);
  void inject(SearchActivity searchActivity);
  void inject(PageSelectActivity pageSelectActivity);

  // fragments
  void inject(BookmarksFragment bookmarksFragment);
  void inject(QuranSettingsFragment fragment);
  void inject(TranslationManagerActivity translationManagerActivity);
  void inject(QuranAdvancedSettingsFragment quranAdvancedSettingsFragment);
  void inject(SuraListFragment suraListFragment);
  void inject(JuzListFragment juzListFragment);
  void inject(AyahPlaybackFragment ayahPlaybackFragment);
  void inject(JumpFragment jumpFragment);

  // dialogs
  void inject(TagBookmarkDialog tagBookmarkDialog);
  void inject(AddTagDialog addTagDialog);
}
