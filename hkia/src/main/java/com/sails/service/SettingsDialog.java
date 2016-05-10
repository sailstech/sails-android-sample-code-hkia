package com.sails.service;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.sails.hkiademo.R;

/**
 * Created by eddiehua on 2014/4/9.
 */
public class SettingsDialog extends Dialog implements Switch.OnCheckedChangeListener {
    private static final int ID_SWITCH_Service = 1;
    private static final int ID_SWITCH_VIBRATE = 2;
    //private static final int FONT_SIZE = 26;
    private TextView    mTvCheckInterval = null;
    private LinearLayout    mCkIntervalLayout = null;
    private Switch      mSwVibrate = null;

    public SettingsDialog(Context context) {
        super(context);
        Config.loadConfig(context);
        setTitle(R.string.settings);
        //----
        final WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        final Display screen = wm.getDefaultDisplay();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        screen.getMetrics(displaymetrics);
        float fScreenDensity = displaymetrics.density;
        //----

        final int nMargin = (int) (16 * fScreenDensity);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        //layout.setPadding(nPadding, 0, nPadding, 0);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(nMargin, nMargin, nMargin, nMargin);
        Switch swService = new Switch(context);
        swService.setId(ID_SWITCH_Service);
        //swService.setTextSize(FONT_SIZE);
        swService.setText(R.string.push_msg_service);
        swService.setOnCheckedChangeListener(this);
        layout.addView(swService, params);

        // 檢查頻率　1分鐘（較耗電） - 5分鐘 - 10分鐘 - 15分鐘...
        mCkIntervalLayout = new LinearLayout(context);
        LinearLayout.LayoutParams chkTvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mTvCheckInterval = new TextView(context);
        //mTvCheckInterval.setTextSize(FONT_SIZE);
        mCkIntervalLayout.addView(mTvCheckInterval, chkTvParams);
        //
        SeekBar seekbar = new SeekBar(context);
        seekbar.setMax(12);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTextView(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        LinearLayout.LayoutParams chkSeekParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        mCkIntervalLayout.addView(seekbar, chkSeekParams);
        layout.addView(mCkIntervalLayout, params);

        // 通知時是否 震動
        mSwVibrate = new Switch(context);
        mSwVibrate.setId(ID_SWITCH_VIBRATE);
        //swVibrate.setTextSize(FONT_SIZE);
        mSwVibrate.setText(R.string.notification_vibrate);
        mSwVibrate.setOnCheckedChangeListener(this);
        layout.addView(mSwVibrate, params);

        setContentView(layout);

        // -------- 設定值 -----------
        swService.setChecked(Config.serviceEnable);
        updateLayoutByServiceEnable(Config.serviceEnable);
        int pos = MinuteToProgress(Config.intervalCheckSecond / 60);    // 除以６０秒後才是分鐘
        // 取得 default value
        seekbar.setProgress(pos);
        updateTextView(pos);
        mSwVibrate.setChecked(Config.vibrateMills > 0);
    }

    private int MinuteToProgress(int nMinute)
    {
        return nMinute / 5;
    }
    private int ProgressToMinute(int pos)
    {
        int nMin = pos * 5;
        if (nMin == 0)
            nMin = 1;
        return nMin;
    }
    // 檢查頻率　1分鐘（較耗電） - 5分鐘 - 10分鐘 - 15分鐘...
    private void updateTextView(int pos)
    {
        int nMin = ProgressToMinute(pos);

        mTvCheckInterval.setText(String.format(getContext().getResources().getString(R.string.check_interval_min_param), nMin));
        Config.intervalCheckSecond = nMin * 60;
    }

    private void updateLayoutByServiceEnable(boolean enable)
    {
        final int nViewShow = enable?View.VISIBLE:View.GONE;
        mCkIntervalLayout.setVisibility(nViewShow);
        mSwVibrate.setVisibility(nViewShow);
    }
/*
    private void enableService(Context context, boolean bEnable)
    {
        final ComponentName service = new ComponentName(context.getPackageName(),
                "com.sails.service.SailsService");
        final PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(service,
                bEnable? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
*/
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case ID_SWITCH_Service:
                Config.serviceEnable = isChecked;
                //enableService(getContext(), Config.serviceEnable);
                updateLayoutByServiceEnable(Config.serviceEnable);
                break;
            case ID_SWITCH_VIBRATE:
                Config.vibrateMills = isChecked ? Config.DEFAULT_VIBRATE_MS : 0;
                break;
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Context context = getContext();
        Config.saveConfig(context);
        if (Config.serviceEnable)
            SailsService.startCheckByConfig(context);
    }
}
