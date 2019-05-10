package com.quran.labs.androidquran.ui.helpers;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.ViewGroup;

import com.quran.labs.androidquran.data.Constants;
import com.quran.labs.androidquran.data.QuranInfo;
import com.quran.labs.androidquran.ui.fragment.QuranPageFragment;
import com.quran.labs.androidquran.ui.fragment.TabletFragment;
import com.quran.labs.androidquran.ui.fragment.TranslationFragment;

import timber.log.Timber;

public class QuranPageAdapter extends FragmentStatePagerAdapter {

  private boolean isShowingTranslation;
  private boolean isDualPages;
  private final QuranInfo quranInfo;
  private final int totalPages;
  private final int totalPagesDual;

  public QuranPageAdapter(FragmentManager fm, boolean dualPages,
                          boolean isShowingTranslation,
                          QuranInfo quranInfo) {
    super(fm, dualPages ? "dualPages" : "singlePage");
    this.quranInfo = quranInfo;
    isDualPages = dualPages;
    this.isShowingTranslation = isShowingTranslation;
    totalPages = quranInfo.getNumberOfPages();
    totalPagesDual = totalPages / 2;
  }

  public void setTranslationMode() {
    if (!isShowingTranslation) {
      isShowingTranslation = true;
      notifyDataSetChanged();
    }
  }

  public void setQuranMode() {
    if (isShowingTranslation) {
      isShowingTranslation = false;
      notifyDataSetChanged();
    }
  }

  public boolean getIsShowingTranslation() {
    return isShowingTranslation;
  }

  @Override
  public int getItemPosition(@NonNull Object object) {
    /* when the ViewPager gets a notifyDataSetChanged (or invalidated),
     * it goes through its set of saved views and runs this method on
     * each one to figure out whether or not it should remove the view
     * or not.  the default implementation returns POSITION_UNCHANGED,
     * which means that "this page is as is."
     *
     * as noted in http://stackoverflow.com/questions/7263291 in one
     * of the answers, if you're just updating your view (changing a
     * field's value, etc), this is highly inefficient (because you
     * recreate the view for nothing).
     *
     * in our case, however, this is the right thing to do since we
     * change the fragment completely when we notifyDataSetChanged.
     */
    return POSITION_NONE;
  }

  @Override
  public int getCount() {
    return isDualPages ? totalPagesDual : totalPages;
  }

  @Override
  public Fragment getItem(int position) {
    int page = quranInfo.getPageFromPos(position, isDualPages);
    Timber.d("getting page: %d", page);
    if (isDualPages) {
      return TabletFragment.newInstance(page,
          isShowingTranslation ? TabletFragment.Mode.TRANSLATION :
              TabletFragment.Mode.ARABIC);
    } else if (isShowingTranslation) {
      return TranslationFragment.newInstance(page);
    } else {
      return QuranPageFragment.newInstance(page);
    }
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    Fragment f = (Fragment) object;
    Timber.d("destroying item: %d, %s", position, f);
    cleanupFragment(f);
    super.destroyItem(container, position, object);
  }

  @Override
  public void cleanupFragment(Fragment f) {
    if (f instanceof QuranPageFragment) {
      ((QuranPageFragment) f).cleanup();
    } else if (f instanceof TabletFragment) {
      ((TabletFragment) f).cleanup();
    }
  }

  public QuranPage getFragmentIfExistsForPage(int page) {
    if (page < Constants.PAGES_FIRST || totalPages < page) {
      return null;
    }
    int position = quranInfo.getPosFromPage(page, isDualPages);
    Fragment fragment = getFragmentIfExists(position);
    return fragment instanceof QuranPage && fragment.isAdded() ? (QuranPage) fragment : null;
  }

}
