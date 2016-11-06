package com.HMSolutions.thikrallah;


import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import org.json.JSONException;
import org.json.JSONObject;

import com.HMSolutions.thikrallah.Fragments.MainFragment;
import com.HMSolutions.thikrallah.Fragments.QuranFragment;
import com.HMSolutions.thikrallah.Fragments.ThikrFragment;
import com.HMSolutions.thikrallah.Fragments.TutorialFragment;
import com.HMSolutions.thikrallah.Notification.MyAlarmsManager;
import com.HMSolutions.thikrallah.Utilities.AppRater;
import com.HMSolutions.thikrallah.Utilities.MainInterface;
import com.HMSolutions.thikrallah.Utilities.MyDBHelper;
import com.HMSolutions.thikrallah.Utilities.PrayTime;
import com.HMSolutions.thikrallah.Utilities.WhatsNewScreen;

import com.HMSolutions.thikrallah.Utilities.inapp.Security;
import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
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
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TimingLogger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends Activity implements MainInterface, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, android.location.LocationListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2334;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_FOR_LOCATION_UPDATES = 5678;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_FOR_STOP_LOCATION_UPDATES = 7896;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS =7051 ;
    private String appLink;
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

    private InterstitialAd interstitial;


    SharedPreferences mPrefs;
    Activity activity = this;
    private static boolean mIsPremium = false;
    private final static String SKU_PREMIUM = "premiumupgrade";
    private String base64RSAPublicKey = "";
    static final int RC_REQUEST = 9648253;
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
            QuranFragment fragment = (QuranFragment) this.getFragmentManager().findFragmentByTag("QuranFragment");
            if (fragment != null && fragment.isVisible()) {
                Log.d(TAG, "visible");
                fragment.setCurrentlyPlaying(position);
            }
            return;
        } else {
            ThikrFragment fragment = (ThikrFragment) this.getFragmentManager().findFragmentByTag("ThikrFragment");
            if (fragment != null && fragment.isVisible()) {
                fragment.setCurrentlyPlaying(position);
            }
        }

    }

    private ServiceConnection mConnectionMediaServer = new ServiceConnection() {
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

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            unbindtoMediaService();
            mServiceThikrMediaPlayerMessenger = null;
            mIsBoundMediaService = false;
            Log.d(TAG, "Disconnected. unbided? mIsBoundMediaService set to false");
        }
    };
    IInAppBillingService mServiceInAppBilling;

    ServiceConnection mServiceInAppBillingConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceInAppBilling = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            Log.d(TAG, "service connected");
            mServiceInAppBilling = IInAppBillingService.Stub.asInterface(service);
            isPremiumPurchasedAsync();
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


    private String deviceId;
    private AdListener adsListener;


    @Override
    protected void onStart() {
        timeOperation("timing", "onstart called");
        Log.d(TAG, "onStart called");
        mGoogleApiClient.connect();

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
            this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
            bindtoMediaService();
            this.requestMediaServiceStatus();

        }

    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();

    }

    @Override
    public void requestLocationUpdate() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_FOR_LOCATION_UPDATES);
        } else {
            if (locationManager == null) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    && PreferenceManager.getDefaultSharedPreferences(this).getString("latitude", "0.0").equalsIgnoreCase("0.0")) {
                buildAlertMessageNoGps();
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d(TAG, "requesting gps");
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                try {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("latitude", Double.toString(location.getLatitude())).commit();
                        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("longitude", Double.toString(location.getLongitude())).commit();
                    }

                } catch (IllegalArgumentException e) {

                }

            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Log.d(TAG, "requesting network");
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                try {
                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("latitude", Double.toString(location.getLatitude())).commit();
                        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("longitude", Double.toString(location.getLongitude())).commit();
                    }
                } catch (IllegalArgumentException e) {

                }
            }
            if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                Log.d(TAG, "requesting passive");
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, this);
                try {
                    Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    if (location != null) {
                        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("latitude", Double.toString(location.getLatitude())).commit();
                        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("longitude", Double.toString(location.getLongitude())).commit();
                    }
                } catch (IllegalArgumentException e) {

                }
            }

        }


    }

    private void requestPermissions(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 9999);
            }
        }

        int mediacontrolPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.MEDIA_CONTENT_CONTROL);

        int writePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (mediacontrolPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.MEDIA_CONTENT_CONTROL);
        }
        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

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

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            Log.d(TAG, "mGoogleApiClient is null");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            // mGoogleApiClient.reconnect();
        } else {
            Log.d(TAG, "mGoogleApiClient is not null");
        }
        timeOperation("timing", "defined_mgoogleApiClient");
        base64RSAPublicKey = getResources().getText(R.string.base64RSAPublicKey).toString();
        deviceId = "AC73D67B1C23A45BBDFCAF3F4040A0AA";//md5(android_id).toUpperCase();


        adsListener = new myAdListener(this);
        appLink = "\n" + this.getResources().getString(R.string.app_link);


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


        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(getResources().getText(R.string.ad_unit_id_interstital).toString());
        interstitial.setAdListener(adsListener);

        timeOperation("timing", "defining intersittal ad");


        if (mPrefs.getBoolean("isPremium", false) == true || doesAdShowBasedOnClicks() == false) {
            Log.d(TAG, "ad hide" + doesAdShowBasedOnClicks());
            hideAd();
        } else {
            //show banner ad
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showAd();
                    timeOperation("timing", "ad showed");

                    //load interstital ad
                    Log.d(TAG, "ad show");
                    // Create the interstitial.
                    //String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

                    // Create ad request.
                    AdRequest adRequest = new AdRequest.Builder()
                            .addTestDevice(deviceId)
                            .build();
                    timeOperation("timing", "add built");

                    // Begin loading your interstitial.
                    interstitial.loadAd(adRequest);
                    timeOperation("timing", "ad loaded");
                }
            }, 3000);


        }


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
                Bundle data = new Bundle();
                data.putString("DataType", intent.getExtras().getString("DataType"));
                data.putInt("surat", Integer.parseInt(intent.getExtras().getString("DataType").split("/")[1]));
                //data.putInt("surat", this.getResources().getIntArray(R.array.surat_values)[0]);
                launchFragment(new QuranFragment(), data, "QuranFragment");
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

    private void timeOperation(String tag, String operation) {
        endnow = android.os.SystemClock.elapsedRealtime();
        Log.d(tag, "Execution time: " + (endnow - startnow) + " ms for " + operation);
        //Log.d(tag,operation);
        startnow = android.os.SystemClock.elapsedRealtime();
    }

    private void isPremiumPurchasedAsync() {
        new isPremiumPurchased().execute();

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

    private void hideAd() {
        final AdView adLayout = (AdView) findViewById(R.id.adView);
        adLayout.setVisibility(View.GONE);

    }

    private void showAd() {

        AdView adView = (AdView) this.findViewById(R.id.adView);
        adView.setAdListener(adsListener);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(deviceId)
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        timeOperation("timing", "banner ad set");

        adView.loadAd(adRequest);
        timeOperation("timing", "banner ad loaded");

    }

    // Invoke displayInterstitial() when you are ready to display an interstitial.
    public void displayInterstitial() {
        if (interstitial.isLoaded() && (mPrefs.getBoolean("isPremium", false) == false)) {
            long timeAtLastAd = mPrefs.getLong("time_at_last_ad", 0);
            if ((System.currentTimeMillis() - timeAtLastAd) >= 5 * 60 * 1000) {
                //1 interstital ad no less than 5 minutes apart
                if (doesAdShowBasedOnClicks() == true) {
                    mPrefs.edit().putLong("time_at_last_ad", System.currentTimeMillis()).commit();
                    interstitial.show();
                }

            }
        }
    }

    private boolean doesAdShowBasedOnClicks() {
        if (mPrefs.getBoolean("isPremium", false) == true) {
            Log.d(TAG, "user is premium");
            return false;
        }
        long timeAtLastAd = mPrefs.getLong("time_at_last_click", 0);
        if ((System.currentTimeMillis() - timeAtLastAd) < 14 * 24 * 60 * 60 * 1000) {
            Log.d(TAG, "time since last ad=" + (System.currentTimeMillis() - timeAtLastAd));
            return false;
        }
        return true;
    }

    @Override
    public void launchFragment(Fragment iFragment, Bundle args, String tag) {
        iFragment.setArguments(args);
        android.app.FragmentTransaction fragmentTransaction1 = this.getFragmentManager().beginTransaction();
        fragmentTransaction1.replace(R.id.container, iFragment, tag);
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
    public void upgrade() {
        Bundle buyIntentBundle = null;
        if (mServiceInAppBilling != null) {
            try {
                buyIntentBundle = mServiceInAppBilling.getBuyIntent(3, getPackageName(),
                        String.valueOf(SKU_PREMIUM), "inapp", String.valueOf(RC_REQUEST));
                if (buyIntentBundle != null) {
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    if (pendingIntent != null) {
                        startIntentSenderForResult(pendingIntent.getIntentSender(),
                                RC_REQUEST, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                                Integer.valueOf(0));
                    }

                }

            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }


        // mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST, mPurchaseFinishedListener, "");

    }

    @Override
    public void onPause() {
        stopLocationUpdates();
        unbindtoMediaService();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        //if (mServiceInAppBilling != null) {
        try {
            unbindService(mServiceInAppBillingConn);
        } catch (Exception e) {
            Log.d(TAG, "exception caught. null service");
        }
        super.onPause();

    }

    @Override
    protected void onResume() {
        timeOperation("timing", "onresume started");
        bindtoMediaService();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        prefListener = this;
        prefs.registerOnSharedPreferenceChangeListener(prefListener);


        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceInAppBillingConn, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "oncreate 6");
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
        this.displayInterstitial();
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

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, " RC_REQUEST  " + RC_REQUEST + "results ok " + RESULT_OK);
        // Pass on the activity result to the helper for handling
        if (requestCode == RC_REQUEST) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
            if (resultCode == RESULT_OK && responseCode == 0) {

                try {

                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    if (Security.verifyPurchase(base64RSAPublicKey, dataSignature, String.valueOf(RC_REQUEST)) && sku.equalsIgnoreCase(SKU_PREMIUM)) {

                        mPrefs.edit().putBoolean("isPremium", true).commit();
                        Log.d(TAG,"premium true");
                        this.hideAd();
                    }
                    //alert("You have bought the " + sku + ". Excellent choice,adventurer!");
                } catch (JSONException e) {
                    // alert("Failed to parse purchase data.");
                    e.printStackTrace();
                }
            }

        }

    }

    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed " + connectionResult.toString());
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            Log.d(TAG, "mLastLocation is not null");
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("latitude", Double.toString(mLastLocation.getLatitude())).commit();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("longitude", Double.toString(mLastLocation.getLongitude())).commit();
            Log.d(TAG, "latitude is " + Double.toString(mLastLocation.getLatitude()));
            PrayTime.instancePrayTime(this);
            new MyAlarmsManager(this).UpdateAllApplicableAlarms();
        } else {
            if (PreferenceManager.getDefaultSharedPreferences(this).getString("latitude", "0.0").equalsIgnoreCase("0.0")) {
                Log.d(TAG, "mLastLocation is null");
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                }


                locationRequest = LocationRequest.create();

                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                locationRequest.setInterval(5000);
                locationRequest.setNumUpdates(3);
                locationRequest.setExpirationDuration(1000 * 30);
                locationRequest.setFastestInterval(1000);
                int permissionCheck = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                } else {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                }


            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )) {
                    //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,  this);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_FOR_LOCATION_UPDATES:{
                if (grantResults.length > 0
                        && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )) {
                    this.requestLocationUpdate();
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION_FOR_STOP_LOCATION_UPDATES:{
                if (grantResults.length > 0
                        && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )) {
                    this.stopLocationUpdates();
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
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
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
            if (mGoogleApiClient.isConnected()){
                LocationServices.FusedLocationApi.removeLocationUpdates(
                        mGoogleApiClient, this);
            }
            if(locationManager!=null){
                locationManager.removeUpdates(this);
            }
        }


    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG,"onLocationChanged called requesting with provider ="+location.getProvider().toString());
        Log.d(TAG,"latitude is "+Double.toString(location.getLatitude()));
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("latitude", Double.toString(location.getLatitude())).commit();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("longitude", Double.toString(location.getLongitude())).commit();
        stopLocationUpdates();

    }
    private class updateLocationDiscription extends AsyncTask<Context, String, String> {


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

    class myAdListener extends com.google.android.gms.ads.AdListener{
        MainActivity myActivity;
        public myAdListener(MainActivity context){
            super();
            myActivity=context;
        }
        @Override
        public void onAdOpened() {
            // this
            Log.d(TAG,"ad openned");
            //mPrefs.edit().putLong("time_at_last_click", System.currentTimeMillis()).commit();
            super.onAdOpened();
        }
        @Override
        public void onAdClosed(){
            super.onAdClosed();
        }
        @Override
        public void onAdLeftApplication(){
            Log.d(TAG,"ad left application");
            myActivity.hideAd();
            mPrefs.edit().putLong("time_at_last_click", System.currentTimeMillis()).commit();
            super.onAdLeftApplication();

        }
    }



    private class isPremiumPurchased extends AsyncTask<Void, Void, Boolean> {
        int response;

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Log.d(TAG,"isPremiumPurchased called");
                Bundle ownedItems = mServiceInAppBilling.getPurchases(3, getPackageName(), "inapp", null);
                response = ownedItems.getInt("RESPONSE_CODE");
                Log.d(TAG,"response ="+response);
                if (response == 0) {
                    ArrayList<String> ownedSkus =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    ArrayList<String>  purchaseDataList =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                    ArrayList<String>  signatureList =
                            ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                    String continuationToken =
                            ownedItems.getString("INAPP_CONTINUATION_TOKEN");
                    Log.d(TAG,"ownedSkus ="+ownedSkus.size());
                    for (int i = 0; i < purchaseDataList.size(); ++i) {

                        String purchaseData = purchaseDataList.get(i);
                        String signature = signatureList.get(i);
                        String sku = ownedSkus.get(i);
                        Log.d(TAG,"sku ="+sku);
                        Log.d(TAG,"base64"+signature);
                        Log.d(TAG,"base64"+base64RSAPublicKey);
                        Log.d(TAG,"Is signature valid? "+Security.verifyPurchase(base64RSAPublicKey,String.valueOf(RC_REQUEST),signature));
                        Log.d(TAG,"Is premium sku? "+sku.equalsIgnoreCase(SKU_PREMIUM));
                        if(Security.verifyPurchase(base64RSAPublicKey, signature, String.valueOf(RC_REQUEST)) && sku.equalsIgnoreCase(SKU_PREMIUM)) {

                            Log.d(TAG, "ispremium true ");
                            Log.d(TAG, "base64 matches");
                            return true;

                        }
                    }

                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return false;
        }

        protected void onProgressUpdate(Void...progress) {
        }

        protected void onPostExecute(Boolean isPremium) {
            if (isPremium==true){
                mPrefs.edit().putBoolean("isPremium", true).commit();
                hideAd();
            }else{
                switch (response){
                    case 1://BILLING_RESPONSE_RESULT_USER_CANCELED
                        Toast.makeText(mcontext,"User cancelled",Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        Toast.makeText(mcontext,"No Internet",Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        Toast.makeText(mcontext,"Billing API version is not supported for the type requested",Toast.LENGTH_LONG).show();
                        break;
                    case 4:
                        Toast.makeText(mcontext,"Product not available",Toast.LENGTH_LONG).show();
                        break;
                    case 5:
                        Toast.makeText(mcontext,"developer error. App not signed or not properly setup",Toast.LENGTH_LONG).show();
                        break;
                    case 6:
                        Toast.makeText(mcontext,"Fatal error during the API action",Toast.LENGTH_LONG).show();
                        break;
                    case 7:
                        Toast.makeText(mcontext,"Product already owned",Toast.LENGTH_LONG).show();
                        mPrefs.edit().putBoolean("isPremium", true).commit();
                        hideAd();
                        break;
                    case 8:
                        Toast.makeText(mcontext,"Failure to consume since item is not owned",Toast.LENGTH_LONG).show();
                        break;
                }
            }
            return;
        }
    }


}


