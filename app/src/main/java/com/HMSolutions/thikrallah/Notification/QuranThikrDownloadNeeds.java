package com.HMSolutions.thikrallah.Notification;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.HMSolutions.thikrallah.BuildConfig;
import com.HMSolutions.thikrallah.R;
import com.HMSolutions.thikrallah.quran.data.page.provider.madani.MadaniPageProvider;
import com.HMSolutions.thikrallah.quran.labs.androidquran.common.QariItem;
import com.HMSolutions.thikrallah.quran.labs.androidquran.dao.audio.AudioPathInfo;
import com.HMSolutions.thikrallah.quran.labs.androidquran.dao.audio.AudioRequest;
import com.HMSolutions.thikrallah.quran.labs.androidquran.data.Constants;
import com.HMSolutions.thikrallah.quran.labs.androidquran.data.SuraAyah;
import com.HMSolutions.thikrallah.quran.labs.androidquran.service.AudioService;
import com.HMSolutions.thikrallah.quran.labs.androidquran.service.QuranDownloadService;
import com.HMSolutions.thikrallah.quran.labs.androidquran.service.util.ServiceIntentHelper;
import com.HMSolutions.thikrallah.quran.labs.androidquran.util.AudioUtils;
import com.HMSolutions.thikrallah.quran.labs.androidquran.util.QuranSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class QuranThikrDownloadNeeds extends BroadcastReceiver {
    String TAG = "QuranThikrDownloadNeeds";
    private static final String QURAN_BASE = "quran_android/";
    private static final String AUDIO_DIRECTORY=new MadaniPageProvider().getAudioDirectoryName();
    private static final String AUDIO_EXTENSION = ".mp3";
    private static final String DATABASE_DIRECTORY=new MadaniPageProvider().getDatabaseDirectoryName();
    private static final String AYAHINFO_DIRECTORY=new MadaniPageProvider().getAyahInfoDirectoryName();
    private static final String DB_EXTENSION = ".db";
    private static final String  ZIP_EXTENSION = ".zip";
    private static final String DATABASE_BASE_URL =new MadaniPageProvider().getAudioDatabasesBaseUrl();
    Context mcontext;
    QuranSettings quransettings;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"onReceive called");
        mcontext=context;
        quransettings=QuranSettings.getInstance(mcontext);
        int sura=intent.getIntExtra("sura",1);
        int ayah=intent.getIntExtra("ayah",1);
        int qari_num=intent.getIntExtra("qari",1);
        SuraAyah start = new SuraAyah(sura, 1);
        SuraAyah end = new SuraAyah(sura, ayah);
        List<QariItem> qlist = getQariList(mcontext);
        QariItem qari=qlist.get(qari_num);

        AudioPathInfo audioPathInfo = this.getLocalAudioPathInfo(qari);
               /*
                File databaseFile=new File(audioPathInfo.getGaplessDatabase());
                if(!databaseFile.exists()){

                    Intent downloadIntent = ServiceIntentHelper.getAudioDownloadIntent(this, getGaplessDatabaseUrl(qari), audioPathInfo.getLocalDirectory(), mcontext.getString(R.string.timing_database));
                    startService(downloadIntent);
                }

*/
        if (audioPathInfo != null) {
            // override streaming if all the files are already downloaded
            boolean stream = false;
            if (quransettings.shouldStream()) {
                stream = !haveAllFiles(audioPathInfo.getUrlFormat(),audioPathInfo.getLocalDirectory(), start, end,qari.isGapless());
            }

            // if we're still streaming, change the base qari format in audioPathInfo
            // to a remote url format (instead of a path to a local directory)
            AudioPathInfo audioPath;
            if (stream) {
                audioPath = audioPathInfo.copy(getQariUrl(qari), audioPathInfo.getLocalDirectory(), audioPathInfo.getGaplessDatabase());

            } else {
                audioPath = audioPathInfo;
                //check if the audio files are available
                if (!haveAllFiles(audioPathInfo.getUrlFormat(),audioPathInfo.getLocalDirectory(), start, end,qari.isGapless())){

                }
            }


            Log.d(TAG, "ready to play Quran");
            if (audioPathInfo != null) {
                AudioRequest audioRequest = new AudioRequest(start, end, qari, 0, 0, true, false, audioPath);

                //TODO:Check for all needed files downloaded yet
                ArrayList<Intent> DownloadIntents=DownloadedNeededFiles(mcontext,audioRequest);
                Log.d(TAG, "DownloadIntents are "+DownloadIntents.size());
                if (DownloadIntents.size()==0){
                    Log.d(TAG, "calling handlePlayback");
                    handlePlayback(audioRequest);
                }else{
                    for (int i=0;i<DownloadIntents.size();i++){
                        Log.d(TAG,"starting intent"+DownloadIntents.get(i));
                        Log.d(TAG,"starting extras"+DownloadIntents.get(i).getExtras().toString());
                        if (i==DownloadIntents.size()-1){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                mcontext.startForegroundService(DownloadIntents.get(i).putExtra("START_AUDIO_REMINDER_SERVICE",true));
                            } else {
                                mcontext.startService(DownloadIntents.get(i).putExtra("START_AUDIO_REMINDER_SERVICE",true));
                            }

                        }else{
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                mcontext.startForegroundService(DownloadIntents.get(i));
                            } else {
                                mcontext.startService(DownloadIntents.get(i));
                            }

                        }

                    }
                }

            }
        }



    }

    private boolean makeQuranDatabaseDirectory(Context context) {
        return makeDirectory(getQuranDatabaseDirectory(context));
    }

    private boolean makeQuranAyahDatabaseDirectory(Context context) {
        return makeQuranDatabaseDirectory(context) &&
                makeDirectory(getQuranAyahDatabaseDirectory(context));
    }
    public String getQuranAyahDatabaseDirectory(Context context) {
        String base = getQuranBaseDirectory(context);
        return base == null ? null : base + File.separator + AYAHINFO_DIRECTORY;
    }
    private boolean makeDirectory(String path) {
        if (path == null) {
            return false;
        }

        File directory = new File(path);
        return (directory.exists() && directory.isDirectory()) || directory.mkdirs();
    }
    public String getQuranDatabaseDirectory(Context context) {
        String base = getQuranBaseDirectory(context);
        return (base == null) ? null : base + DATABASE_DIRECTORY;
    }

    private ArrayList<Intent> DownloadedNeededFiles(Context context, AudioRequest request){
        ArrayList<Intent> downloadIntents=new ArrayList<Intent>();
        QariItem qari = request.getQari();
        AudioPathInfo audioPathInfo = request.getAudioPathInfo();
        String path = audioPathInfo.getLocalDirectory();
        String gaplessDb = audioPathInfo.getGaplessDatabase();
        if (gaplessDb != null && !new File(gaplessDb).exists()) {
            Log.d(TAG,"db download needed");
            Intent DatabaseIntent=getDownloadIntent(context,
                    getGaplessDatabaseUrl(qari),
                    path,
                    context.getString(R.string.timing_database));
            downloadIntents.add(DatabaseIntent);

        }
        if (!request.getShouldStream() &&
                shouldDownloadBasmallah(path,
                        request.getStart(),
                        request.getEnd(),
                        qari.isGapless())) {
             Log.d(TAG,"bismillah download needed");

            String title = getNotificationTitle(
                    context, request.getStart(), request.getStart(), qari.isGapless());
            Intent beslmalahIntent = getDownloadIntent(context, getQariUrl(qari), path, title);

            beslmalahIntent.putExtra(QuranDownloadService.EXTRA_START_VERSE, request.getStart());
            beslmalahIntent.putExtra(QuranDownloadService.EXTRA_END_VERSE, request.getStart());
            downloadIntents.add(beslmalahIntent);


        }
        if (!request.getShouldStream() &&
                !haveAllFiles(audioPathInfo.getUrlFormat(),audioPathInfo.getLocalDirectory(), request.getStart(), request.getEnd(),qari.isGapless())) {

             Log.d(TAG,"audio download needed");
            String title = getNotificationTitle(
                    context, request.getStart(), request.getEnd(), qari.isGapless());
            Intent AudioIntent=getDownloadIntent(context, getQariUrl(qari), path, title);
            AudioIntent.putExtra(QuranDownloadService.EXTRA_START_VERSE, request.getStart());
            AudioIntent.putExtra(QuranDownloadService.EXTRA_END_VERSE, request.getEnd());
            AudioIntent.putExtra(QuranDownloadService.EXTRA_IS_GAPLESS, qari.isGapless());
            downloadIntents.add(AudioIntent);


        }
        return downloadIntents;
    }
    private boolean shouldDownloadBasmallah( String baseDirectory,
                                             SuraAyah start,
                                             SuraAyah end,
                                             Boolean isGapless)  {
        if (isGapless) {
            return false;
        }

        if (!baseDirectory.isEmpty()) {
            File f = new File(baseDirectory);
            if (f.exists()) {
                String filename = "1" + File.separator + 1 + AUDIO_EXTENSION;
                f = new File(baseDirectory + File.separator + filename);
                if (f.exists()) {
                    Timber.d("already have basmalla...");
                    return false;
                }
            } else {
                f.mkdirs();
            }
        }

        return doesRequireBasmallah(start, end);
    }

    private boolean doesRequireBasmallah(SuraAyah minAyah, SuraAyah maxAyah) {
        Timber.d("seeing if need basmalla...");

        for (int i = minAyah.sura; i <= maxAyah.sura; i++) {
            int firstAyah;
            if (i == minAyah.sura) {
                firstAyah = minAyah.ayah;
            } else {
                firstAyah = 1;
            }

            if (firstAyah == 1 && i != 1 && i != 9) {
                return true;
            }
        }

        return false;
    }
    public String getSuraName(Context context, int sura, boolean wantPrefix, boolean wantTranslation) {
        if (sura < Constants.SURA_FIRST ||
                sura > Constants.SURA_LAST) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        String[] suraNames = context.getResources().getStringArray(com.HMSolutions.thikrallah.R.array.sura_names);
        if (wantPrefix) {
            builder.append(context.getString(com.HMSolutions.thikrallah.R.string.quran_sura_title, suraNames[sura - 1]));
        } else {
            builder.append(suraNames[sura - 1]);
        }
        if (wantTranslation) {
            String translation = context.getResources().getStringArray(com.HMSolutions.thikrallah.R.array.sura_names_translation)[sura - 1];
            if (!TextUtils.isEmpty(translation)) {
                // Some sura names may not have translation
                builder.append(" (");
                builder.append(translation);
                builder.append(")");
            }
        }

        return builder.toString();
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

    private Intent getDownloadIntent(Context context,
                                     String url,
                                     String destination,
                                     String title) {
        return ServiceIntentHelper.getAudioDownloadIntent(context, url, destination, title);
    }
    private String getGaplessDatabaseUrl( QariItem qari) {
        if (!qari.isGapless() || qari.getDatabaseName() == null) {
            return null;
        }

        String dbName = qari.getDatabaseName() + ZIP_EXTENSION;
        return DATABASE_BASE_URL + "/" + dbName;
    }
    private String getQariUrl(QariItem item) {
        if (item.isGapless()) {
            return item.getUrl() + "%03d" + AudioUtils.AUDIO_EXTENSION;
        } else {
            return item.getUrl() + "%03d%03d" + AudioUtils.AUDIO_EXTENSION;
        }
    }
    private List<QariItem> getQariList(Context context) {
        Resources resources = context.getResources();
        String[] shuyookh = resources.getStringArray(R.array.quran_readers_name);
        String[]paths = resources.getStringArray(R.array.quran_readers_path);
        String[]urls = resources.getStringArray(R.array.quran_readers_urls);
        String[]databases = resources.getStringArray(R.array.quran_readers_db_name);
        int[] hasGaplessEquivalent = resources.getIntArray(R.array.quran_readers_have_gapless_equivalents);
        List<QariItem> items = new ArrayList<QariItem>();

        for (int i=0;i<shuyookh.length;i++ ) {
            items.add(new QariItem(i, shuyookh[i], urls[i], paths[i], databases[i]));
        }

        return items;
    }
    public void handlePlayback(AudioRequest request) {
        boolean needsPermissionToDownloadOver3g = true;
        final Intent intent = new Intent(mcontext, AudioService.class);
        intent.setAction(AudioService.ACTION_PLAYBACK);

        if (request != null) {
            intent.putExtra(AudioService.EXTRA_PLAY_INFO, request);
            intent.putExtra("isFromService",true);
        }


        Log.d(TAG,"starting service for audio playback");
        mcontext.startService(intent);
    }
    private boolean isSDCardMounted() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public String getQuranBaseDirectory(Context context) {
        String basePath = QuranSettings.getInstance(context).getAppCustomLocation();

        if (!isSDCardMounted()) {
            // if our best guess suggests that we won't have access to the data due to the sdcard not
            // being mounted, then set the base path to null for now.
            if (basePath == null || basePath.equals(
                    this.mcontext.getExternalFilesDir(null).getAbsolutePath()) ||
                    (basePath.contains(BuildConfig.APPLICATION_ID) && context.getExternalFilesDir(null) == null)) {
                basePath = null;
            }
        }

        if (basePath != null) {
            if (!basePath.endsWith(File.separator)) {
                basePath += File.separator;
            }
            return basePath + QURAN_BASE;
        }
        return null;
    }
    private boolean haveAllFiles(String baseUrl,
                                 String path,
                                 SuraAyah start,
                                 SuraAyah end,
                                 Boolean isGapless) {
        if (path.isEmpty()) {
            return false;
        }

        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
            return false;
        }

        int startSura = start.sura;
        int startAyah = start.ayah;

        int endSura = end.sura;
        int endAyah = end.ayah;

        if (endSura < startSura || (endSura == startSura && endAyah < startAyah)) {
            throw new IllegalStateException("End isn't larger than the start");
        }
        int lastAyah;
        int firstAyah;
        for (int i = startSura; i<=endSura;i++) {
            if (i == endSura) {
                lastAyah =endAyah;
            } else {
                lastAyah = getNumAyahs(i) ;
            }
            if (i == startSura) {
                firstAyah = startAyah;
            } else {
                firstAyah = 1;
            }

            if (isGapless) {
                if (i == endSura && endAyah == 0) {
                    continue;
                }
                String fileName = String.format(Locale.US, baseUrl, i);
                Timber.d("gapless, checking if we have %s", fileName);
                f = new File(fileName);
                if (!f.exists()) {
                    return false;
                }
                continue;
            }

            Timber.d("not gapless, checking each ayah...");
            for (int j = firstAyah; j<=lastAyah;j++) {
                String filename = i + File.separator + j + AUDIO_EXTENSION;
                f =new File(path + File.separator + filename);
                if (!f.exists()) {
                    return false;
                }
            }
        }

        return true;
    }
    public int getNumAyahs(int sura) {
        if (sura==67){
            return 30;
        }else if (sura == 18){
            return 110;
        }else{
            return 1;
        }
    }
    @Nullable
    private String getQuranAudioDirectory(Context context){
        String path = getQuranBaseDirectory(context);
        if (path == null) {
            return null;
        }
        path += AUDIO_DIRECTORY;
        File dir = new File(path);
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }
        return path + File.separator;
    }
    private String getLocalQariUrl(Context context,QariItem item) {
        String rootDirectory = getQuranAudioDirectory(mcontext);
        if (rootDirectory == null){
            return null;
        } else{
            return rootDirectory + item.getPath();
        }
    }
    private String getQariDatabasePathIfGapless(Context context,  QariItem item) {
        String databaseName = item.getDatabaseName();
        if (databaseName != null) {
            String path = getLocalQariUrl(context, item);
            if (path != null) {
                databaseName = path + File.separator + databaseName + DB_EXTENSION;
            }
        }
        return databaseName;
    }
    private AudioPathInfo getLocalAudioPathInfo(QariItem item) {
        String databaseName = item.getDatabaseName();
        if (databaseName != null) {
            String localPath = getLocalQariUrl(mcontext, item);
            if (localPath != null) {
                String databasePath = getQariDatabasePathIfGapless(mcontext, item);
                String urlFormat;
                if (databasePath == null || databasePath.isEmpty()) {
                    urlFormat = localPath + File.separator + "%d" + File.separator +
                            "%d" + AudioUtils.AUDIO_EXTENSION;
                } else {
                    urlFormat = localPath + File.separator + "%03d" + AudioUtils.AUDIO_EXTENSION;
                }
                return new AudioPathInfo(urlFormat, localPath, databasePath);
            }
        }
        return null;
    }
}