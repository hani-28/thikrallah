package com.HMSolutions.thikrallah.quran.labs.androidquran.service.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.quran.labs.androidquran.QuranDataActivity;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class QuranDownloadNotifier {
  // error messages
  public static final int ERROR_DISK_SPACE = 1;
  public static final int ERROR_PERMISSIONS = 2;
  public static final int ERROR_NETWORK = 3;
  public static final int ERROR_INVALID_DOWNLOAD = 4;
  public static final int ERROR_CANCELLED = 5;
  public static final int ERROR_GENERAL = 6;

  // notification ids
  private static final int DOWNLOADING_NOTIFICATION = 1;
  public static final int DOWNLOADING_COMPLETE_NOTIFICATION = 2;
  private static final int DOWNLOADING_ERROR_NOTIFICATION = 3;

  private static final String NOTIFICATION_CHANNEL_ID = "quran_download_progress";

  public static class ProgressIntent {
    public static final String INTENT_NAME =
        "com.HMSolutions.thikrallah.quran.labs.androidquran.download.ProgressUpdate";
    static final String NAME = "notificationTitle";
    public static final String DOWNLOAD_KEY = "downloadKey";
    public static final String DOWNLOAD_TYPE = "downloadType";
    public static final String STATE = "state";
    public static final String PROGRESS = "progress";
    public static final String TOTAL_SIZE = "totalSize";
    public static final String DOWNLOADED_SIZE = "downloadedSize";
    public static final String PROCESSED_FILES = "processedFiles";
    public static final String TOTAL_FILES = "totalFiles";
    public static final String ERROR_CODE = "errorCode";
    public static final String SURA = "sura";
    public static final String AYAH = "ayah";

    // states for the intent maybe one of these values
    public static final String STATE_DOWNLOADING = "downloading";
    public static final String STATE_PROCESSING = "processing";
    public static final String STATE_SUCCESS = "success";
    public static final String STATE_ERROR = "error";
    public static final String STATE_ERROR_WILL_RETRY = "errorWillRetry";
  }

  public static class NotificationDetails {
    public String title;
    public String key;
    public int type;
    public int currentFile;
    public int totalFiles;
    public int sura;
    public int ayah;
    boolean sendIndeterminate;
    public boolean isGapless;

    public NotificationDetails(String title, String key, int type){
      this.key = key;
      this.title = title;
      this.type = type;
      sendIndeterminate = false;
    }

    public void setFileStatus(int current, int total){
      totalFiles = total;
      currentFile = current;
    }

    public void setIsGapless(boolean isGapless) {
      this.isGapless = isGapless;
    }
  }

  private Context appContext;
  private Service service;
  private NotificationManager notificationManager;
  private LocalBroadcastManager broadcastManager;
  private int notificationColor;
  private int lastProgress;
  private int lastMaximum;
  private boolean isForeground;

  public QuranDownloadNotifier(Context context, Service service) {
    appContext = context.getApplicationContext();
    notificationManager = (NotificationManager) appContext
        .getSystemService(Context.NOTIFICATION_SERVICE);
    broadcastManager = LocalBroadcastManager.getInstance(appContext);
    notificationColor = ContextCompat.getColor(appContext, R.color.notification_color);
    lastProgress = -1;
    lastMaximum = -1;
    this.service = service;

    // setup Android O notification channels
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      setupNotificationChannel();
    }
  }

  public void resetNotifications() {
    // hide any previous errors, canceled, etc
    lastMaximum = -1;
    lastProgress = -1;
    notificationManager.cancel(DOWNLOADING_ERROR_NOTIFICATION);
    notificationManager.cancel(DOWNLOADING_COMPLETE_NOTIFICATION);
  }

  public Intent notifyProgress(NotificationDetails details,
      long downloadedSize, long totalSize){

    int max = 100;
    int progress = 0;

    // send indeterminate if total size is 0 or less or the notification
    // details says to always send indeterminate (never happens right now,
    // so only when the total size is 0 or less).
    boolean isIndeterminate = details.sendIndeterminate;
    if (!isIndeterminate && totalSize <= 0){
      isIndeterminate = true;
    }

    if (!isIndeterminate){
      // calculate percentage based on files downloaded, files left,
      // and percentage of this file that is left
      double percent = (1.0 * downloadedSize) / (1.0 * totalSize);
      if (details.isGapless) {
        progress = (int)(percent * 100);
      } else {
        double percentPerFile = 100.0f / details.totalFiles;
        progress = (int)((percentPerFile * (details.currentFile - 1)) +
                (percent * percentPerFile));
      }

      if (details.sura > 0 && details.ayah > 0) {
        progress = (int) (((float) details.currentFile / (float) details.totalFiles) * 100.0f);
      }
    }

    showNotification(details.title,
        appContext.getString(R.string.downloading_title),
        DOWNLOADING_NOTIFICATION, true, max, progress, isIndeterminate, !isForeground);

    // send broadcast
    Intent progressIntent = new Intent(ProgressIntent.INTENT_NAME);
    progressIntent.putExtra(ProgressIntent.NAME, details.title);
    progressIntent.putExtra(ProgressIntent.DOWNLOAD_KEY, details.key);
    progressIntent.putExtra(ProgressIntent.DOWNLOAD_TYPE, details.type);
    progressIntent.putExtra(ProgressIntent.STATE,
        ProgressIntent.STATE_DOWNLOADING);
    if (details.sura > 0) {
      progressIntent.putExtra(ProgressIntent.SURA, details.sura);
      progressIntent.putExtra(ProgressIntent.AYAH, details.ayah);
    }

    if (!isIndeterminate){
      progressIntent.putExtra(ProgressIntent.DOWNLOADED_SIZE,
          downloadedSize);
      progressIntent.putExtra(ProgressIntent.TOTAL_SIZE, totalSize);
      progressIntent.putExtra(ProgressIntent.PROGRESS, progress);
    }
    broadcastManager.sendBroadcast(progressIntent);
    return progressIntent;
  }

  public Intent notifyDownloadProcessing(
      NotificationDetails details, int done, int total){
    String processingString =
        appContext.getString(R.string.download_processing);
    showNotification(details.title, processingString,
        DOWNLOADING_NOTIFICATION, true, !isForeground);

    // send broadcast
    Intent progressIntent = new Intent(ProgressIntent.INTENT_NAME);
    progressIntent.putExtra(ProgressIntent.NAME, details.title);
    progressIntent.putExtra(ProgressIntent.DOWNLOAD_KEY, details.key);
    progressIntent.putExtra(ProgressIntent.DOWNLOAD_TYPE, details.type);
    progressIntent.putExtra(ProgressIntent.STATE,
        ProgressIntent.STATE_PROCESSING);

    if (total > 0){
      int progress = (int)((100.0 * done) / (1.0 * total));
      progressIntent.putExtra(ProgressIntent.PROGRESS, progress);
      progressIntent.putExtra(ProgressIntent.PROCESSED_FILES, done);
      progressIntent.putExtra(ProgressIntent.TOTAL_FILES, total);
    }

    broadcastManager.sendBroadcast(progressIntent);
    return progressIntent;
  }

  public Intent notifyDownloadSuccessful(NotificationDetails details){
    String successString = appContext.getString(R.string.download_successful);
    notificationManager.cancel(DOWNLOADING_ERROR_NOTIFICATION);

    lastMaximum = -1;
    lastProgress = -1;
    showNotification(details.title, successString,
        DOWNLOADING_COMPLETE_NOTIFICATION, false, false);
    return broadcastDownloadSuccessful(details);
  }

  public Intent broadcastDownloadSuccessful(NotificationDetails details){
    // send broadcast
    Intent progressIntent = new Intent(ProgressIntent.INTENT_NAME);
    progressIntent.putExtra(ProgressIntent.NAME, details.title);
    progressIntent.putExtra(ProgressIntent.STATE,
        ProgressIntent.STATE_SUCCESS);
    progressIntent.putExtra(ProgressIntent.DOWNLOAD_KEY, details.key);
    progressIntent.putExtra(ProgressIntent.DOWNLOAD_TYPE, details.type);
    broadcastManager.sendBroadcast(progressIntent);
    return progressIntent;
  }

  public Intent notifyError(int errorCode, boolean isFatal, NotificationDetails details){
    int errorId;
    switch (errorCode){
      case ERROR_DISK_SPACE:
        errorId = R.string.download_error_disk;
        break;
      case ERROR_NETWORK:
        errorId = R.string.download_error_network;
        break;
      case ERROR_PERMISSIONS:
        errorId = R.string.download_error_perms;
        break;
      case ERROR_INVALID_DOWNLOAD:
        errorId = R.string.download_error_invalid_download;
        if (!isFatal){
          errorId = R.string.download_error_invalid_download_retry;
        }
        break;
      case ERROR_CANCELLED:
        errorId = R.string.notification_download_canceled;
        break;
      case ERROR_GENERAL:
      default:
        errorId = R.string.download_error_general;
        break;
    }

    String errorString = appContext.getString(errorId);
    if (isFatal) {
      service.stopForeground(true);
      isForeground = false;
    }

    showNotification(details.title, errorString,
        DOWNLOADING_ERROR_NOTIFICATION, false, false);

    String state = isFatal? ProgressIntent.STATE_ERROR :
        ProgressIntent.STATE_ERROR_WILL_RETRY;

    // send broadcast
    Intent progressIntent = new Intent(ProgressIntent.INTENT_NAME);
    progressIntent.putExtra(ProgressIntent.NAME, details.title);
    progressIntent.putExtra(ProgressIntent.DOWNLOAD_KEY, details.key);
    progressIntent.putExtra(ProgressIntent.DOWNLOAD_TYPE, details.type);
    progressIntent.putExtra(ProgressIntent.STATE, state);
    progressIntent.putExtra(ProgressIntent.ERROR_CODE, errorCode);
    broadcastManager.sendBroadcast(progressIntent);
    return progressIntent;
  }

  public void notifyDownloadStarting(){
    String title = appContext.getString(R.string.downloading_title);
    notificationManager.cancel(DOWNLOADING_ERROR_NOTIFICATION);

    lastMaximum = -1;
    lastProgress = -1;
    showNotification(title, appContext.getString(R.string.downloading_message),
        DOWNLOADING_NOTIFICATION, true, true);
  }

  public void stopForeground() {
    if (isForeground) {
      service.stopForeground(true);
      isForeground = false;
    } else {
      notificationManager.cancel(DOWNLOADING_NOTIFICATION);
    }
  }

  private void showNotification(String titleString,
      String statusString, int notificationId, boolean isOnGoing, boolean shouldForeground) {
    showNotification(titleString, statusString, notificationId,
        isOnGoing, 0, 0, false, shouldForeground);
  }

  private void showNotification(String titleString,
      String statusString, int notificationId, boolean isOnGoing,
      int maximum, int progress, boolean isIndeterminate, boolean shouldForeground) {
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID);
    builder.setSmallIcon(R.drawable.ic_notification)
        .setColor(notificationColor)
        .setAutoCancel(true)
        .setOngoing(isOnGoing)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setContentTitle(titleString)
        .setContentText(statusString);

    boolean wantProgress = maximum > 0 && maximum >= progress;
    if (lastProgress == progress && lastMaximum == maximum) {
      // don't keep sending repeat notifications
      return;
    }
    lastProgress = progress;
    lastMaximum = maximum;

    if (wantProgress) {
      builder.setProgress(maximum, progress, isIndeterminate);
    }

    Intent notificationIntent = new Intent(appContext, QuranDataActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(appContext, 0, notificationIntent, 0);
    builder.setContentIntent(contentIntent);

    try {
      if (shouldForeground) {
        service.startForeground(notificationId, builder.build());
        isForeground = true;
       } else {
        notificationManager.notify(notificationId, builder.build());
      }
    } catch (SecurityException se) {
      FirebaseCrashlytics.getInstance().recordException(se);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private void setupNotificationChannel() {
    final String channelName = appContext.getString(R.string.notification_channel_download);
    NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
        channelName, NotificationManager.IMPORTANCE_LOW);
    if (notificationManager.getNotificationChannel(channelName) == null) {
      notificationManager.createNotificationChannel(channel);
    }
  }
}
