package com.HMSolutions.thikrallah.quran.labs.androidquran.ui.helpers;

import android.graphics.drawable.BitmapDrawable;

import com.HMSolutions.thikrallah.quran.labs.androidquran.common.Response;

public interface PageDownloadListener {
  void onLoadImageResponse(BitmapDrawable drawable, Response response);
}
