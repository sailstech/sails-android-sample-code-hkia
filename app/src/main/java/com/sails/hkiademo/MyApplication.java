package com.sails.hkiademo;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.parse.Parse;

/**
 * Created by richard on 2016/5/11.
 */
public class MyApplication extends Application {
    final String PARSE_SERVER_APPLICATION_ID="srANu2SqvQXWB9ZEfuM8";
    final String PARSE_SERVER_URL="https://api.sailstech.com/parse/";

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(PARSE_SERVER_APPLICATION_ID)
                .server(PARSE_SERVER_URL)
                .enableLocalDataStore()
                .build());
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }
}