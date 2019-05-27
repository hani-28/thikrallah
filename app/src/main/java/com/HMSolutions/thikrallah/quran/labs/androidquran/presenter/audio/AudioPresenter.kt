package com.HMSolutions.thikrallah.quran.labs.androidquran.presenter.audio

import android.content.Context
import android.content.Intent
import com.HMSolutions.thikrallah.R
import com.HMSolutions.thikrallah.quran.labs.androidquran.common.QariItem
import com.HMSolutions.thikrallah.quran.labs.androidquran.dao.audio.AudioPathInfo
import com.HMSolutions.thikrallah.quran.labs.androidquran.dao.audio.AudioRequest
import com.HMSolutions.thikrallah.quran.labs.androidquran.data.QuranInfo
import com.HMSolutions.thikrallah.quran.labs.androidquran.data.SuraAyah
import com.HMSolutions.thikrallah.quran.labs.androidquran.presenter.Presenter
import com.HMSolutions.thikrallah.quran.labs.androidquran.service.QuranDownloadService
import com.HMSolutions.thikrallah.quran.labs.androidquran.service.util.ServiceIntentHelper
import com.HMSolutions.thikrallah.quran.labs.androidquran.ui.PagerActivity
import com.HMSolutions.thikrallah.quran.labs.androidquran.util.AudioUtils
import com.HMSolutions.thikrallah.quran.labs.androidquran.util.QuranFileUtils
import java.io.File
import javax.inject.Inject

class AudioPresenter @Inject
constructor(private val quranInfo: QuranInfo,
            private val audioUtil: AudioUtils,
            private val quranFileUtils: QuranFileUtils) : Presenter<PagerActivity> {
  private var pagerActivity: PagerActivity? = null
  private var lastAudioRequest: AudioRequest? = null

  fun play(start: SuraAyah,
           end: SuraAyah,
           qari: QariItem,
           verseRepeat: Int,
           rangeRepeat: Int,
           enforceRange: Boolean,
           shouldStream: Boolean) {
    val audioPathInfo = getLocalAudioPathInfo(qari)
    if (audioPathInfo != null) {
      // override streaming if all the files are already downloaded
      val stream = if (shouldStream) {
        !haveAllFiles(audioPathInfo, start, end)
      } else {
        false
      }

      // if we're still streaming, change the base qari format in audioPathInfo
      // to a remote url format (instead of a path to a local directory)
      val audioPath = if (stream) {
        audioPathInfo.copy(urlFormat = audioUtil.getQariUrl(qari))
      } else {
        audioPathInfo
      }

      val audioRequest = AudioRequest(
          start, end, qari, verseRepeat, rangeRepeat, enforceRange, stream, audioPath)
      play(audioRequest)
    }
  }

  fun play(audioRequest: AudioRequest) {
    lastAudioRequest = audioRequest
    pagerActivity?.let {
      val downloadIntent = getDownloadIntent(it, audioRequest)
      if (downloadIntent != null) {
        it.handleRequiredDownload(downloadIntent)
      } else {
        // play the audio
        it.handlePlayback(audioRequest)
      }
    }
  }

  fun onDownloadPermissionGranted() {
    lastAudioRequest?.let { play(it) }
  }

  fun onDownloadSuccess() {
    lastAudioRequest?.let { play(it) }
  }

  private fun getDownloadIntent(context: Context, request: AudioRequest): Intent? {
    val qari = request.qari
    val audioPathInfo = request.audioPathInfo
    val path = audioPathInfo.localDirectory
    val gaplessDb = audioPathInfo.gaplessDatabase

    return if (!quranFileUtils.haveAyaPositionFile(context)) {
      getDownloadIntent(context,
          quranFileUtils.ayaPositionFileUrl,
          quranFileUtils.getQuranAyahDatabaseDirectory(context),
          context.getString(R.string.highlighting_database))
    } else if (gaplessDb != null && !File(gaplessDb).exists()) {
      getDownloadIntent(context,
          audioUtil.getGaplessDatabaseUrl(qari)!!,
          path,
          context.getString(R.string.timing_database))
    } else if (!request.shouldStream &&
        audioUtil.shouldDownloadBasmallah(path,
            request.start,
            request.end,
            qari.isGapless)) {
      val title = quranInfo.getNotificationTitle(
          context, request.start, request.start, qari.isGapless)
      getDownloadIntent(context, audioUtil.getQariUrl(qari), path, title).apply {
        putExtra(QuranDownloadService.EXTRA_START_VERSE, request.start)
        putExtra(QuranDownloadService.EXTRA_END_VERSE, request.start)
      }
    } else if (!request.shouldStream && !haveAllFiles(audioPathInfo, request.start, request.end)) {
      val title = quranInfo.getNotificationTitle(
          context, request.start, request.end, qari.isGapless)
      getDownloadIntent(context, audioUtil.getQariUrl(qari), path, title).apply {
        putExtra(QuranDownloadService.EXTRA_START_VERSE, request.start)
        putExtra(QuranDownloadService.EXTRA_END_VERSE, request.end)
        putExtra(QuranDownloadService.EXTRA_IS_GAPLESS, qari.isGapless)
      }
    } else {
      null
    }
  }

  private fun getDownloadIntent(context: Context,
                                url: String,
                                destination: String,
                                title: String): Intent {
    return ServiceIntentHelper.getAudioDownloadIntent(context, url, destination, title)
  }

  private fun getLocalAudioPathInfo(qari: QariItem): AudioPathInfo? {
    pagerActivity?.let {
      val localPath = audioUtil.getLocalQariUrl(it, qari)
      if (localPath != null) {
        val databasePath = audioUtil.getQariDatabasePathIfGapless(it, qari)
        val urlFormat = if (databasePath.isNullOrEmpty()) {
          localPath + File.separator + "%d" + File.separator +
              "%d" + AudioUtils.AUDIO_EXTENSION
        } else {
          localPath + File.separator + "%03d" + AudioUtils.AUDIO_EXTENSION
        }
        return AudioPathInfo(urlFormat, localPath, databasePath)
      }
    }
    return null
  }

  private fun haveAllFiles(audioPathInfo: AudioPathInfo, start: SuraAyah, end: SuraAyah): Boolean {
    return audioUtil.haveAllFiles(audioPathInfo.urlFormat,
        audioPathInfo.localDirectory, start, end, audioPathInfo.gaplessDatabase != null)
  }

  override fun bind(what: PagerActivity) {
    pagerActivity = what
  }

  override fun unbind(what: PagerActivity) {
    if (pagerActivity == what) {
      pagerActivity = null
    }
  }
}
