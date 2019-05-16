package com.thikrallah.quran.labs.androidquran.presenter;

public interface Presenter<T> {
  void bind(T what);
  void unbind(T what);
}
