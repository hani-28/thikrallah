package com.quran.labs.androidquran.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quran.labs.androidquran.common.LocalTranslation;
import com.quran.labs.androidquran.common.QuranAyahInfo;
import com.quran.labs.androidquran.data.QuranInfo;
import com.quran.labs.androidquran.module.fragment.QuranPageModule;
import com.quran.labs.androidquran.presenter.quran.ayahtracker.AyahTrackerItem;
import com.quran.labs.androidquran.presenter.quran.ayahtracker.AyahTrackerPresenter;
import com.quran.labs.androidquran.presenter.quran.ayahtracker.AyahTranslationTrackerItem;
import com.quran.labs.androidquran.presenter.translation.TranslationPresenter;
import com.quran.labs.androidquran.ui.PagerActivity;
import com.quran.labs.androidquran.ui.helpers.AyahTracker;
import com.quran.labs.androidquran.ui.helpers.QuranPage;
import com.quran.labs.androidquran.ui.translation.OnTranslationActionListener;
import com.quran.labs.androidquran.ui.translation.TranslationView;
import com.quran.labs.androidquran.util.QuranSettings;
import com.quran.labs.androidquran.widgets.QuranTranslationPageLayout;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class TranslationFragment extends Fragment implements
    AyahTrackerPresenter.AyahInteractionHandler, QuranPage,
    TranslationPresenter.TranslationScreen,
    OnTranslationActionListener {
  private static final String PAGE_NUMBER_EXTRA = "pageNumber";

  private static final String SI_PAGE_NUMBER = "SI_PAGE_NUMBER";
  private static final String SI_HIGHLIGHTED_AYAH = "SI_HIGHLIGHTED_AYAH";
  private static final String SI_SCROLL_POSITION = "SI_SCROLL_POSITION";

  private int pageNumber;
  private int highlightedAyah;
  private int scrollPosition;

  private TranslationView translationView;
  private QuranTranslationPageLayout mainView;
  private AyahTrackerItem[] ayahTrackerItems;

  @Inject QuranInfo quranInfo;
  @Inject QuranSettings quranSettings;
  @Inject TranslationPresenter presenter;
  @Inject AyahTrackerPresenter ayahTrackerPresenter;

  public static TranslationFragment newInstance(int page) {
    final TranslationFragment f = new TranslationFragment();
    final Bundle args = new Bundle();
    args.putInt(PAGE_NUMBER_EXTRA, page);
    f.setArguments(args);
    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      int page = savedInstanceState.getInt(SI_PAGE_NUMBER, -1);
      if (page == pageNumber) {
        int highlightedAyah =
            savedInstanceState.getInt(SI_HIGHLIGHTED_AYAH, -1);
        if (highlightedAyah > 0) {
          this.highlightedAyah = highlightedAyah;
        }
      }
      scrollPosition = savedInstanceState.getInt(SI_SCROLL_POSITION);
    }
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    Context context = getActivity();
    mainView = new QuranTranslationPageLayout(context);
    mainView.setPageController(null, pageNumber);

    translationView = mainView.getTranslationView();
    translationView.setTranslationClickedListener(v -> {
      final Activity activity = getActivity();
      if (activity instanceof PagerActivity) {
        ((PagerActivity) getActivity()).toggleActionBar();
      }
    });

    translationView.setOnTranslationActionListener(this);
    return mainView;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    pageNumber = getArguments() != null ? getArguments().getInt(PAGE_NUMBER_EXTRA) : -1;
    ((PagerActivity) getActivity()).getPagerActivityComponent()
        .quranPageComponentBuilder()
        .withQuranPageModule(new QuranPageModule(pageNumber))
        .build()
        .inject(this);
  }

  @Override
  public void onTranslationAction(QuranAyahInfo ayah,
                                  LocalTranslation[] translations,
                                  int actionId) {
    Activity activity = getActivity();
    if (activity instanceof PagerActivity) {
      presenter.onTranslationAction((PagerActivity) activity, ayah, translations, actionId);
    }
    translationView.unhighlightAyat();
  }

  @Override
  public void updateView() {
    if (isAdded()) {
      mainView.updateView(quranSettings);
      refresh();
    }
  }

  @Override
  public AyahTracker getAyahTracker() {
    return ayahTrackerPresenter;
  }

  @Override
  public AyahTrackerItem[] getAyahTrackerItems() {
    if (ayahTrackerItems == null) {
      ayahTrackerItems = new AyahTrackerItem[] {
          new AyahTranslationTrackerItem(pageNumber, quranInfo, translationView) };
    }
    return ayahTrackerItems;
  }

  @Override
  public void onResume() {
    super.onResume();
    ayahTrackerPresenter.bind(this);
    presenter.bind(this);
    updateView();
  }

  @Override
  public void onPause() {
    ayahTrackerPresenter.unbind(this);
    presenter.unbind(this);
    super.onPause();
  }

  @Override
  public void setVerses(int page,
                        @NonNull LocalTranslation[] translations,
                        @NonNull List<QuranAyahInfo> verses) {
    translationView.setVerses(quranInfo, translations, verses);
    if (highlightedAyah > 0) {
      translationView.highlightAyah(highlightedAyah);
    }
  }

  @Override
  public void updateScrollPosition() {
    translationView.setScrollPosition(scrollPosition);
  }

  public void refresh() {
    presenter.refresh();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    if (highlightedAyah > 0) {
      outState.putInt(SI_HIGHLIGHTED_AYAH, highlightedAyah);
    }
    scrollPosition = translationView.findFirstCompletelyVisibleItemPosition();
    outState.putInt(SI_SCROLL_POSITION, scrollPosition);
    super.onSaveInstanceState(outState);
  }
}
