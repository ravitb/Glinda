package com.ravit.android.glinda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by ravit on 11/03/16.
 */
public class GlindaReceiver extends BroadcastReceiver {

	private static String TAG = "Glinda";
	private long mTimestamp;
	private static int mCountPowerOff = 0;
	private GoogleApiClient mGoogleApiClient;

	public GlindaReceiver () {
		mTimestamp = System.currentTimeMillis();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "receiver");

		if ((intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) || (intent.getAction().equals(Intent.ACTION_SCREEN_ON))) {
			if (System.currentTimeMillis() - mTimestamp < 60000 ) {
				mCountPowerOff++;
				if (mCountPowerOff >= 3) {
					Log.i(TAG, "Start help method");
					Intent i = new Intent(context, LocationService.class);
					context.startService(i);
				}
			} else {
				mCountPowerOff = 0;
			}

			Log.i(TAG, "In Method:  ACTION_SCREEN_ON./OFF " + String.valueOf(System.currentTimeMillis() - mTimestamp) + " count: " + mCountPowerOff);
			mTimestamp = System.currentTimeMillis();
		}
	}

}
