package com.sails.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.sails.hkiademo.MainActivity;
import com.sails.hkiademo.R;
import com.sails.engine.Beacon;
import com.sails.engine.LocationRegion;
import com.sails.engine.SAILS;

/**
 * SailsService - Always stay alive
 * 1) ??sec interval to check Sails Engine
 * 2) iBeacon Notification
 */
public final class SailsService extends Service implements LocationListener {
    private static final String TAG = "SailsService";
    //private static final String KEY_UNIQUE_NOTI_ID = "UniNotId";            // Config:使用同一個 Notification ID

    private static final int ID_NOTIFY_IN_REGION = 1;
    private static final int ID_NOTIFY_BEACON = 2;
    //private static final int NOTIFICATION_ID = 88;

    private static PendingIntent mServicePendingIntent = null;      // 用來判別 Service 是否作用中
    private static SAILS mSails = null;
    private static LocationRegion mWuQuanParkwayRegion = null;      // 存 綠園道的 Region
    private static boolean isInWuQuanParkRegion = false;

    private PushAdManager       mPushAdManager;

    // ------------------------------- Battery Level ---------------------------------
    private int mBatteryLevel = 100;    // Use this member - Not yet
    private BroadcastReceiver mBatterLevelReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                //Log.i(TAG, "onReceive action = " + action);
                if (0 == action.compareTo(Intent.ACTION_BATTERY_CHANGED)) {
                    mBatteryLevel = intent.getIntExtra("level", 100);
                    if (Version.DEBUG)
                        Log.i(TAG, "Battery Level = " + intent.getIntExtra("level", 0));
                }
            }
        }
    };
    private void startBatteryStateReceiver()
    {
        registerReceiver(mBatterLevelReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
    // ----------------------------------------------------
    public SailsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        startBatteryStateReceiver();
        mPushAdManager = new PushAdManager(this);
        // Load Taichung 綠園道 圖資
        initSailsEngine(Version.SAILS_TOKEN, Version.BUILDING_ID);
        if (Version.DEBUG)
            Log.i(TAG, "onCreate");
    }
    // load map
    private void initSailsEngine(String token, String buildingId) {
        if (mSails == null) {
            //new a SAILS engine.
            try {
                mSails = new SAILS(this);
            } catch (NoClassDefFoundError error) {
                stopCheck();
                Log.w(TAG, "Not support!");
                //if (Version.DEBUG)
                //notifyBeacon(null, 0, false);
            }
        }
        if (mSails != null) {   // 成功產生
            if (Version.DEBUG)
                Log.w(TAG, "Map loading...");
            mSails.loadCloudBuilding(token, buildingId, new SAILS.OnFinishCallback() {
                @Override
                public void onSuccess(String response) {
                    if (Version.DEBUG)
                        Log.w(TAG, "loadMapFromCloud Success + " + response);
                    mWuQuanParkwayRegion = mSails.getFilteredLocationRegionList(mSails.getFloorNameList().get(0), "building", null).get(0);
                }
                @Override
                public void onFailed(String response) {
                    if (Version.DEBUG)
                        Log.w(TAG, "loadMapFromCloud Failed + " + response);
                }
            });

            if (Version.DEBUG)
                Log.i(TAG, "[Sails] Start : " + System.currentTimeMillis());
            //create location change call back.
            mSails.setMode(SAILS.BLE_ADVERTISING);
        }
    }
    @Override
    public void onDestroy() {
        if (mSails != null) {
            mSails.stopLocatingEngine();
            mSails.setOnBTLEPushEventListener(null);
            mSails = null;
        }
        if (mPushAdManager != null)
        {
            mPushAdManager.release();
            mPushAdManager = null;
        }
        if (mBatterLevelReceiver != null) {
            unregisterReceiver(mBatterLevelReceiver);
            mBatterLevelReceiver = null;
        }
        if (Version.DEBUG)
            Log.i(TAG, "onDestroy");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Version.DEBUG)
            Log.i(TAG, "onStartCommand StartId = " + startId + " currentTimeMillis = " + System.currentTimeMillis());
        if (!Config.serviceEnable)
        {
            if (Version.DEBUG)
                Log.i(TAG, "SailsService is disable");
            stopCheck();
            stopSelf();
            return START_NOT_STICKY;    // 告訴系統不要刻意去重新啟動該服務，即使它有足夠的內存。
        }
        // -------------------
        if (Config.intervalCheckSecond > 0) {
            // 檢查~
            //if (!isInForeground("com.sails.imap"))      // 如果service跟 com.sails.imap 包在一起，就沒辦法判別了。
            if (mWuQuanParkwayRegion != null && !MainActivity.isForeground) {
                checkSailsEngine();
            } else {
                if (Version.DEBUG)
                    Log.w(TAG, "iMap in Foreground Or MapNotLoad : Skip check Sails Engine");
            }
        } else {
            startCheckByConfig(this); // 從設定中戴入
        }
        return START_NOT_STICKY;//super.onStartCommand(intent, flags, startId);
    }
    // check流程 - 目前是假設 20 秒 就關閉。
    protected void checkSailsEngine()
    {
        // TODO : Adjust process time...
        final int nStopEngineAfterMilliSec = 20 * 1000; // 20秒以內完成
        if (Version.DEBUG)
            Log.i(TAG, "checkSailsEngine");
        startGps(this, nStopEngineAfterMilliSec / 2, 0, this);

        mSails.setOnBTLEPushEventListener(new SAILS.OnBTLEPushEventListener() {
            @Override
            public void OnPush(Beacon beacon) {
                mPushAdManager.notifyBeacon(beacon, ID_NOTIFY_BEACON, true);
            }

            @Override
            public void OnNothingPush() {
                if (Version.DEBUG) {
                    mPushAdManager.notifyBeacon(null, ID_NOTIFY_BEACON, false);
                }
            }
        });
        // Sails Engine
        mSails.startLocatingEngine();
        // -- 後關閉
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSails != null) {
                    mSails.stopLocatingEngine();
                    mSails.setOnBTLEPushEventListener(null);
                    if (Version.DEBUG)
                        Log.i(TAG, "[Sails] Stop : " + System.currentTimeMillis());
                }
                stopGps(SailsService.this, SailsService.this);
            }
        }, nStopEngineAfterMilliSec);
    }
    // ------------------------------- Start/Stop Interval Check ------------------------------------------
    public static void startCheckByConfig(Context context)
    {
        // 沒有設定間隔秒數的話，從設定中載入
        Config.loadConfig(context);
        if (Config.intervalCheckSecond > 0)
            startIntervalCheck(context, Config.intervalCheckSecond);
    }
    //
    public static void startIntervalCheck(Context context, int intervalSecond) {
        Config.intervalCheckSecond = intervalSecond;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        _stopCheck(alarmManager);

        if (mServicePendingIntent == null) {
            mServicePendingIntent = PendingIntent.getService(context, 0,
                    new Intent(context, SailsService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        }
        // RTC : No wake device
        if (Version.DEBUG)
            intervalSecond/=2;  // 讓 Debug 更快
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), intervalSecond * 1000, mServicePendingIntent);
        if (Version.DEBUG)
            Log.i(TAG, "startIntervalCheck : " + intervalSecond + " Sec");
    }
    private void stopCheck() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        _stopCheck(alarmManager);
    }
    private static void _stopCheck(AlarmManager alarmManager)
    {
        if (mServicePendingIntent != null) {
            alarmManager.cancel(mServicePendingIntent);
            mServicePendingIntent = null;
        }
    }
    // ============================ GPS ================================
    /*
     * @param minTime minimum time interval between location updates, in milliseconds
     * @param minDistance minimum distance between location updates, in meters
     */
    public static void startGps(Context context, long minTime, float minDistance, LocationListener listener)
    {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, listener);

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, listener);

