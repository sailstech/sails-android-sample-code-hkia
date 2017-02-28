package com.sails.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Service 的相關設定值
 * Created by eddiehua on 2014/4/9.
 */
public class Config {
    //public static boolean isInit = false;
    public static boolean serviceEnable = true;
    public static int intervalCheckSecond = 0;
    public static int vibrateMills = 0; // 震動秒數，0就是關掉
    public static final int DEFAULT_INTERVAL_CHECK = 5 * 60;   // 5 min
    public static final int DEFAULT_VIBRATE_MS = 900;       // default vibrate milliseconds

    private static final String TAG = "ServiceConfig";

    private static final String KEY_SERVICE_ENABLE = "SrvEna";    // Service 是否啟動
    private static final String KEY_INTERVAL_CHECK_SECOND = "IntChkSec";    // 檢查的間隔秒數
    private static final String KEY_VIBRATE_MILLISECOND = "VibMs";            // 震動時間。0為關閉

    public static void loadConfig(Context context)
    {
        if (Version.DEBUG)
            Log.i(TAG, "loadConfig");
        // Load Config
        SharedPreferences prefer = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        serviceEnable = prefer.getBoolean(KEY_SERVICE_ENABLE, true);
        vibrateMills = prefer.getInt(KEY_VIBRATE_MILLISECOND, DEFAULT_VIBRATE_MS);
        intervalCheckSecond = prefer.getInt(KEY_INTERVAL_CHECK_SECOND, DEFAULT_INTERVAL_CHECK);
        //isInit = true;  // 有初始化過了
    }
    public static void saveConfig(Context context)
    {
        if (Version.DEBUG)
            Log.i(TAG, "saveConfig");
        SharedPreferences prefer = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefer.edit();
        editor.putBoolean(KEY_SERVICE_ENABLE, serviceEnable);
        editor.putInt(KEY_VIBRATE_MILLISECOND, vibrateMills);
        editor.putInt(KEY_INTERVAL_CHECK_SECOND, intervalCheckSecond);
        editor.commit();
    }
}
