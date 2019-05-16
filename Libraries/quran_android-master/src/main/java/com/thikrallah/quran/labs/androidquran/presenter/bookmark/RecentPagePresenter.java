package com.thikrallah.quran.labs.androidquran.presenter.bookmark;

import com.thikrallah.quran.labs.androidquran.data.Constants;
import com.thikrallah.quran.labs.androidquran.di.ActivityScope;
import com.thikrallah.quran.labs.androidquran.model.bookmark.RecentPageModel;
import com.thikrallah.quran.labs.androidquran.presenter.Presenter;
import com.thikrallah.quran.labs.androidquran.ui.PagerActivity;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

@ActivityScope
public class RecentPagePresenter implements Presenter<PagerActivity> {
  private final RecentPageModel model;

  private int lastPage;
  private int minimumPage;
  private int maximumPage;
  private Disposable disposable;

  @Inject
  RecentPagePresenter(RecentPageModel model) {
    this.model = model;
  }

  private void onPageChanged(int page) {
    model.updateLatestPage(page);

    lastPage = page;
    if (minimumPage == Constants.NO_PAGE) {
      minimumPage = page;
      maximumPage = page;
    } else if (page < minimumPage) {
      minimumPage = page;
    } else if (page > maximumPage) {
      maximumPage = page;
    }
  }

  public void onJump() {
    saveAndReset();
  }

  @Override
  public void bind(PagerActivity what) {
    minimumPage = Constants.NO_PAGE;
    maximumPage = Constants.NO_PAGE;
    lastPage = Constants.NO_PAGE;

    disposable = what.getViewPagerObservable()
        .subscribeWith(new DisposableObserver<Integer>() {
          @Override
          public void onNext(Integer value) {
            onPageChanged(value);
          }

          @Override
          public void onError(Throwable e) {
          }

          @Override
          public void onComplete() {
          }
        });
  }

  @Override
  public void unbind(PagerActivity what) {
    disposable.dispose();
    saveAndReset();
  }

  private void saveAndReset() {
    if (minimumPage != Constants.NO_PAGE || maximumPage != Constants.NO_PAGE) {
      model.persistLatestPage(minimumPage, maximumPage, lastPage);

      minimumPage = Constants.NO_PAGE;
      maximumPage = Constants.NO_PAGE;
    }
    lastPage = Constants.NO_PAGE;
  }
}