/*
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {

            // 由Criteria物件判斷提供最準確的資訊
            String bestProvider = LocationManager.NETWORK_PROVIDER;
            Criteria criteria = new Criteria();  //資訊提供者選取標準
            criteria.setAccuracy(Criteria.ACCURACY_FINE); // 精確度
            criteria.setAltitudeRequired(false);   // 是否需要知道 海拔高度
            criteria.setBearingRequired(false);     //
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            criteria.setSpeedRequired(false); // 对速度是否关注
            bestProvider = locationManager.getBestProvider(criteria, true);    //選擇精準度最高的提供者
            Location location = locationManager.getLastKnownLocation(bestProvider);
            locationManager.requestLocationUpdates(bestProvider, minTime, minDistance, listener);
            if (Version.DEBUG) {
                Log.i(TAG, "Best Provider : " + bestProvider);
                if (location != null)
                    Log.i(TAG, "Last Known Location Lat = " + location.getLatitude() + " Lon" + location.getLongitude());
            }
        } else {
            Toast.makeText(context, "請開啟定位服務", Toast.LENGTH_LONG).show();
            //getService = true; //確認開啟定位服務
            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); //開啟設定頁面
        }*/
    }

    public static void stopGps(Context context, LocationListener listener)
    {
        if (Version.DEBUG)
            Log.i(TAG, "stopGps");
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(listener);
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        // 緯度 Latitude,  經度 Longitude
        if (Version.DEBUG)
            Log.i(TAG, location.getProvider() + " > 緯度 lat:" + location.getLatitude() + ", 經度 long:"+location.getLongitude());

        // ----------- Eddie Try : 開啟指定的 geo座標 -----------
        /*
        Uri uri = Uri.parse("geo:" + location.getLatitude() + "," + location.getLongitude());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/
        // ---------------------------------------------------
        if (mWuQuanParkwayRegion != null) {
            // 判別是否進入 Region 內
            if (mWuQuanParkwayRegion.isInRegion(location.getLongitude(), location.getLatitude())) {
                if (Version.DEBUG)
                    Log.i(TAG, "In Region");
                if (!isInWuQuanParkRegion) {
                    isInWuQuanParkRegion = true;
                    Resources res = getResources();
                    String strName = res.getString(R.string.welcome_wuquanpark);
                    String strUrl = mWuQuanParkwayRegion.beacon.store_link;
                    String strMsg = res.getString(R.string.introduction);

                    Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(strUrl));
                    PendingIntent contentIntent = PendingIntent.getActivity(SailsService.this, 0, notificationIntent, 0);
                    mPushAdManager.pushSystemNotification(strName, strMsg, contentIntent, ID_NOTIFY_IN_REGION, true);
                }
            } else {
                if (Version.DEBUG)
                    Log.i(TAG, "Out Region");
                if (isInWuQuanParkRegion)
                {
                    isInWuQuanParkRegion = false;
                    if (Version.DEBUG) {
                        String strName = "你離開了綠園道 (Debug Only)";
                        String strUrl = mWuQuanParkwayRegion.beacon.store_link;
                        String strMsg = "瀏覽簡介";

                        //Intent notificationIntent = new Intent(Intent.ACTION_VIEW,
                                //Uri.parse("geo:" + mWuQuanParkwayRegion.beacon.lat + "," + mWuQuanParkwayRegion.beacon.lon));
                        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(strUrl));
                        PendingIntent contentIntent = PendingIntent.getActivity(SailsService.this, 0, notificationIntent, 0);
                        mPushAdManager.pushSystemNotification(strName, strMsg, contentIntent, ID_NOTIFY_IN_REGION, true);
                    } else {
                        mPushAdManager.cancelNotification(ID_NOTIFY_IN_REGION);
                    }
                }
            }
        }
    }
    @Override
    public void onProviderDisabled(String provider) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){}

/*
    // 只適用於 不同 application package
    private boolean isInForeground(String strProcessName)
    {
        boolean bExist = false;
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessList = activityManager
                .getRunningAppProcesses();
        if (appProcessList != null) {
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
                if (0 == strProcessName.compareTo(appProcess.processName)) {
                    Log.i(TAG, "processName: " + appProcess.processName + " importance = " + appProcess.importance
                            + " ReasonCode = " + appProcess.importanceReasonCode);
                }
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    bExist = true;
                    break;
                }
                    if (Version.DEBUG)
                    {
                        Log.i(TAG, "processName: " + appProcess.processName);
                        for (String strPkgName : appProcess.pkgList) {
                            Log.i(TAG, "packageName " + strPkgName);
                        }
                    }
                    int pid = appProcess.pid; // pid
                    String processName = appProcess.processName; // 进程名
                    Log.i(TAG, "processName: " + processName + "  pid: " + pid);

                    String[] pkgNameList = appProcess.pkgList; // 获得运行在该进程里的所有应用程序包

                    // 输出所有应用程序的包名
                    for (int i = 0; i < pkgNameList.length; i++) {
                        String pkgName = pkgNameList[i];
                        Log.i(TAG, "packageName " + pkgName + " at index " + i + " in process " + pid);
                        // 加入至map对象里
                        //pgkProcessAppMap.put(pkgName, appProcess);
                    }

            }
        }
        return bExist;
    }*/
}
