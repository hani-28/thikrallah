package com.HMSolutions.thikrallah.quran.labs.androidquran.data;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.quran.data.source.PageProvider;
import com.HMSolutions.thikrallah.quran.data.source.QuranDataSource;
import com.HMSolutions.thikrallah.quran.labs.androidquran.util.QuranUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

public class QuranInfo {
  private final int[] suraPageStart;
  private final int[] pageSuraStart;
  private final int[] pageAyahStart;
  private final int[] juzPageStart;
  private final Map<Integer, Integer> juzPageOverride;
  private final int[] pageRub3Start;
  private final int[] suraNumAyahs;
  private final boolean[] suraIsMakki;
  private final int[][] quarters;
  private final int numberOfPages;
  private final int numberOfPagesDual;

  @Inject
  public QuranInfo(PageProvider pageProvider) {
    final QuranDataSource quranDataSource = pageProvider.getDataSource();

    suraPageStart = quranDataSource.getPageForSuraArray();
    pageSuraStart = quranDataSource.getSuraForPageArray();
    pageAyahStart = quranDataSource.getAyahForPageArray();
    juzPageStart = quranDataSource.getPageForJuzArray();
    juzPageOverride = quranDataSource.getJuzDisplayPageArrayOverride();
    pageRub3Start = quranDataSource.getQuarterStartByPage();
    suraNumAyahs = quranDataSource.getNumberOfAyahsForSuraArray();
    suraIsMakki = quranDataSource.getIsMakkiBySuraArray();
    quarters = quranDataSource.getQuartersArray();
    numberOfPages = quranDataSource.getNumberOfPages();
    numberOfPagesDual = numberOfPages / 2;
  }

  public int getNumberOfPages() {
    return numberOfPages;
  }

  public int getNumberOfPagesDual() {
    return numberOfPagesDual;
  }

  /**
   * Get localized sura name from resources
   *
   * @param context    Application context
   * @param sura       Sura number (1~114)
   * @param wantPrefix Whether or not to show prefix "Sura"
   * @return Compiled sura name without translations
   */
  public String getSuraName(Context context, int sura, boolean wantPrefix) {
    return getSuraName(context, sura, wantPrefix, false);
  }

  /**
   * Get localized sura name from resources
   *
   * @param context         Application context
   * @param sura            Sura number (1~114)
   * @param wantPrefix      Whether or not to show prefix "Sura"
   * @param wantTranslation Whether or not to show sura name translations
   * @return Compiled sura name based on provided arguments
   */
  public String getSuraName(Context context, int sura, boolean wantPrefix, boolean wantTranslation) {
    if (sura < Constants.SURA_FIRST ||
        sura > Constants.SURA_LAST) {
      return "";
    }

    StringBuilder builder = new StringBuilder();
    String[] suraNames = context.getResources().getStringArray(R.array.sura_names);
    if (wantPrefix) {
      builder.append(context.getString(R.string.quran_sura_title, suraNames[sura - 1]));
    } else {
      builder.append(suraNames[sura - 1]);
    }
    if (wantTranslation) {
      String translation = context.getResources().getStringArray(R.array.sura_names_translation)[sura - 1];
      if (!TextUtils.isEmpty(translation)) {
        // Some sura names may not have translation
        builder.append(" (");
        builder.append(translation);
        builder.append(")");
      }
    }

    return builder.toString();
  }

  public int getStartingPageForJuz(int juz) {
    return juzPageStart[juz - 1];
  }

  public int getPageNumberForSura(int sura) {
    return suraPageStart[sura - 1];
  }

  public int getSuraNumberFromPage(int page) {
    int sura = -1;
    for (int i = 0; i < Constants.SURAS_COUNT; i++) {
      if (suraPageStart[i] == page) {
        sura = i + 1;
        break;
      } else if (suraPageStart[i] > page) {
        sura = i;
        break;
      }
    }

    return sura;
  }

  public List<Integer> getListOfSurahWithStartingOnPage(int page) {
    int startIndex = pageSuraStart[page - 1] - 1;
    List<Integer> result = new ArrayList<>();
    for (int i = startIndex; i < Constants.SURAS_COUNT; i++) {
      if (suraPageStart[i] == page) {
        result.add(i + 1);
      } else if (suraPageStart[i] > page) break;
    }
    return result;
  }

  public String getSuraNameFromPage(Context context, int page,
                                           boolean wantTitle) {
    int sura = getSuraNumberFromPage(page);
    return (sura > 0) ? getSuraName(context, sura, wantTitle, false) : "";
  }

