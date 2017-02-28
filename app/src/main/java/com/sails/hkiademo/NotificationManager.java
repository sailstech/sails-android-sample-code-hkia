package com.sails.hkiademo;

import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.sails.engine.LocationRegion;

import java.util.List;

/**
 * Created by Richard on 2014/1/9.
 */
public class NotificationManager {
    private final View layoutView;
    private TextView mainTextView;
    private TextView subTextView;
    private ImageView infoImageView;

    private Animation startAnimation=null,endAnimation=null;
    private LocationRegion locationRegion=new LocationRegion();
    private boolean notificationON=false;
    private AnimationController advanceanimation;

    NotificationManager(View layoutView) {
        this.layoutView=layoutView;
        layoutView.setVisibility(View.GONE);
        advanceanimation=new AnimationController();
    }
    public void setGoIntoSlidingInfoAnimation(){
        advanceanimation.slideOut(layoutView,500,0);
        layoutView.setVisibility(View.GONE);
    }

    public void setStartAnimation(Animation startAnimation) {
        this.startAnimation = startAnimation;
    }
    public void setEndAnimation(Animation endAnimation) {
        this.endAnimation=endAnimation;
    }
    public void openNotification(final List<LocationRegion> lrlist) {
        if(lrlist==null||lrlist.size()<1)
            return;

        if(mainTextView==null)
            return;
        final String info=mainTextView.getContext().getString(R.string.detail_info);

        if(notificationON) {
            if(endAnimation!=null) {
                layoutView.startAnimation(endAnimation);
                endAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        ((ImageView)layoutView.findViewById(R.id.arrangepath)).setImageResource(R.drawable.detail);
                        ((TextView)layoutView.findViewById(R.id.textViewArrange)).setText(R.string.detail);
                        mainTextView.setText(String.format(info, lrlist.size()));
                        subTextView.setVisibility(View.GONE);
                        if(startAnimation!=null)
                            layoutView.startAnimation(startAnimation);
                        layoutView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                return;
            }
        }
        ((ImageView)layoutView.findViewById(R.id.arrangepath)).setImageResource(R.drawable.detail);
        ((TextView)layoutView.findViewById(R.id.textViewArrange)).setText(R.string.detail);
        mainTextView.setText(String.format(info, lrlist.size()));
        subTextView.setVisibility(View.GONE);
        if(startAnimation!=null)
            layoutView.startAnimation(startAnimation);
        layoutView.setVisibility(View.VISIBLE);
        notificationON=true;
    }
    // ----
    private void updateLayoutViewByLocationRegion()
    {
//        // 為了 AD MODE-不支援 導航，所以要關掉
//        boolean bShowBeaconMsg = locationRegion.beacon != null;
//        View arrangeLayout = layoutView.findViewById(R.id.arrangeLayout);
//        if (arrangeLayout != null) {
//            arrangeLayout.setVisibility(bShowBeaconMsg ? View.VISIBLE : View.INVISIBLE);
//            arrangeLayout.setTag(locationRegion.beacon);
//        }
//
//        if (bShowBeaconMsg)
//        {
//            Log.i("DEBUG", "locationRegion.beacon.store_link : " + locationRegion.beacon.store_link);
//            ((ImageView)layoutView.findViewById(R.id.arrangepath)).setImageResource(R.drawable.info);
//            layoutView.findViewById(R.id.textViewArrange).setVisibility(View.GONE);
//        } else {
//            layoutView.findViewById(R.id.textViewArrange).setVisibility(View.VISIBLE);
//        }
        ((ImageView) layoutView.findViewById(R.id.arrangepath)).setImageResource(R.drawable.takeme);
        ((TextView) layoutView.findViewById(R.id.textViewArrange)).setText(R.string.arrangepath);

        setLayoutView(locationRegion);
        if(startAnimation!=null)
            layoutView.startAnimation(startAnimation);
        layoutView.setVisibility(View.VISIBLE);
    }
    public void openNotification(LocationRegion lr) {
        if(lr==null)
            return;

        if(locationRegion!=lr) {
            locationRegion=lr;
            if(notificationON) {
                if(endAnimation!=null) {
                    layoutView.startAnimation(endAnimation);
                    endAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            updateLayoutViewByLocationRegion();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    return;
                }
            }
            updateLayoutViewByLocationRegion();
            notificationON=true;
        }
    }
    public interface OnInfoClickListener{
        void onClick(LocationRegion lr);
    }
    OnInfoClickListener onInfoClickListener=null;
    void setOnInfoClickListener(OnInfoClickListener listener) {
        onInfoClickListener=listener;
    }
    private void setLayoutView(final LocationRegion lr) {
        if(mainTextView==null)
            return;
        if(infoImageView!=null) {
            if(lr.url==null) {
                layoutView.setOnClickListener(null);
                infoImageView.setVisibility(View.GONE);
            }
            else {
                layoutView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(onInfoClickListener!=null)
                            onInfoClickListener.onClick(lr);
                    }
                });
                infoImageView.setVisibility(View.VISIBLE);
            }
        }
        mainTextView.setText(lr.getName());
        if(subTextView==null)
            return;
        subTextView.setVisibility(View.VISIBLE);
        subTextView.setText(lr.getFloorDescription());

    }
    public void setMainTextView(int id) {
            mainTextView=(TextView)layoutView.findViewById(id);
    }
    public void setSubTextView(int id) {
        subTextView=(TextView)layoutView.findViewById(id);
    }
    public void setInfoImageView(int id) {
        infoImageView=(ImageView) layoutView.findViewById(id);
    }
    public void closeNotification(){
        if(notificationON) {
            if(endAnimation!=null) {
                layoutView.startAnimation(endAnimation);
                endAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        layoutView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            } else {
                layoutView.setVisibility(View.GONE);
            }
            locationRegion=new LocationRegion();
            notificationON=false;
        }
    }

}
