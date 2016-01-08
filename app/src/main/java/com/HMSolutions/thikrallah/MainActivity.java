package com.HMSolutions.thikrallah;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import com.HMSolutions.thikrallah.Fragments.MainFragment;
import com.HMSolutions.thikrallah.Fragments.ThikrFragment;
import com.HMSolutions.thikrallah.Utilities.AppRater;
import com.HMSolutions.thikrallah.Utilities.MainInterface;
import com.HMSolutions.thikrallah.Utilities.PrayTime;
import com.HMSolutions.thikrallah.Utilities.WhatsNewScreen;
import com.HMSolutions.thikrallah.Utilities.inapp.IabHelper;
import com.HMSolutions.thikrallah.Utilities.inapp.IabResult;
import com.HMSolutions.thikrallah.Utilities.inapp.Inventory;
import com.HMSolutions.thikrallah.Utilities.inapp.Purchase;
import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.Fragment;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
	ThikrMediaPlayerService mediaService;
	boolean mBound = false;
	public static final String DATA_TYPE_NIGHT_THIKR="night";
	public static final String DATA_TYPE_DAY_THIKR="morning";
	public static final String DATA_TYPE_GENERAL_THIKR="general";
	private InterstitialAd interstitial;
	IInAppBillingService mService;
	IabHelper mHelper;
	SharedPreferences mPrefs;
	private static final String TAG = "MainActivity";
	Activity activity=this;
	private static boolean mIsPremium = false;
	private final static String SKU_PREMIUM = "premiumupgrade";
	private String base64RSAPublicKey="";
	static final int RC_REQUEST = 9648253;
	// Listener that's called when we finish querying the items and subscriptions we own
	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

			//Log.d(TAG, "Query inventory finished.");
			// Have we been disposed of in the meantime? If so, quit.
			if (mHelper == null) return;
			if (result.isFailure()) {
				//Toast.makeText(MainActivity.this, "Failed to query inventory: " + result, Toast.LENGTH_SHORT).show();
				return;
			}

			//Log.d(TAG, "Query inventory was successful.");

			/*
			 * Check for items we own. Notice that for each purchase, we check
			 * the developer payload to see if it's correct! See
			 * verifyDeveloperPayload().
			 */

			// Do we have the premium upgrade?
			Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
			mIsPremium = (premiumPurchase != null);
			Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
			mPrefs.edit().putBoolean("isPremium", mIsPremium).commit();
			//Log.d(TAG, "Initial inventory query finished; enabling main UI.");
		}
	};

	// Callback for when a purchase is finished
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
			// if we were disposed of in the meantime, quit.
			if (mHelper == null) return;

			if (result.isFailure()) {
				//Toast.makeText(MainActivity.this, "Error purchasing: " + result, Toast.LENGTH_SHORT).show();
				return;
			}
			/*
	            if (!verifyDeveloperPayload(purchase)) {
	            	Toast.makeText(MainActivity.this, "Error purchasing. Authenticity verification failed.", Toast.LENGTH_SHORT).show();
	                return;
	            }
			 */

			Log.d(TAG, "Purchase successful.");


			if (purchase.getSku().equals(SKU_PREMIUM)) {
				// bought the premium upgrade!
				Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
				Builder lBuilder = new Builder(MainActivity.this);
				lBuilder.setMessage(activity.getString(R.string.thanksforupgrade));
				lBuilder.create().show();
				mIsPremium = true;
			}
		}
	};
	private String deviceId;
    private AdListener adsListener;



    @Override
	protected void onStart() {
        //mGoogleApiClient.connect();
        super.onStart();

	}
	public void sendActionToMediaService(Bundle data){
		if (data!=null){
			this.startService(new Intent(this, ThikrMediaPlayerService.class).putExtras(data));
		}

	}
	@Override
	protected void onStop() {
        //mGoogleApiClient.disconnect();
        super.onStop();

	}

	/** Defines callbacks for service binding, passed to bindService() */
	/*
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			MyBinder binder = (MyBinder) service;
			mediaService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }



		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues (this.getApplicationContext(), R.xml.preferences, false);
		 
		adsListener=new myAdListener(this);
		appLink="\n"+this.getResources().getString(R.string.app_link);


		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		Intent intent1 = new Intent("com.HMSolutions.thikrallah.Notification.ThikrBootReceiver.android.action.broadcast");  
		new WhatsNewScreen(this).show();
		AppRater.app_launched(this);
		if (mPrefs.getBoolean("isFirstLaunch", true)){
			Log.d("thikr", "first launch. calling boot recbiever");
			sendBroadcast(intent1);
			mPrefs.edit().putBoolean("isFirstLaunch", false).commit();
			mPrefs.edit().putLong("time_at_last_ad",System.currentTimeMillis()).commit();
		}
		base64RSAPublicKey=getResources().getText(R.string.base64RSAPublicKey).toString();
		deviceId = "AC73D67B1C23A45BBDFCAF3F4040A0AA";//md5(android_id).toUpperCase();

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


		//bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), mServiceConn, Context.BIND_AUTO_CREATE);
		// Create the helper, passing it our context and the public key to verify signatures with
		//Log.d(TAG, "Creating IAB helper.");
		mHelper = new IabHelper(this, base64RSAPublicKey);

		// enable debug logging (for a production application, you should set this to false).
		mHelper.enableDebugLogging(false);

		// Start setup. This is asynchronous and the specified listener
		// will be called once setup completes.
		//	Log.d(TAG, "Starting setup.");
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			public void onIabSetupFinished(IabResult result) {
				//Log.d(TAG, "Setup finished.");

				if (!result.isSuccess()) {
					// Oh noes, there was a problem.
					//Toast.makeText(MainActivity.this, "Problem setting up in-app billing: " + result, Toast.LENGTH_SHORT).show();
					return;
				}
				// Have we been disposed of in the meantime? If so, quit.
				if (mHelper == null) return;

				// Hooray, IAB is fully set up. Now, let's get an inventory of stuff we own.
				//	Log.d(TAG, "Setup successful. Querying inventory.");
				mHelper.queryInventoryAsync(mGotInventoryListener);
			}
		});










		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new MainFragment()).commit();
		}
		Intent intent=this.getIntent();
		boolean isNotification=intent.getBooleanExtra("FromNotification", false);
		if (isNotification==true){
			Bundle data=intent.getExtras();
			launchFragment(new ThikrFragment(), data);
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
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT,appLink);
			startActivity(Intent.createChooser(shareIntent, this.getResources().getString(R.string.share)));
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
	public void launchFragment(Fragment iFragment, Bundle args) {
		iFragment.setArguments(args);
		android.app.FragmentTransaction fragmentTransaction1 = this.getFragmentManager().beginTransaction();
		fragmentTransaction1.replace(R.id.container, iFragment);
		fragmentTransaction1.addToBackStack(null);
		fragmentTransaction1.commit();

	}

	@Override
	public void upgrade() {
		mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST, 
				mPurchaseFinishedListener, "");

	}
	@Override
	public void onPause(){
      //  stopLocationUpdates();
		super.onPause();
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
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		sharedPrefs.edit().putInt("currentPlaying",  sharedPrefs.getInt("currentPlaying",1)+i).commit();
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
		// Pass on the activity result to the helper for handling
        if (requestCode == 1001) {
            if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
				String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
				if (resultCode == RESULT_OK) {
					try {
						JSONObject jo = new JSONObject(purchaseData);
						String sku = jo.getString("productId");

						if (sku.equals(SKU_PREMIUM)){
							mPrefs.edit().putBoolean("isPremium", true).commit();
						}
					}
					catch (JSONException e) {

						e.printStackTrace();
					}
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
            locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
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
}