  public String getPageSubtitle(Context context, int page) {
    String description = context.getString(R.string.page_description);
    return String.format(description,
        QuranUtils.getLocalizedNumber(context, page),
        QuranUtils.getLocalizedNumber(context, getJuzForDisplayFromPage(page)));
  }

  public String getJuzDisplayStringForPage(Context context, int page) {
    String description = context.getString(R.string.juz2_description);
    return String.format(description, QuranUtils.getLocalizedNumber(context,
        getJuzForDisplayFromPage(page)));
  }

  public String getSuraAyahString(Context context, int sura, int ayah) {
    String suraName = getSuraName(context, sura, false, false);
    return context.getString(R.string.sura_ayah_notification_str, suraName, ayah);
  }

  public String getNotificationTitle(Context context,
                                            SuraAyah minVerse,
                                            SuraAyah maxVerse,
                                            boolean isGapless) {
    int minSura = minVerse.sura;
    int maxSura = maxVerse.sura;

    String notificationTitle =
        getSuraName(context, minSura, true, false);
    if (isGapless) {
      // for gapless, don't show the ayah numbers since we're
      // downloading the entire sura(s).
      if (minSura == maxSura) {
        return notificationTitle;
      } else {
        return notificationTitle + " - " +
            getSuraName(context, maxSura, true, false);
      }
    }

    int maxAyah = maxVerse.ayah;
    if (maxAyah == 0) {
      maxSura--;
      maxAyah = getNumAyahs(maxSura);
    }

    if (minSura == maxSura) {
      if (minVerse.ayah == maxAyah) {
        notificationTitle += " (" + maxAyah + ")";
      } else {
        notificationTitle += " (" + minVerse.ayah +
            "-" + maxAyah + ")";
      }
    } else {
      notificationTitle += " (" + minVerse.ayah +
          ") - " + getSuraName(context, maxSura, true, false) +
          " (" + maxAyah + ")";
    }

    return notificationTitle;
  }

  public String getSuraListMetaString(Context context, int sura) {
    String info = context.getString(suraIsMakki[sura - 1]
        ? R.string.makki : R.string.madani) + " - ";

    int ayahs = suraNumAyahs[sura - 1];
    info += context.getResources().getQuantityString(R.plurals.verses, ayahs,
        QuranUtils.getLocalizedNumber(context, ayahs));
    return info;
  }

  public VerseRange getVerseRangeForPage(int page) {
    int[] result = getPageBounds(page);
    final int versesInRange =
        1 + Math.abs(getAyahId(result[0], result[1]) - getAyahId(result[2], result[3]));
    return new VerseRange(result[0], result[1], result[2], result[3], versesInRange);
  }

  public int getFirstAyahOnPage(int page) {
    return pageAyahStart[page - 1];
  }

  @NonNull
  public int[] getPageBounds(int page) {
    if (page > numberOfPages)
      page = numberOfPages;
    if (page < 1) page = 1;

    int[] bounds = new int[4];
    bounds[0] = pageSuraStart[page - 1];
    bounds[1] = pageAyahStart[page - 1];
    if (page == numberOfPages) {
      bounds[2] = Constants.SURA_LAST;
      bounds[3] = 6;
    } else {
      int nextPageSura = pageSuraStart[page];
      int nextPageAyah = pageAyahStart[page];

      if (nextPageSura == bounds[0]) {
        bounds[2] = bounds[0];
        bounds[3] = nextPageAyah - 1;
      } else {
        if (nextPageAyah > 1) {
          bounds[2] = nextPageSura;
          bounds[3] = nextPageAyah - 1;
        } else {
          bounds[2] = nextPageSura - 1;
          bounds[3] = suraNumAyahs[bounds[2] - 1];
        }
      }
    }
    return bounds;
  }

  public int safelyGetSuraOnPage(int page) {
    if (page < Constants.PAGES_FIRST || page > numberOfPages) {

      page = 1;
    }
    return pageSuraStart[page - 1];
  }

  private String getSuraNameFromPage(Context context, int page) {
    for (int i = 0; i < Constants.SURAS_COUNT; i++) {
      if (suraPageStart[i] == page) {
        return getSuraName(context, i + 1, false, false);
      } else if (suraPageStart[i] > page) {
        return getSuraName(context, i, false, false);
      }
    }
    return "";
  }

  /**
   * Gets the juz' that should be printed at the top of the page
   * This may be different than the actual juz' for the page (for example, juz' 7 starts at page
   * 121, but despite this, the title of the page is juz' 6).
   *
   * @param page the page
   * @return the display juz' display string for the page
   */
  @VisibleForTesting
  int getJuzForDisplayFromPage(int page) {
    final int actualJuz = getJuzFromPage(page);
    final Integer overriddenJuz = juzPageOverride.get(page);
    return overriddenJuz == null ? actualJuz : overriddenJuz;
  }

