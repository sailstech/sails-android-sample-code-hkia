package com.sails.hkiademo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sails.engine.Beacon;

import java.lang.ref.WeakReference;

/**
 * Created by eddiehua on 2014/4/8.
 */
public class BeaconMsgDialog extends Dialog {
    private static final String     TAG = BeaconMsgDialog.class.getSimpleName();
    public static final int         CLOSE_ON_CLICK_OUTSIDE = 0;
    private static final int 		ID_TEXT_MSG = 11;
    private static BeaconMsgDialog  mDialog = null;


    public static void hideMsg()
    {
        if (mDialog != null)
            mDialog.dismiss();
    }

    public static void showBeacon(Context context, Beacon beacon, long delayMillisHide)
    {
        showMsg(context, beacon.push_name, beacon.push_link, delayMillisHide);
    }

    public static void showMsg(Context context, String strMsg, String strLinkUrl, long delayMillisHide)
    {
        if (mDialog == null) {
            mDialog = new BeaconMsgDialog(context);
        }
        mDialog.cancelDelayDismiss();
        mDialog.setMsg(strMsg, strLinkUrl);
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
        if (delayMillisHide > 0) {
            mDialog.setDelayDismiss(delayMillisHide);
        }
    }

    // ======================================================
    private static final int MESSAGE_DISMISS = 100;
    private static class MyHandler extends Handler {
        private WeakReference<BeaconMsgDialog> mWeakDialog = null;
        MyHandler(BeaconMsgDialog dialog) {
            mWeakDialog = new WeakReference<BeaconMsgDialog>(dialog);
        }
        @Override
        public void handleMessage(Message msg)
        {
            BeaconMsgDialog dialog = mWeakDialog.get();
            if (dialog != null && msg.what == MESSAGE_DISMISS)
            {
                dialog.dismiss();
            }
        }
    }
    private MyHandler        mHandler = new MyHandler(this);

    private static LinearLayout     mMainLayout = null;
    private static String           mStrMsg = null;
    private static String           mStrLinkUrl = null;
    // Dialog Constructor
    public BeaconMsgDialog(Context context)
    {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(true);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams wmlp = window.getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        //  ------- Layout -------
        LinearLayout layout = new LinearLayout(context);
        //layout.setBackgroundColor(Color.BLACK);
        //layout.setBackgroundResource(R.drawable.msg_background);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setPadding(10, 8, 10, 4);
        mMainLayout = layout;

        LinearLayout.LayoutParams	txtParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        // App Icon
        ImageView appIcon = new ImageView(context);
        appIcon.setImageResource(R.drawable.beacon_info_icon);
        layout.addView(appIcon, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        // text view
        TextView text = new TextView(context);
        text.setId(ID_TEXT_MSG);
        //text.setTextColor(Color.WHITE);
        text.setPadding(8, 4, 4, 0);
        text.setGravity(Gravity.CENTER);
        text.setTextSize(32);
        //text.setShadowLayer(6, 0, 0, Color.rgb(41, 154, 148));//Color.rgb(71, 184, 168));
        layout.addView(text, txtParam);
        // View Web Button
        ImageButton button = new ImageButton(context);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStrLinkUrl != null) {
                    Context context = getContext();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mStrLinkUrl));
                    try {
                        context.startActivity(intent);
                    } catch (android.content.ActivityNotFoundException anfe) {
                        Log.e(TAG, "Beacon 的 Uri有問題 : " + mStrLinkUrl);
                        //Toast.makeText(context, "Beacon 的 Uri有問題 : " + mStrLinkUrl, Toast.LENGTH_LONG).show();
                    }
                    // 關閉 Dialog
                    dismiss();
                }
            }
        });
        button.setImageResource(R.drawable.website);
        //int BUTTON_SIZE = 100;
        layout.addView(button, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //
        setContentView(layout);
    }
    public void setDelayDismiss(long delayMillisHide)
    {
        mHandler.sendEmptyMessageDelayed(MESSAGE_DISMISS, delayMillisHide);
    }
    public void cancelDelayDismiss()
    {
        mHandler.removeMessages(MESSAGE_DISMISS);
    }
    private void setMsg(String strMsg, String strLinkUrl)
    {
        mStrMsg = strMsg;
        mStrLinkUrl = strLinkUrl;

        View textView = mMainLayout.findViewById(ID_TEXT_MSG);
        if (textView != null)
            ((TextView)textView).setText(mStrMsg);
    }
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDialog = null;
    }
}
