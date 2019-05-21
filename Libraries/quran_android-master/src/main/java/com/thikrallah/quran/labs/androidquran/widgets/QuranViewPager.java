package com.thikrallah.quran.labs.androidquran.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class QuranViewPager extends ViewPager {

  public QuranViewPager(Context context) {
    super(context);
  }

  public QuranViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    try {
      return super.onTouchEvent(ev);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
