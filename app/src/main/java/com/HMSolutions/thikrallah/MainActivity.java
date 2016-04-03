package com.HMSolutions.thikrallah;


import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;


import org.json.JSONException;
import org.json.JSONObject;

import com.HMSolutions.thikrallah.Fragments.MainFragment;
import com.HMSolutions.thikrallah.Fragments.ThikrFragment;
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

import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
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

public class MainActivity extends Activity implements MainInterface,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {
	private String appLink;

	public static final String DATA_TYPE_NIGHT_THIKR="night";
	public static final String DATA_TYPE_DAY_THIKR="morning";
	public static final String DATA_TYPE_GENERAL_THIKR="general";
	private InterstitialAd interstitial;


	SharedPreferences mPrefs;
	private static final String TAG = "MainActivity";
	Activity activity=this;
	private static boolean mIsPremium = false;
	private final static String SKU_PREMIUM = "premiumupgrade";
	private String base64RSAPublicKey="";
	static final int RC_REQUEST = 9648253;
    private Context mcontext;

    Messenger mServiceThikrMediaPlayerMessenger = null;
    boolean mIsBoundMediaService;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d("testing321","message recieved what="+msg.what+"arg1="+msg.arg1);
            switch (msg.what) {
                case ThikrMediaPlayerService.MSG_CURRENT_PLAYING:
                    Log.d("testing321","position"+msg.arg1);
                    sendPositionToThikrFragment(msg.arg1);
                    break;
                case ThikrMediaPlayerService.MSG_UNBIND:
                    unbindtoMediaService();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    private void sendPositionToThikrFragment(int position){
        ThikrFragment thikrFragment = (ThikrFragment)this.getFragmentManager().findFragmentByTag("ThikrFragment");
        if (thikrFragment != null && thikrFragment.isVisible()) {
            thikrFragment.setCurrentlyPlaying(position);
        }
    }
    private ServiceConnection mConnectionMediaServer = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mServiceThikrMediaPlayerMessenger = new Messenger(service);
            mIsBoundMediaService=true;
            Log.d("testing123","connected. binded? mIsBoundMediaService set to true");
            try {
                Message msg = Message.obtain(null, ThikrMediaPlayerService.MSG_CURRENT_PLAYING);
                msg.replyTo = mMessenger;
                mServiceThikrMediaPlayerMessenger.send(msg);
                requestMediaServiceStatus();
                Log.d("testing321","requested status");
            }
            catch (RemoteException e) {

            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            unbindtoMediaService();
            mServiceThikrMediaPlayerMessenger = null;
            mIsBoundMediaService=false;
            Log.d("testing123","Disconnected. unbided? mIsBoundMediaService set to false");
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
            Log.d("testing123","service connected");
            mServiceInAppBilling = IInAppBillingService.Stub.asInterface(service);
            isPremiumPurchasedAsync();
        }
    };
    private void requestMediaServiceStatus(){
        if (mIsBoundMediaService) {
            if (mServiceThikrMediaPlayerMessenger != null) {
                try {
                    Message msg = Message.obtain(null, ThikrMediaPlayerService.MSG_CURRENT_PLAYING, 0, 0);
                    msg.replyTo = mMessenger;
                    mServiceThikrMediaPlayerMessenger.send(msg);
                }
                catch (RemoteException e) {
                }
            }
        }
    }

    private void unbindtoMediaService(){
        // unbind to the service
        Log.d("testing123","unbind called. mIsBoundMediaService ="+mIsBoundMediaService);
        if (mIsBoundMediaService==true){
            unbindService(mConnectionMediaServer);
        }


        mIsBoundMediaService=false;

    }
    private void bindtoMediaService(){
        // Bind to the service
        if (mIsBoundMediaService==false){
            try{
                mIsBoundMediaService=bindService(new Intent(this, ThikrMediaPlayerService.class), mConnectionMediaServer,
                        Context.BIND_ABOVE_CLIENT);

            }catch(Exception e){
                mIsBoundMediaService=false;
            }

        }
        Log.d("testing123","bind called. mIsBoundMediaService"+mIsBoundMediaService);
    }



