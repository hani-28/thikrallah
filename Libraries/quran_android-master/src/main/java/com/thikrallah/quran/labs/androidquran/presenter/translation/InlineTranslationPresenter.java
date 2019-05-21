package com.thikrallah.quran.labs.androidquran.presenter.translation;

import androidx.annotation.NonNull;

import com.thikrallah.quran.labs.androidquran.common.LocalTranslation;
import com.thikrallah.quran.labs.androidquran.common.QuranAyahInfo;
import com.thikrallah.quran.labs.androidquran.data.QuranInfo;
import com.thikrallah.quran.labs.androidquran.data.VerseRange;
import com.thikrallah.quran.labs.androidquran.database.TranslationsDBAdapter;
import com.thikrallah.quran.labs.androidquran.model.translation.TranslationModel;
import com.thikrallah.quran.labs.androidquran.util.QuranSettings;
import com.thikrallah.quran.labs.androidquran.util.TranslationUtil;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;

public class InlineTranslationPresenter extends
    BaseTranslationPresenter<InlineTranslationPresenter.TranslationScreen> {
  private final QuranSettings quranSettings;

  @Inject
  InlineTranslationPresenter(TranslationModel translationModel,
                             TranslationsDBAdapter dbAdapter,
                             TranslationUtil translationUtil,
                             QuranSettings quranSettings,
                             QuranInfo quranInfo) {
    super(translationModel, dbAdapter, translationUtil, quranInfo);
    this.quranSettings = quranSettings;
  }

  public void refresh(VerseRange verseRange) {
    if (getDisposable() != null) {
      getDisposable().dispose();
    }

    setDisposable(getVerses(false, getTranslations(quranSettings), verseRange)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableSingleObserver<ResultHolder>() {
          @Override
          public void onSuccess(ResultHolder result) {
            if (getTranslationScreen() != null) {
              getTranslationScreen()
                  .setVerses(result.getTranslations(), result.getAyahInformation());
            }
          }

          @Override
          public void onError(Throwable e) {
          }
        }));
  }

  public interface TranslationScreen {
    void setVerses(@NonNull LocalTranslation[] translations, @NonNull List<QuranAyahInfo> verses);
  }
}
