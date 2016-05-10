package com.sails.ui;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by richard on 2016/5/11.
 */
public class SwipeTouchListener implements View.OnTouchListener {
    View.OnClickListener listener=null;

    static final String logTag = "ActivitySwipeDetector";
    private final RelativeLayout layout;
    private Activity activity;
    static final int MIN_DISTANCE = 100;// TODO change this runtime based on screen resolution. for 1920x1080 is to small the 100 distance
    private float downX, downY, upX, upY;
    private int height;
    private static final float TRAVEL_MIN=0.25f;
    private boolean travelDone=false;
    // private MainActivity mMainActivity;
    public void setClickListener(View.OnClickListener listener) {
        this.listener=listener;
    }
    public SwipeTouchListener(RelativeLayout layout) {
        this.layout = layout;

    }
    boolean click=false;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int X = (int) event.getRawX();
        final int Y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                if(X-downX>100||Y-downY>100)
                    click=false;
                if(Y-downY>0) {
                    RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) layout.getLayoutParams();
                    params.topMargin = (int) (Y - downY);
                    layout.setLayoutParams(params);
                    float alpha=(Y-downY)/(float)(height)/TRAVEL_MIN;
                    layout.setAlpha(1-alpha);
                }else {
                    downY=Y;
                }
                return true;
            }
            case MotionEvent.ACTION_DOWN: {
                travelDone=false;
                height=layout.getHeight();
                downX = X;
                downY = Y;
                click=true;
                return true;
            }
            case MotionEvent.ACTION_UP: {

                float alpha=(Y -downY)/(float)(height)/TRAVEL_MIN;
                if(alpha>=1) {
//                    exit();
                    return true;
                }
                if(click&&listener!=null)
                    listener.onClick(null);
                RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) layout.getLayoutParams();
//                    linearLayout.setPadding(linearLayout.getPaddingLeft(),(int)(y-downY),linearLayout.getPaddingRight(),linearLayout.getPaddingBottom());
                params.topMargin=(int)0;
                layout.setAlpha(1);
                layout.setLayoutParams(params);

                return false; // no swipe horizontally and no swipe vertically
            }// case MotionEvent.ACTION_UP:
        }
        return false;
    }

}
