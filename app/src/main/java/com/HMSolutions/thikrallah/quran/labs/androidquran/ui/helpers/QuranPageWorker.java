package com.HMSolutions.thikrallah.quran.labs.androidquran.ui.helpers;

import android.content.Context;

import com.HMSolutions.thikrallah.quran.labs.androidquran.common.Response;
import com.HMSolutions.thikrallah.quran.labs.androidquran.di.ActivityScope;
import com.HMSolutions.thikrallah.quran.labs.androidquran.util.QuranFileUtils;
import com.HMSolutions.thikrallah.quran.labs.androidquran.util.QuranScreenInfo;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

@ActivityScope
public class QuranPageWorker {
  private static final String TAG = "QuranPageWorker";

  private final Context appContext;
  private final OkHttpClient okHttpClient;
  private final String imageWidth;
  private final QuranScreenInfo quranScreenInfo;
  private final QuranFileUtils quranFileUtils;

  @Inject
  QuranPageWorker(Context context,
                  OkHttpClient okHttpClient,
                  String imageWidth,
                  QuranScreenInfo quranScreenInfo,
                  QuranFileUtils quranFileUtils) {
    this.appContext = context;
    this.okHttpClient = okHttpClient;
    this.imageWidth = imageWidth;
    this.quranScreenInfo = quranScreenInfo;
    this.quranFileUtils = quranFileUtils;
  }

  private Response downloadImage(int pageNumber) {


    Response response = null;
    OutOfMemoryError oom = null;

    try {
      response = QuranDisplayHelper.getQuranPage(
              okHttpClient, appContext, imageWidth, pageNumber, quranFileUtils);
    } catch (OutOfMemoryError me) {
      oom = me;
    }

    if (response == null ||
        (response.getBitmap() == null &&
            response.getErrorCode() != Response.ERROR_SD_CARD_NOT_FOUND)){
      if (quranScreenInfo.isDualPageMode()) {
        String param = quranScreenInfo.getWidthParam();
        if (param.equals(imageWidth)) {
          param = quranScreenInfo.getTabletWidthParam();
        }
        response = QuranDisplayHelper.getQuranPage(
                okHttpClient, appContext, param, pageNumber, quranFileUtils);
        if (response.getBitmap() == null) {
          }
      }

    }

    if ((response == null || response.getBitmap() == null) && oom != null) {
      throw oom;
    }

    response.setPageData(pageNumber);
    return response;
  }

  public Observable<Response> loadPages(Integer... pages) {
    return Observable.fromArray(pages)
        .flatMap(page -> Observable.fromCallable(() -> downloadImage(page)))
        .subscribeOn(Schedulers.io());
  }
}
