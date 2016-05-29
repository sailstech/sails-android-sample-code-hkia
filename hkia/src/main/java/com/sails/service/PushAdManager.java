package com.sails.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.util.LongSparseArray;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.sails.hkiademo.R;
import com.sails.engine.Beacon;

/**
 * 管理 Beacon通知的顯示頻率
 * 1) 同一則訊息 1分鐘(Debug) / 1天(Release) 內，不得顯示第二次。
 * Created by eddiehua on 2014/4/11.
 */
public class PushAdManager {
    private static final String TAG = "PushAdManager";
    private LongSparseArray<Long> mAdPushRecord;
    private static final long LIFE_MILLISEC_DEBUG = 60 * 1000;  // 60秒
    private static final long LIFE_MILLISEC_RELEASE = 24 * 60 * 60 * 1000;  // 1天
    private static final long LIFE_TIME = Version.DEBUG ? LIFE_MILLISEC_DEBUG : LIFE_MILLISEC_RELEASE;

    private Context mContext;

    public PushAdManager(Context context) {
        mAdPushRecord = new LongSparseArray<Long>();
        mContext = context;
    }

    public void release()
    {
        mContext = null;
        if (mAdPushRecord != null) {
            synchronized (mAdPushRecord) {
                mAdPushRecord.clear();
                mAdPushRecord = null;
            }
        }
    }

    // 清除過期的
    public void clearExpiredRecord()
    {
        if (mAdPushRecord == null)
            return;

        LongSparseArray<Long> adPushRecord = new LongSparseArray<Long>();
        synchronized(mAdPushRecord) {
            int nCount = mAdPushRecord.size();
            for (int i = 0; i < nCount; ++i) {
                final long notifyTime = mAdPushRecord.valueAt(i);
                if (isHot(notifyTime))  // 留下熱的，過期就算了
                    adPushRecord.put(mAdPushRecord.keyAt(i), notifyTime);
            }
            mAdPushRecord.clear();
            mAdPushRecord = adPushRecord;
        }
    }
    // 燒唷~ 才剛顯示沒多久
    private boolean isHot(long notifyTime)
    {
        return ((System.currentTimeMillis() - notifyTime) < LIFE_TIME);
    }
    public void notifyBeacon(Beacon beacon, int nId, boolean bVibrate)
    {
        if (Version.DEBUG)
        {
            if (beacon != null)
                Log.i(TAG, "notifyBeacon : " + beacon.push_name);
            else
                Log.i(TAG, "notifyBeacon");
        }
        synchronized(mAdPushRecord) {
            // check
            Long prevNotifyTime = mAdPushRecord.get(beacon.getBTLEId());
            if (prevNotifyTime != null) {
                if (isHot(prevNotifyTime)) {
                    if (Version.DEBUG)
                        Log.i(TAG, "Skip Push Ad : " + beacon.push_name);
                    return; // 不顯示了
                }
            }
            mAdPushRecord.put(beacon.getBTLEId(), System.currentTimeMillis());
        }
        // -----------------------------------------------
        String strName = "Sails Service (TEST)" + nId;
        String strUrl = "http://www.sailstech.com";
        String strMsg = "Go to website";

        if (beacon != null)
        {
            strName = beacon.push_name;
            strUrl = beacon.push_link;
            if (!strUrl.contains("http://") && !strUrl.contains("https://"))
                strUrl = "http://" + strUrl;
        }

        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(strUrl));
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
        pushSystemNotification(strName, strMsg, contentIntent, nId, bVibrate);
    }

    public void pushSystemNotification(CharSequence contentTitle, CharSequence contentText, PendingIntent contentIntent, int nId, boolean bVibrate)
    {
        Notification noti = new Notification(R.drawable.noti_sails, contentTitle, System.currentTimeMillis());
        //Notification noti = new Notification(android.R.drawable.stat_notify_voicemail, strName, System.currentTimeMillis());
//        noti.setLatestEventInfo(mContext, contentTitle, contentText, contentIntent);
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notiMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notiMgr.notify(nId, noti);
        if (bVibrate)
        {
            Object vibrator = mContext.getSystemService(Service.VIBRATOR_SERVICE);
            if (Config.vibrateMills > 0 && vibrator != null) {
                TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                if (TelephonyManager.CALL_STATE_IDLE == telephonyManager.getCallState())
                    ((Vibrator) vibrator).vibrate(Config.vibrateMills);
                else
                    Log.i(TAG, "Phone is not idle, Skip Vibrate!");
            }
        }
    }
    public void cancelNotification(int nNotiID)
    {
        NotificationManager notiMgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notiMgr.cancel(nNotiID);
    }
}
