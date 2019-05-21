package com.HMSolutions.thikrallah;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.HMSolutions.thikrallah.Fragments.MainFragment;
import com.HMSolutions.thikrallah.Fragments.ThikrFragment;
import com.HMSolutions.thikrallah.Fragments.TutorialFragment;
import com.HMSolutions.thikrallah.Notification.AthanTimerService;
import com.HMSolutions.thikrallah.Utilities.AppRater;
import com.HMSolutions.thikrallah.Utilities.MainInterface;
import com.HMSolutions.thikrallah.Utilities.MyDBHelper;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity implements MainInterface, LocationListener, android.location.LocationListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 2334;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION_FOR_LOCATION_UPDATES = 5678;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS =7051 ;

    String TAG = "MainActivity";
    public static final String DATA_TYPE_NIGHT_THIKR = "night";
    public static final String DATA_TYPE_DAY_THIKR = "morning";
    public static final String DATA_TYPE_GENERAL_THIKR = "general";
    public static final String DATA_TYPE_QURAN_KAHF = "quran/0";
    public static final String DATA_TYPE_QURAN_MULK = "quran/1";
    public static final String DATA_TYPE_QURAN = "quran";
    public static final String DATA_TYPE_ATHAN = "athan";
    public static final String DATA_TYPE_ATHAN1 = "athan1";
    public static final String DATA_TYPE_ATHAN2 = "athan2";
    public static final String DATA_TYPE_ATHAN3 = "athan3";
    public static final String DATA_TYPE_ATHAN4 = "athan4";
    public static final String DATA_TYPE_ATHAN5 = "athan5";


    private static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"))};
    SharedPreferences mPrefs;
    Activity activity = this;
    private String base64RSAPublicKey = "";
    static final int RC_REQUEST = 9648253;
    static  final int RC_ENABLE_LOCATION_SETTINGS=7866755;
    private Context mcontext;

    Messenger mServiceThikrMediaPlayerMessenger = null;
    boolean mIsBoundMediaService;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private LocationManager locationManager;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    private long endnow;
    private long startnow = 0;


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equalsIgnoreCase("latitude") || key.equalsIgnoreCase("longitude")) {
            // new updateLocationDiscription().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,this);

        }
    }



    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "message recieved what=" + msg.what + "arg1=" + msg.arg1);
            switch (msg.what) {
                case ThikrMediaPlayerService.MSG_CURRENT_PLAYING:
                    Log.d(TAG, "position" + msg.arg1);
                    sendPositionToThikrFragment(msg.arg1, msg.getData());
                    break;
                case ThikrMediaPlayerService.MSG_UNBIND:
                    unbindtoMediaService();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendPositionToThikrFragment(int position, Bundle data) {
        String datatype = data.getString("com.HMSolutions.thikrallah.datatype", null);
        Log.d(TAG, "datatype=" + datatype);
        if (datatype == null) {
            return;
        }
        if (datatype.contains(DATA_TYPE_QURAN)) {
            Log.d(TAG, "quran");

        } else {
            ThikrFragment fragment = (ThikrFragment) this.getFragmentManager().findFragmentByTag("ThikrFragment");
            if (fragment != null && fragment.isVisible()) {
                fragment.setCurrentlyPlaying(position);
            }
        }

    }

    private ServiceConnection mConnectionMediaServer = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mServiceThikrMediaPlayerMessenger = new Messenger(service);
            mIsBoundMediaService = true;
            Log.d(TAG, "connected. binded? mIsBoundMediaService set to true");
            try {
                Message msg = Message.obtain(null, ThikrMediaPlayerService.MSG_CURRENT_PLAYING);
                msg.replyTo = mMessenger;
                mServiceThikrMediaPlayerMessenger.send(msg);
                requestMediaServiceStatus();
                Log.d(TAG, "requested status");
            } catch (RemoteException e) {

            }
        }
        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            unbindtoMediaService();
            mServiceThikrMediaPlayerMessenger = null;
            mIsBoundMediaService = false;
            Log.d(TAG, "Disconnected. unbided? mIsBoundMediaService set to false");
        }
    };


    @Override
    public void requestMediaServiceStatus() {
        if (mIsBoundMediaService) {
            if (mServiceThikrMediaPlayerMessenger != null) {
                try {
                    Log.d(TAG, "requestMediaServiceStatus called to request message status");
                    Message msg = Message.obtain(null, ThikrMediaPlayerService.MSG_CURRENT_PLAYING, 0, 0);
                    msg.replyTo = mMessenger;
                    mServiceThikrMediaPlayerMessenger.send(msg);
                } catch (RemoteException e) {
                }
            }
        } else {
            Log.d(TAG, "mIsBoundMediaService is false to send message");
        }
    }

    private void unbindtoMediaService() {
        // unbind to the service
        Log.d(TAG, "unbind called. mIsBoundMediaService =" + mIsBoundMediaService);
        if (mIsBoundMediaService == true) {
            unbindService(mConnectionMediaServer);
        }


        mIsBoundMediaService = false;

    }

    private void bindtoMediaService() {
        // Bind to the service
        if (!mIsBoundMediaService) {
            try {
                mIsBoundMediaService = bindService(new Intent(this, ThikrMediaPlayerService.class), mConnectionMediaServer,
                        Context.BIND_ABOVE_CLIENT);

            } catch (Exception e) {
                mIsBoundMediaService = false;
            }

        }
        Log.d(TAG, "bind called. mIsBoundMediaService" + mIsBoundMediaService);
    }





    @Override
    protected void onStart() {
        timeOperation("timing", "onstart called");
        Log.d(TAG, "onStart called");


        bindtoMediaService();
        timeOperation("timing", "onstart finished");
        super.onStart();


    }

    public void sendActionToMediaService(Bundle data) {
        Log.d(TAG, "sendActionToMediaService called");
        if (data != null) {
            Log.d(TAG, "data is not null");
            if (data.getString("com.HMSolutions.thikrallah.datatype", "").equalsIgnoreCase("")) {
                Log.d(TAG, "datatype was empty");
                data.putString("com.HMSolutions.thikrallah.datatype", this.getThikrType());
                Log.d(TAG, "datatype assigned to " + this.getThikrType());
            }

            data.putBoolean("isUserAction", true);
            Log.d(TAG, "service to start");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
            } else {
                this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
            }

            bindtoMediaService();
            this.requestMediaServiceStatus();

        }

    }

    @Override
    protected void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
        Log.d(TAG,"onstop finished");
    }

    @Override
    public void requestLocationUpdate() {
        Log.d(TAG,"requestLocationUpdate");
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_LOCATION_FOR_LOCATION_UPDATES);
        } else {
            if (locationManager == null) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }
            String provider = locationManager.getBestProvider(criteria, true);
            if (provider==null){
                if (PreferenceManager.getDefaultSharedPreferences(this).getString("latitude", "0.0").equalsIgnoreCase("0.0")){
                    buildAlertMessageNoGps();
                }

                return;
            }else {


                Location location = locationManager.getLastKnownLocation(provider);
                locationManager.requestLocationUpdates(provider, 0, 0, this);
                Log.d(TAG, "requesting best provider: " + provider);

                if (location != null) {

                    PreferenceManager.getDefaultSharedPreferences(this).edit().putString("latitude", Double.toString(location.getLatitude())).commit();
                    PreferenceManager.getDefaultSharedPreferences(this).edit().putString("longitude", Double.toString(location.getLongitude())).commit();
                }
                Log.d(TAG, "isproviderenabled" + locationManager.isProviderEnabled(provider));
                if ((!locationManager.isProviderEnabled(provider) || !isLocationEnabled(this)) &&
                        PreferenceManager.getDefaultSharedPreferences(this).getString("latitude", "0.0").equalsIgnoreCase("0.0")) {
                    buildAlertMessageNoGps();
                }
            }
        }


    }
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }
    private void requestPermissions(){
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 9999);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            int foregroundservice_permission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.FOREGROUND_SERVICE);
            if (foregroundservice_permission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.FOREGROUND_SERVICE);
                Log.d(TAG,"forground_service permission requested");
            }
        }


        int mediacontrolPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.MEDIA_CONTENT_CONTROL);

        int locationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        //ask for this when needed and not here
       // int writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        if (mediacontrolPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.MEDIA_CONTENT_CONTROL);
        }
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        int PhoneStatePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE);
        if (PhoneStatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }

       /* if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
*/

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
        }




        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SYSTEM_ALERT_WINDOW);
        Log.d(TAG,"SYSTEM_ALERT_WINDOW permission ="+permissionCheck);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"SYSTEM_ALERT_WINDOW permission requested");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SYSTEM_ALERT_WINDOW},
                    1);
        }

        permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.MEDIA_CONTENT_CONTROL);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.MEDIA_CONTENT_CONTROL},
                    1);
        }




    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        requestPermissions();
        timeOperation("timing", "oncreate_started");
        Log.d(TAG, "oncreate 1");
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        PreferenceManager.setDefaultValues(this.getApplicationContext(), R.xml.preferences, true);
        PreferenceManager.setDefaultValues(this.getApplicationContext(), R.xml.preferences_athan, true);
        PreferenceManager.setDefaultValues(this.getApplicationContext(), R.xml.preferences_general, true);
        mcontext = this.getApplicationContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang = mPrefs.getString("language", null);
        Log.d(TAG, "oncreate 2");
        timeOperation("timing", "minor_setups");
        if (lang != null) {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
            Log.d(TAG, "oncreate 3");

        }
        timeOperation("timing", "locale_setup_if_needed");
        Log.d(TAG, "oncreate 4");


        timeOperation("timing", "defined_mgoogleApiClient");



        populateBuiltinDatabase();
        timeOperation("timing", "populating builtin database");


        Log.d(TAG, "oncreate 7");

        Intent intent1 = new Intent("com.HMSolutions.thikrallah.Notification.ThikrBootReceiver.android.action.broadcast");


        //new WhatsNewScreen(this).show();

        timeOperation("timing", "showing what's new screen");

        Log.d(TAG, "oncreate 8");
        AppRater.app_launched(this);
        timeOperation("timing", "launching apprater if applicable");
        setContentView(R.layout.activity_main);
        timeOperation("timing", "setting content");
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new MainFragment()).commit();
        }
        timeOperation("timing", "replacing with main fragment");

        if (mPrefs.getBoolean("isFirstLaunch", true)) {
            Log.d(TAG, "first launch. calling boot recbiever");
            sendBroadcast(intent1);
            mPrefs.edit().putLong("time_at_last_ad", System.currentTimeMillis()).commit();
            launchFragment(new TutorialFragment(), null, "TutorialFragment");
            timeOperation("timing", "launching tutorial freagment");
        }
        if(!mPrefs.getBoolean("protected",false)) {
            for (final Intent intent : POWERMANAGER_INTENTS)
                if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(this.getResources().getString(R.string.autostart)).setMessage(this.getResources().getString(R.string.autostart_message))
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    try{
                                        startActivity(intent);
                                    }catch(Exception e){
                                        e.printStackTrace();
                                        Log.d(TAG,e.getMessage());
                                    }finally{
                                        mPrefs.edit().putBoolean("protected", true).apply();
                                    }



                                }
                            })
                            .setCancelable(false)
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create().show();
                    break;
                }
        }


        boolean isTimer=mPrefs.getBoolean("foreground_athan_timer",true);

        if(isTimer){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this,AthanTimerService.class));
            } else {
                this.startService(new Intent(this,AthanTimerService.class));
            }
        }




        timeOperation("timing", "defining intersittal ad");




        Intent intent = this.getIntent();
        boolean isNotification = intent.getBooleanExtra("FromNotification", false);
        if (isNotification == true) {
            Log.d(TAG, "from notification");
            if (intent.getExtras().getString("DataType").equalsIgnoreCase(MainActivity.DATA_TYPE_DAY_THIKR) ||
                    intent.getExtras().getString("DataType").equalsIgnoreCase(MainActivity.DATA_TYPE_NIGHT_THIKR)) {
                Log.d(TAG, "general thikr notification");
                launchFragment(new ThikrFragment(), intent.getExtras(), "ThikrFragment");
            }
            if (intent.getExtras().getString("DataType").contains(MainActivity.DATA_TYPE_QURAN)) {
                Log.d(TAG, "quran thikr notification");
                //Bundle data = new Bundle();
                //data.putString("DataType", intent.getExtras().getString("DataType"));
                //data.putInt("surat", Integer.parseInt(intent.getExtras().getString("DataType").split("/")[1]));
                //data.putInt("surat", this.getResources().getIntArray(R.array.surat_values)[0]);
                //launchFragment(new QuranFragment(), data, "QuranFragment");
            }
        }
        boolean isFromSettings = intent.getBooleanExtra("FromPreferenceActivity", false);
        if (isFromSettings == true) {
            intent = new Intent();
            intent.setClass(MainActivity.this, PreferenceActivity.class);
            startActivityForResult(intent, 0);
        }
        timeOperation("timing", "remainder of oncreate ");


    }

    private void timeOperation(String mytag, String operation) {
        endnow = SystemClock.elapsedRealtime();
        Log.d(mytag, "Execution time: " + (endnow - startnow) + " ms for " + operation);
        //Log.d(tag,operation);
        startnow = SystemClock.elapsedRealtime();
    }



    private void populateBuiltinDatabase() {
        MyDBHelper db = new MyDBHelper(this);
        db.getReadableDatabase();
        db.close();
        if (db.getAllBuiltinThikrs().size() == 0) {
            db.populateInitialThikr();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, PreferenceActivity.class);
            startActivityForResult(intent, 0);
            return true;
        }
        if (id == R.id.menu_share) {
            share();
            //mCallback.shareToFacebook(DBHelper.getInstance(this.getActivity()).getHadithTextforPageCurlView(v.getHadithsIdList(), v.getmIndex()));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void launchFragment(Fragment iFragment, Bundle args, String mytag) {
        iFragment.setArguments(args);
        FragmentTransaction fragmentTransaction1 = this.getFragmentManager().beginTransaction();
        fragmentTransaction1.replace(R.id.container, iFragment, mytag);
        fragmentTransaction1.addToBackStack(null);
        fragmentTransaction1.commit();

    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }
    @Override
    public void share() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, this.getResources().getText(R.string.share_text));
        startActivity(Intent.createChooser(shareIntent, this.getResources().getString(R.string.share)));
    }



    @Override
    public void onPause() {
        Log.d(TAG,"on pause started");
        stopLocationUpdates();
        unbindtoMediaService();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        Log.d(TAG,"on pause finished");
        super.onPause();
        Log.d(TAG,"on pause finished on parent");

    }

    @Override
    protected void onResume() {
        timeOperation("timing", "onresume started");
        bindtoMediaService();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        prefListener = this;
        prefs.registerOnSharedPreferenceChangeListener(prefListener);



        timeOperation("timing", "onresume finished");


        super.onResume();

    }

    @Override
    protected void onDestroy() {

        unbindtoMediaService();

        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void playAll(String AssetFolder) {
        Bundle data = new Bundle();
        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAYALL);
        data.putString("com.HMSolutions.thikrallah.datatype", AssetFolder);
        sendActionToMediaService(data);
    }

    @Override
    public void incrementCurrentPlaying(String AssetFolder, int i) {
        Bundle data = new Bundle();
        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_INNCREMENT);
        data.putInt("INCREMENT", i);
        data.putString("com.HMSolutions.thikrallah.datatype", AssetFolder);
        sendActionToMediaService(data);
    }

    @Override
    public void pausePlayer(String thikrtype) {
        Log.d(TAG, "pauseplayer called");
        Bundle data = new Bundle();
        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PAUSE);
        data.putString("com.HMSolutions.thikrallah.datatype", thikrtype);
        sendActionToMediaService(data);

    }

    @Override
    public void play(String AssetFolder, int fileNumber) {
        Bundle data = new Bundle();
        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAY);
        data.putInt("FILE", fileNumber);
        data.putString("com.HMSolutions.thikrallah.datatype", AssetFolder);
        sendActionToMediaService(data);

    }

    @Override
    public void play(String path) {
        Bundle data = new Bundle();
        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAY);
        data.putInt("FILE", -1);
        data.putString("com.HMSolutions.thikrallah.datatype", DATA_TYPE_GENERAL_THIKR);
        data.putString("FILE_PATH", path);
        sendActionToMediaService(data);

    }

    @Override
    public boolean isPlaying() {
        //hanihani
        Bundle data = new Bundle();
        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_ISPLAYING);
        sendActionToMediaService(data);
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        return sharedPrefs.getBoolean("ISPLAYING", false);

    }

    @Override
    public void resetPlayer(String thikrtype) {
        Bundle data = new Bundle();
        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_RESET);
        data.putString("com.HMSolutions.thikrallah.datatype", thikrtype);
        sendActionToMediaService(data);


    }

    @Override
    public void setCurrentPlaying(String AssetFolder, int currentPlaying) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        sharedPrefs.edit().putInt("currentPlaying", currentPlaying).commit();

    }

    @Override
    public int getCurrentPlaying() {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        return sharedPrefs.getInt("currentPlaying", 1);
    }

    @Override
    public void setThikrType(String thikrType) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        sharedPrefs.edit().putString("thikrType", thikrType).commit();

    }

    public String getThikrType() {
        return this.mPrefs.getString("thikrType", MainActivity.DATA_TYPE_DAY_THIKR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode==RC_ENABLE_LOCATION_SETTINGS){
            Log.d(TAG,"requestLocationUpdate. Settings enabled");
            this.requestLocationUpdate();
        }

    }

    private Location mLastLocation;
    private LocationRequest locationRequest;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS:{
                if (grantResults.length > 0
                        && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )) {
                    Log.d(TAG,"requestLocationUpdate. permissions granted");

                    this.requestLocationUpdate();
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )) {
                    Log.d(TAG,"requestLocationUpdate. permissions granted");

                    this.requestLocationUpdate();
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION_FOR_LOCATION_UPDATES:{
                if (grantResults.length > 0
                        && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )) {
                    Log.d(TAG,"requestLocationUpdate. permissions granted");

                    this.requestLocationUpdate();
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }



            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.location_services_not_enabled)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),RC_ENABLE_LOCATION_SETTINGS);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    protected void stopLocationUpdates() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            /*
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_FOR_STOP_LOCATION_UPDATES);
            */
        } else {
            if(locationManager!=null){
                locationManager.removeUpdates(this);
            }
        }


    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"onLocationChanged called requesting with provider ="+location.getProvider().toString());
        Log.d(TAG,"latitude is "+Double.toString(location.getLatitude()));
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("latitude", Double.toString(location.getLatitude())).commit();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("longitude", Double.toString(location.getLongitude())).commit();
        stopLocationUpdates();

    }
    private static class updateLocationDiscription extends AsyncTask<Context, String, String> {
        String TAG = "updateLocationDiscription";
        @Override
        protected String doInBackground(Context... context) {
            return updateCity(context[0]);
        }

        private String updateCity(Context context){

            double latitude=Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(context).getString("latitude","0.0"));
            double longitude=Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(context).getString("longitude","0.0"));
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses;
            String locationDiscription="";
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
//                String cityName = addresses.get(0).getAddressLine(0);
                //un hg              String stateName = addresses.get(0).getAddressLine(1);
                //       String countryName = addresses.get(0).getAddressLine(2);
                if (addresses.size()>0) {
                    String country=addresses.get(0).getCountryName();
                    String city=addresses.get(0).getLocality();
                    locationDiscription = country+"  "+city;// nbn  khjbmn countryName+" "+stateName + "" + cityName;
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putString("location", locationDiscription).commit();
                }else{
                    // locationDiscription = latitude + ", "+longitude;
                    // PreferenceManager.getDefaultSharedPreferences(context).edit().putString("location", locationDiscription).commit();

                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,e.getMessage());
            }
            return locationDiscription;
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG,"onStatusChanged called");

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }





}


