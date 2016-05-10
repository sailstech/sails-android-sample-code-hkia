package com.sails.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Set interval Check on ACTION_BOOT_COMPLETED.
 */
public class SailsInitReceiver extends BroadcastReceiver {
    //private final static String TAG = "SailsInitReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            //if (Version.DEBUG)
                //Log.i(TAG, "SailsInitReceiver : ACTION_BOOT_COMPLETED");
            Config.loadConfig(context);
            if (Config.serviceEnable)
                SailsService.startCheckByConfig(context);
        }
    }
}
