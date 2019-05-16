package com.thikrallah.quran.labs.androidquran.ui.helpers;

import android.graphics.drawable.BitmapDrawable;

import com.thikrallah.quran.labs.androidquran.common.Response;

public interface PageDownloadListener {
  void onLoadImageResponse(BitmapDrawable drawable, Response response);
}
