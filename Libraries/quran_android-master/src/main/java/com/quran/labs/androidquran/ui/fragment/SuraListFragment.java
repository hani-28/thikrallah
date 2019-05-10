package com.quran.labs.androidquran.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quran.labs.androidquran.QuranApplication;
import com.quran.labs.androidquran.R;
import com.quran.labs.androidquran.data.Constants;
import com.quran.labs.androidquran.data.QuranInfo;
import com.quran.labs.androidquran.ui.QuranActivity;
import com.quran.labs.androidquran.ui.helpers.QuranListAdapter;
import com.quran.labs.androidquran.ui.helpers.QuranRow;
import com.quran.labs.androidquran.util.QuranSettings;
import com.quran.labs.androidquran.util.QuranUtils;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;

import static com.quran.labs.androidquran.data.Constants.JUZ2_COUNT;
import static com.quran.labs.androidquran.data.Constants.SURAS_COUNT;

public class SuraListFragment extends Fragment {

  private RecyclerView mRecyclerView;
  private Disposable disposable;

  @Inject QuranInfo quranInfo;
  private int numberOfPages;

  public static SuraListFragment newInstance() {
    return new SuraListFragment();
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           ViewGroup container, Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.quran_list, container, false);

    final Context context = getActivity();
    mRecyclerView = view.findViewById(R.id.recycler_view);
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    mRecyclerView.setItemAnimator(new DefaultItemAnimator());

    final QuranListAdapter adapter =
        new QuranListAdapter(context, mRecyclerView, getSuraList(), false);
    mRecyclerView.setAdapter(adapter);
    return view;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    ((QuranApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    numberOfPages = quranInfo.getNumberOfPages();
  }

  @Override
  public void onPause() {
    if (disposable != null) {
      disposable.dispose();
    }
    super.onPause();
  }

  @Override
  public void onResume() {
    final Activity activity = getActivity();
    if (activity instanceof QuranActivity) {
      QuranSettings settings = QuranSettings.getInstance(activity);
      disposable = ((QuranActivity) activity).getLatestPageObservable()
          .first(Constants.NO_PAGE)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribeWith(new DisposableSingleObserver<Integer>() {
            @Override
            public void onSuccess(Integer recentPage) {
              if (recentPage != Constants.NO_PAGE) {
                int sura = quranInfo.safelyGetSuraOnPage(recentPage);
                int juz = quranInfo.getJuzFromPage(recentPage);
                int position = sura + juz - 1;
                mRecyclerView.scrollToPosition(position);
              }
            }

            @Override
            public void onError(Throwable e) {
            }
          });

      if (settings.isArabicNames()) {
        updateScrollBarPositionHoneycomb();
      }
    }

    super.onResume();
  }

  private void updateScrollBarPositionHoneycomb() {
    mRecyclerView.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);
  }

  private QuranRow[] getSuraList() {
    int next;
    int pos = 0;
    int sura = 1;
    QuranRow[] elements = new QuranRow[SURAS_COUNT + JUZ2_COUNT];

    Activity activity = getActivity();
    boolean wantPrefix = activity.getResources().getBoolean(R.bool.show_surat_prefix);
    boolean wantTranslation = activity.getResources().getBoolean(R.bool.show_sura_names_translation);
    for (int juz = 1; juz <= JUZ2_COUNT; juz++) {
      final String headerTitle = activity.getString(R.string.juz2_description,
          QuranUtils.getLocalizedNumber(activity, juz));
      final QuranRow.Builder headerBuilder = new QuranRow.Builder()
          .withType(QuranRow.HEADER)
          .withText(headerTitle)
          .withPage(quranInfo.getStartingPageForJuz(juz));
      elements[pos++] = headerBuilder.build();
      next = (juz == JUZ2_COUNT) ? numberOfPages + 1 :
          quranInfo.getStartingPageForJuz(juz + 1);

      while ((sura <= SURAS_COUNT) && (quranInfo.getPageNumberForSura(sura) < next)) {
        final QuranRow.Builder builder = new QuranRow.Builder()
            .withText(quranInfo.getSuraName(activity, sura, wantPrefix, wantTranslation))
            .withMetadata(quranInfo.getSuraListMetaString(activity, sura))
            .withSura(sura)
            .withPage(quranInfo.getPageNumberForSura(sura));
        elements[pos++] = builder.build();
        sura++;
      }
    }

    return elements;
  }
}
