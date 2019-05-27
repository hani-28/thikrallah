package com.HMSolutions.thikrallah.quran.labs.androidquran.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.quran.labs.androidquran.common.LocalTranslation;
import com.HMSolutions.thikrallah.quran.labs.androidquran.common.QuranAyahInfo;
import com.HMSolutions.thikrallah.quran.labs.androidquran.common.QuranText;
import com.HMSolutions.thikrallah.quran.labs.androidquran.data.QuranInfo;

import java.util.List;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
public class ShareUtil {
  private final QuranInfo quranInfo;

  @Inject
  ShareUtil(QuranInfo quranInfo) {
    this.quranInfo = quranInfo;
  }

  public void copyVerses(Activity activity, List<QuranText> verses) {
    String text = getShareText(activity, verses);
    copyToClipboard(activity, text);
  }

  public void copyToClipboard(Activity activity, String text) {
    ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText(activity.getString(R.string.app_name), text);
    cm.setPrimaryClip(clip);
    Toast.makeText(activity, activity.getString(R.string.ayah_copied_popup),
        Toast.LENGTH_SHORT).show();
  }

  public void shareVerses(Activity activity, List<QuranText> verses) {
    String text = getShareText(activity, verses);
    shareViaIntent(activity, text, R.string.share_ayah_text);
  }

  public void shareViaIntent(Activity activity, String text, @StringRes int titleResId) {
    final Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_TEXT, text);
    activity.startActivity(Intent.createChooser(intent, activity.getString(titleResId)));
  }

  public String getShareText(Context context,
                             QuranAyahInfo ayahInfo,
                             LocalTranslation[] translationNames) {
    final StringBuilder sb = new StringBuilder();
    if (ayahInfo.arabicText != null) {
      sb.append(ayahInfo.arabicText)
        .append("\n\n");
    }

    for (int i = 0, size = ayahInfo.texts.size(); i < size; i++) {
      final CharSequence text = ayahInfo.texts.get(i).getText();
      if (text.length() > 0) {
        if (i < translationNames.length) {
          sb.append('(')
              .append(translationNames[i].getTranslatorName())
              .append(")\n");
        }
        sb.append(text)
            .append("\n\n");
      }
    }
    sb.append('-')
      .append(quranInfo.getSuraAyahString(context, ayahInfo.sura, ayahInfo.ayah));

    return sb.toString();
  }

  private String getShareText(Activity activity, List<QuranText> verses) {
    final int size = verses.size();

    StringBuilder sb = new StringBuilder("(");
    for (int i = 0; i < size; i++) {
      sb.append(verses.get(i).getText());
      if (i + 1 < size) {
        sb.append(" * ");
      }
    }

    // append ) and a new line after last ayah
    sb.append(")\n");
    // append [ before sura label
    sb.append("[");

    final QuranText firstAyah = verses.get(0);
    sb.append(quranInfo.getSuraName(activity, firstAyah.getSura(), true));
    sb.append(" ");
    sb.append(firstAyah.getAyah());
    if (size > 1) {
      final QuranText lastAyah = verses.get(size - 1);
      sb.append(" - ");
      if (firstAyah.getSura() != lastAyah.getSura()) {
        sb.append(quranInfo.getSuraName(activity, lastAyah.getSura(), true));
        sb.append(" ");
      }
      sb.append(lastAyah.getAyah());
    }
    // close sura label and append two new lines
    sb.append("]\n\n");

    sb.append(activity.getString(R.string.via_string));
    return sb.toString();
  }
}