	private String deviceId;
    private AdListener adsListener;



    @Override
	protected void onStart() {
     //   mGoogleApiClient.connect();
        super.onStart();
        bindtoMediaService();

	}
	public void sendActionToMediaService(Bundle data){
		if (data!=null){
            data.putString("com.HMSolutions.thikrallah.datatype", this.getThikrType());
            data.putBoolean("isUserAction", true);
            this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
            bindtoMediaService();
            this.requestMediaServiceStatus();

        }

	}
	@Override
	protected void onStop() {
       // mGoogleApiClient.disconnect();
        super.onStop();

	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        base64RSAPublicKey=getResources().getText(R.string.base64RSAPublicKey).toString();
        deviceId = "AC73D67B1C23A45BBDFCAF3F4040A0AA";//md5(android_id).toUpperCase();


        mcontext=this.getApplicationContext();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String lang=mPrefs.getString("language",null);

        if (lang!=null){
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config,
                    getBaseContext().getResources().getDisplayMetrics());
        }



        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceInAppBillingConn, Context.BIND_AUTO_CREATE);




        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }



		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this.getApplicationContext(), R.xml.preferences, false);
		 
		adsListener=new myAdListener(this);
		appLink="\n"+this.getResources().getString(R.string.app_link);



        populateBuiltinDatabase();



		Intent intent1 = new Intent("com.HMSolutions.thikrallah.Notification.ThikrBootReceiver.android.action.broadcast");
		new WhatsNewScreen(this).show();
		AppRater.app_launched(this);
		if (mPrefs.getBoolean("isFirstLaunch", true)){
			Log.d("thikr", "first launch. calling boot recbiever");
			sendBroadcast(intent1);
			mPrefs.edit().putBoolean("isFirstLaunch", false).commit();
			mPrefs.edit().putLong("time_at_last_ad",System.currentTimeMillis()).commit();
		}

		interstitial = new InterstitialAd(this);
		interstitial.setAdUnitId(getResources().getText(R.string.ad_unit_id_interstital).toString());
		interstitial.setAdListener(adsListener);



		if (mPrefs.getBoolean("isPremium", false)==true||doesAdShowBasedOnClicks()==false){
			Log.d("ads management", "ad hide"+doesAdShowBasedOnClicks());
			hideAd();
		}else{
			//show banner ad
			showAd();
			//load interstital ad
			Log.d("ads management", "ad show");
			// Create the interstitial.
			//String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

			// Create ad request.
			AdRequest adRequest = new AdRequest.Builder()
			.addTestDevice(deviceId)
			.build();

			// Begin loading your interstitial.
			interstitial.loadAd(adRequest);

		}



		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new MainFragment()).commit();
		}
		Intent intent=this.getIntent();
		boolean isNotification=intent.getBooleanExtra("FromNotification", false);
		if (isNotification==true){
            if(!intent.getExtras().getString("DataType").equalsIgnoreCase(MainActivity.DATA_TYPE_GENERAL_THIKR)){
                launchFragment(new ThikrFragment(), intent.getExtras(),"ThikrFragment");
            }
		}
        boolean isFromSettings=intent.getBooleanExtra("FromPreferenceActivity", false);
        if (isFromSettings==true){
            intent = new Intent();
            intent.setClass(MainActivity.this, SetPreferenceActivity.class);
            startActivityForResult(intent, 0);
        }

	}
    private void isPremiumPurchasedAsync(){
        new isPremiumPurchased().execute();

    }
    private void populateBuiltinDatabase() {
        MyDBHelper db = new MyDBHelper(this);
        db.getReadableDatabase();
        db.close();
        if(db.getAllBuiltinThikrs().size()==0){
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
			intent.setClass(MainActivity.this, SetPreferenceActivity.class);
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
	private void showAd(){

		AdView adView = (AdView)this.findViewById(R.id.adView);
		adView.setAdListener(adsListener);
		AdRequest adRequest = new AdRequest.Builder()
		.addTestDevice(deviceId)
		//.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
		.build();
		adView.loadAd(adRequest);
	}
	// Invoke displayInterstitial() when you are ready to display an interstitial.
	public void displayInterstitial() {  
		if (interstitial.isLoaded()&&(mPrefs.getBoolean("isPremium", false)==false)) {
			long timeAtLastAd=mPrefs.getLong("time_at_last_ad", 0);
			if ((System.currentTimeMillis()-timeAtLastAd)>=5*60*1000){
				//1 interstital ad no less than 5 minutes apart
				if(doesAdShowBasedOnClicks()==true){
					mPrefs.edit().putLong("time_at_last_ad",System.currentTimeMillis()).commit();
					interstitial.show();
				}

			}
		}
	}
	private boolean doesAdShowBasedOnClicks(){
		if(mPrefs.getBoolean("isPremium", false)==true){
			Log.d("ads management", "user is premium");
			return false;
		}
		long timeAtLastAd=mPrefs.getLong("time_at_last_click", 0);
		if ((System.currentTimeMillis()-timeAtLastAd)<7*24*60*60*1000){
			Log.d("ads management", "time since last ad="+(System.currentTimeMillis()-timeAtLastAd));
			return false;
		}
		return true;
	}

	@Override
	public void launchFragment(Fragment iFragment, Bundle args,String tag) {
		iFragment.setArguments(args);
		android.app.FragmentTransaction fragmentTransaction1 = this.getFragmentManager().beginTransaction();
		fragmentTransaction1.replace(R.id.container, iFragment,tag);
		fragmentTransaction1.addToBackStack(null);
		fragmentTransaction1.commit();

	}

    @Override
    public void share() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,this.getResources().getText(R.string.share_text));
        startActivity(Intent.createChooser(shareIntent, this.getResources().getString(R.string.share)));
    }

    @Override
	public void upgrade() {
        Bundle buyIntentBundle = null;
       if (mServiceInAppBilling!=null) {
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
	public void onPause(){
      //  stopLocationUpdates();
        unbindtoMediaService();
		super.onPause();

	}
    @Override
    protected void onResume() {
        unbindtoMediaService();
        super.onResume();

    }
    @Override
    protected void onDestroy() {

        unbindtoMediaService();
        if (mServiceInAppBilling != null) {
            unbindService(mServiceInAppBillingConn);
        }
        super.onDestroy();

    }
	@Override
	public void onBackPressed()
	{
		this.displayInterstitial();
		super.onBackPressed();  
	}
	@Override
	public void playAll() {
		Bundle data=new Bundle();
		data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAYALL);
		sendActionToMediaService(data);
	}
	@Override
	public void incrementCurrentPlaying(int i) {
        Bundle data=new Bundle();
        data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_INNCREMENT);
        data.putInt("INCREMENT", i);
        sendActionToMediaService(data);
	}
	@Override
	public void pausePlayer() {
		Bundle data=new Bundle();
		data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PAUSE);
		sendActionToMediaService(data);

	}
	@Override
	public void play(int fileNumber) {
		Bundle data=new Bundle();
		data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_PLAY);
		data.putInt("FILE", fileNumber);
		sendActionToMediaService(data);

	}
	@Override
	public boolean isPlaying() {
		//hanihani
		Bundle data=new Bundle();
		data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_ISPLAYING);
		sendActionToMediaService(data);
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		return sharedPrefs.getBoolean("ISPLAYING", false);

	}
	@Override
	public void resetPlayer() {
		Bundle data=new Bundle();
		data.putInt("ACTION", ThikrMediaPlayerService.MEDIA_PLAYER_RESET);
		sendActionToMediaService(data);


	}
	@Override
	public void setCurrentPlaying(int currentPlaying) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		sharedPrefs.edit().putInt("currentPlaying", currentPlaying).commit();

	}
	@Override
	public int getCurrentPlaying() {

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		return sharedPrefs.getInt("currentPlaying",1);
	}
	@Override
	public void setThikrType(String thikrType) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		sharedPrefs.edit().putString("thikrType", thikrType).commit();

	}
    public String getThikrType(){
        return this.mPrefs.getString("thikrType",MainActivity.DATA_TYPE_DAY_THIKR);
    }
	public String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i=0; i<messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		Log.d("testing123", " RC_REQUEST  " + RC_REQUEST + "results ok " + RESULT_OK);
		// Pass on the activity result to the helper for handling
        if (requestCode == RC_REQUEST) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
            if (resultCode == RESULT_OK&&responseCode==0) {

                try {

                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    if(Security.verifyPurchase(base64RSAPublicKey, dataSignature, String.valueOf(RC_REQUEST)) && sku.equalsIgnoreCase(SKU_PREMIUM)) {

                        mPrefs.edit().putBoolean("isPremium", true).commit();
                        this.hideAd();
                    }
                    //alert("You have bought the " + sku + ". Excellent choice,adventurer!");
                }
                catch (JSONException e) {
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
        Log.d("test","onConnectionFailed "+connectionResult.toString());
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("test","onConnected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {

            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("latitude", Double.toString(mLastLocation.getLatitude())).commit();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("longitude", Double.toString(mLastLocation.getLongitude())).commit();
            Log.d("test", "latitude is " + Double.toString(mLastLocation.getLatitude()));
            PrayTime.instancePrayTime(this);
        }else{
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            locationRequest.setInterval(5000);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        }
    }
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.d("test", "onConnectionSuspended");
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d("test","latitude is "+Double.toString(location.getLatitude()));
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("latitude", Double.toString(location.getLatitude())).commit();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("longitude", Double.toString(location.getLongitude())).commit();
        stopLocationUpdates();
        PrayTime.instancePrayTime(this);

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
			Log.d("ads management","ad openned");
			//mPrefs.edit().putLong("time_at_last_click", System.currentTimeMillis()).commit();
			super.onAdOpened();
		}
		@Override
		public void onAdClosed(){
			super.onAdClosed();
		}
		@Override
		public void onAdLeftApplication(){
			Log.d("ads management","ad left application");
			myActivity.hideAd();
			mPrefs.edit().putLong("time_at_last_click", System.currentTimeMillis()).commit();
			super.onAdLeftApplication();

		}
	}



    private class isPremiumPurchased extends AsyncTask<Void, Void, Boolean> {


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
                Log.d("testing123","isPremiumPurchased called");
                Bundle ownedItems = mServiceInAppBilling.getPurchases(3, getPackageName(), "inapp", null);
                int response = ownedItems.getInt("RESPONSE_CODE");
                Log.d("testing123","response ="+response);
                if (response == 0) {
                    ArrayList<String> ownedSkus =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                    ArrayList<String>  purchaseDataList =
                            ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                    ArrayList<String>  signatureList =
                            ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                    String continuationToken =
                            ownedItems.getString("INAPP_CONTINUATION_TOKEN");
                    Log.d("testing123","ownedSkus ="+ownedSkus.size());
                    for (int i = 0; i < purchaseDataList.size(); ++i) {

                        String purchaseData = purchaseDataList.get(i);
                        String signature = signatureList.get(i);
                        String sku = ownedSkus.get(i);
                        Log.d("testing123","sku ="+sku);
                        Log.d("testing123","base64"+signature);
                        Log.d("testing123","base64"+base64RSAPublicKey);
                        Log.d("testing123","Is signature valid? "+Security.verifyPurchase(base64RSAPublicKey,String.valueOf(RC_REQUEST),signature));
                        Log.d("testing123","Is premium sku? "+sku.equalsIgnoreCase(SKU_PREMIUM));
                        if(Security.verifyPurchase(base64RSAPublicKey, signature, String.valueOf(RC_REQUEST)) && sku.equalsIgnoreCase(SKU_PREMIUM)) {

                            Log.d("testing123", "ispremium true ");
                            Log.d("base64", "base64 matches");
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
           }
            return;
        }
    }


}


