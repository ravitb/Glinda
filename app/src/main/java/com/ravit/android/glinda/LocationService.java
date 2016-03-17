package com.ravit.android.glinda;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

/**
 * Created by ravit on 14/03/16.
 */
public class LocationService extends Service implements
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener,
		LocationListener {

	private static final String TAG = "Glinda"; //Service.class.getSimpleName();
	private static final int INTERVAL = 60000;
	private static final int FATEST_INTERVAL = 5000;

	private GoogleApiClient mGoogleApiClient;
	private boolean mRequestingLocationUpdates;
	private LocationRequest mLocationRequest;
	private Location mCurrentLocation;
	private ArrayList<LocationUpdateListener> mListeners;
	private final IBinder mBinder = new LocationServiceBinder();

	private void setupGoogleLocationServices() {
		mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
	}

	private void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(INTERVAL);
//		mLocationRequest.setFastestInterval(FATEST_INTERVAL);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	public Location getCurrentLocation() {
		if(mCurrentLocation == null) {
			try {
				mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
			} catch (java.lang.SecurityException ex) {
				Log.i(TAG, "fail to request location update, ignore", ex);
			} catch (IllegalArgumentException ex) {
				Log.d(TAG, "network provider does not exist, " + ex.getMessage());
			}
		}
		return mCurrentLocation;
	}

	public void stopLocationUpdates() {
		if(!mGoogleApiClient.isConnected()) {
			/// Not connected yet. No need to remove location updates.
			return;
		}
		LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
		Log.d(TAG, "stopping location updates");
	}
	public void startLocationUpdates() {
		if(!mGoogleApiClient.isConnected()) {
			Log.w(TAG, "Google API client is not yet connected; cannot request updates yet");
			return;
		}
		try {
			LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
		} catch (java.lang.SecurityException ex) {
			Log.i(TAG, "fail to request location update, ignore", ex);
		} catch (IllegalArgumentException ex) {
			Log.d(TAG, "network provider does not exist, " + ex.getMessage());
		}
		Log.d(TAG, "starting location updates");
	}

	@Override
	public void onCreate() {
		setupGoogleLocationServices();
		createLocationRequest();
		mRequestingLocationUpdates = true;
		Log.i(TAG, "LocationService created");
		mGoogleApiClient.connect();
		mListeners = new ArrayList<>();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "LocationService destroyed");
		mGoogleApiClient.disconnect();
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(TAG, "connected to Google Play services");
		if(mRequestingLocationUpdates)
			startLocationUpdates();
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.e(TAG, "connection to Google Play services has been suspended");
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.e(TAG, "connection to Google Play services has failed");
	}

	public static class ServiceConnection implements android.content.ServiceConnection {
		private LocationService mLocationService;

		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been established, giving us the service object we can use to interact with the service.  Because we have bound to a explicit service that we know is running in our own process, we can cast its IBinder to a concrete class and directly access it.
			mLocationService = ((LocationService.LocationServiceBinder)service).getService();
			mLocationService.startLocationUpdates();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never see this happen.
			mLocationService = null;
		}
		public LocationService getService() {
			return mLocationService;
		}
	}

	/**
	 * Class for clients to access.  Because we know this service always runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocationServiceBinder extends Binder {
		LocationService getService() {
			return LocationService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.v(TAG, "received location update");
		mCurrentLocation = location;
		Log.d(TAG, mCurrentLocation.toString());
		sendSMSAndShowMessage(this, mCurrentLocation.toString());
		for(LocationUpdateListener l : mListeners)
			l.onLocationUpdated(location);
	}

	/// add/remove listeners which will be notified when the location changes. We're not using these right now (our activities are only interested in querying the location, not getting updates), but they could be useful in the future
	public void addListener(LocationUpdateListener l) {
		mListeners.add(l);
	}
	public void removeListener(LocationUpdateListener l) {
		mListeners.remove(l);
	}

	/**
	 * Interface for other classes that want to know when the Location is updated.
	 */
	public interface LocationUpdateListener {
		void onLocationUpdated(Location location);
	}

	private void sendSMSAndShowMessage(Context context, String location) {
		SmsManager smsManager= SmsManager.getDefault();
		smsManager.sendTextMessage("0556655919", null, "i need your help" + location, null, null);
		Toast.makeText(context, "SMS was sent", Toast.LENGTH_LONG).show();
	}
}
