package com.ravit.android.glinda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by ravit on 11/03/16.
 */
public class GlindaReceiver extends BroadcastReceiver {

	private long mTimestamp;
	private static int mCountPowerOff = 0;

	public GlindaReceiver () {
		mTimestamp = System.currentTimeMillis();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("Glinda", "receiver");

		if ((intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) || (intent.getAction().equals(Intent.ACTION_SCREEN_ON))) {
			if (System.currentTimeMillis() - mTimestamp < 60000 ) {
				mCountPowerOff++;
				if (mCountPowerOff >= 3) {
					Log.i("Glinda", "Start help method");
				}
			} else {
				mCountPowerOff = 0;
			}

			Log.i("Glinda`", "In Method:  ACTION_SCREEN_ON./OFF " + String.valueOf(System.currentTimeMillis() - mTimestamp) + " count: " + mCountPowerOff);
			mTimestamp = System.currentTimeMillis();
		}
	}
}