  public int getJuzFromPage(int page) {
    for (int i = 0; i < juzPageStart.length; i++) {
      if (juzPageStart[i] > page) {
        return i;
      } else if (juzPageStart[i] == page) {
        return i + 1;
      }
    }
    return 30;
  }

  private int getJuzFromSuraAyah(int sura, int ayah, int juz) {
    if (juz == 30) {
      return juz;
    }

    // get the starting point of the next juz'
    final int[] lastQuarter = quarters[juz * 8];
    // if we're after that starting point, return juz + 1
    if (sura > lastQuarter[0] || (lastQuarter[0] == sura && ayah >= lastQuarter[1])) {
      return juz + 1;
    } else {
       // otherwise just return this juz
      return juz;
    }
  }

  public int getRub3FromPage(int page) {
    if ((page > numberOfPages) || (page < 1)) return -1;
    return pageRub3Start[page - 1];
  }

  public int getPageFromSuraAyah(int sura, int ayah) {
    // basic bounds checking
    if (ayah == 0) ayah = 1;
    if ((sura < 1) || (sura > Constants.SURAS_COUNT)
        || (ayah < Constants.AYA_MIN) ||
        (ayah > Constants.AYA_MAX))
      return -1;

    // what page does the sura start on?
    int index = suraPageStart[sura - 1] - 1;
    while (index < numberOfPages) {
      // what's the first sura in that page?
      int ss = pageSuraStart[index];

      // if we've passed the sura, return the previous page
      // or, if we're at the same sura and passed the ayah
      if (ss > sura || ((ss == sura) &&
          (pageAyahStart[index] > ayah))) {
        break;
      }

      // otherwise, look at the next page
      index++;
    }

    return index;
  }

  public int getAyahId(int sura, int ayah) {
    int ayahId = 0;
    for (int i = 0; i < sura - 1; i++) {
      ayahId += suraNumAyahs[i];
    }
    ayahId += ayah;
    return ayahId;
  }

  public SuraAyah getSuraAyahFromAyahId(int ayahId) {
    int sura = 0;
    int ayahIdentifier = ayahId;
    while (ayahIdentifier > suraNumAyahs[sura]) {
      ayahIdentifier -= suraNumAyahs[sura++];
    }
    return new SuraAyah(sura + 1, ayahIdentifier);
  }

  public int getNumAyahs(int sura) {
    if ((sura < 1) || (sura > Constants.SURAS_COUNT)) return -1;
    return suraNumAyahs[sura - 1];
  }

  public  int getPageFromPos(int position, boolean dual) {
    int page = numberOfPages - position;
    if (dual) {
      page = (numberOfPagesDual - position) * 2;
    }
    return page;
  }

  public int getPosFromPage(int page, boolean dual) {
    int position = numberOfPages - page;
    if (dual) {
      if (page % 2 != 0) {
        page++;
      }
      position = numberOfPagesDual - (page / 2);
    }
    return position;
  }

  public String getAyahString(int sura, int ayah, Context context) {
    return getSuraName(context, sura, true) + " - " + context.getString(R.string.quran_ayah, ayah);
  }

  public  String getAyahMetadata(int sura, int ayah, int page, Context context) {
    int juz = getJuzForDisplayFromPage(page);
    return context.getString(R.string.quran_ayah_details, getSuraName(context, sura, true),
        QuranUtils.getLocalizedNumber(context, ayah),
        QuranUtils.getLocalizedNumber(context, getJuzFromSuraAyah(sura, ayah, juz)));
  }

  public  String getSuraNameString(Context context, int page) {
    return context.getString(R.string.quran_sura_title, getSuraNameFromPage(context, page));
  }

  // not ideal, should change this later
  public  int[] getQuarterByIndex(int quarter) {
    return quarters[quarter];
  }

  public Set<String> getAyahKeysOnPage(int page, SuraAyah lowerBound, SuraAyah upperBound) {
    Set<String> ayahKeys = new LinkedHashSet<>();
    int[] bounds = getPageBounds(page);
    SuraAyah start = new SuraAyah(bounds[0], bounds[1]);
    SuraAyah end = new SuraAyah(bounds[2], bounds[3]);
    if (lowerBound != null) {
      start = SuraAyah.max(start, lowerBound);
    }
    if (upperBound != null) {
      end = SuraAyah.min(end, upperBound);
    }
    SuraAyahIterator iterator = new SuraAyahIterator(this, start, end);
    while (iterator.next()) {
      ayahKeys.add(iterator.getSura() + ":" + iterator.getAyah());
    }
    return ayahKeys;
  }

}
