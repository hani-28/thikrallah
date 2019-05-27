package com.HMSolutions.thikrallah.quran.labs.androidquran.module.application;

import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import com.HMSolutions.thikrallah.quran.data.source.DisplaySize;
import com.HMSolutions.thikrallah.quran.data.source.PageProvider;
import com.HMSolutions.thikrallah.quran.data.source.PageSizeCalculator;
import com.HMSolutions.thikrallah.quran.labs.androidquran.util.QuranSettings;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

@Module
public class ApplicationModule {
  private final Application application;

  public ApplicationModule(Application application) {
    this.application = application;
  }

  @Provides
  Context provideApplicationContext() {
    return this.application;
  }

  @Provides
  Display provideDisplay(Context appContext) {
    final WindowManager w = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
    return w.getDefaultDisplay();
  }

  @Provides
  DisplaySize provideDisplaySize(Display display) {
    final Point point = new Point();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      display.getRealSize(point);
    } else {
      display.getSize(point);
    }
    return new DisplaySize(point.x, point.y);
  }

  @Provides
  PageSizeCalculator provideQuranPageSizeCalculator(PageProvider pageProvider,
                                                    DisplaySize displaySize) {
    return pageProvider.getPageSizeCalculator(displaySize);
  }

  @Provides
  @Singleton
  QuranSettings provideQuranSettings() {
    return QuranSettings.getInstance(application);
  }

  @Provides
  Scheduler provideMainThreadScheduler() {
    return AndroidSchedulers.mainThread();
  }

  @Provides
  File provideCacheDirectory() {
    return application.getCacheDir();
  }
}
