package com.sails.hkiademo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sails.engine.LocationRegion;
import com.sails.engine.core.model.GeoPoint;

import java.util.List;

/**
 * Created by Rocky on 2014/1/16.
 */
public class StartEndManager extends RelativeLayout {

    LocationRegion start = null;
    LocationRegion end = null;
    public static LocationRegion Here= new LocationRegion();
    PathRoutingManager pm;
    SearchManager searchManager;
    NotificationManager nm;
    SlidingTransferManager sm;
    AnimationController amc;
    TextView startName, endName;
    ImageView startImage, endImage, exchange;
    RelativeLayout startFrame, endFrame;
    Button beginNavi;
    CheckBox elevatorOnly;
    boolean isSetStartPoint = false;
    boolean isSetEndPoint = false;


    public StartEndManager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setControlRes(final PathRoutingManager pm, SearchManager searchManager, NotificationManager nm, SlidingTransferManager sm) {
        this.pm = pm;
        this.searchManager = searchManager;
        this.nm = nm;
        this.sm = sm;
        this.amc = new AnimationController();
        this.pm.setStratEndManager(this);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.startend_layout, null);
        startName = (TextView) view.findViewById(R.id.startNameText);
        endName = (TextView) view.findViewById(R.id.endNameText);
        endImage = (ImageView) view.findViewById(R.id.endpoint);
        startImage = (ImageView) view.findViewById(R.id.startpoint);
        exchange = (ImageView) view.findViewById(R.id.exchange);
        beginNavi = (Button) view.findViewById(R.id.navibutton);
        startFrame = (RelativeLayout) view.findViewById(R.id.startframe);
        endFrame = (RelativeLayout) view.findViewById(R.id.endframe);
        elevatorOnly = (CheckBox) view.findViewById(R.id.noStairCheck);
        startName.setOnClickListener(clickListener);
        endName.setOnClickListener(clickListener);
        exchange.setOnClickListener(clickListener);
        beginNavi.setOnClickListener(clickListener);
        this.addView(view);
        this.setVisibility(INVISIBLE);
        ((MainActivity)getContext()).placeholderFragment.slidingTransferLayout.setVisibility(INVISIBLE);
        final SharedPreferences sp1=((MainActivity) getContext()).getPreferences(Context.MODE_PRIVATE);
        boolean clicked=sp1.getBoolean("elevatorOnly",false);
        if (clicked){
            elevatorOnly.setChecked(true);
            pm.mSailsMapView.getRoutingManager().setRouteMode(com.sails.engine.PathRoutingManager.ESCALATOR_ONLY);
        }else{
            elevatorOnly.setChecked(false);
            pm.mSailsMapView.getRoutingManager().setRouteMode(com.sails.engine.PathRoutingManager.NORMAL_ROUTING);
        }
        elevatorOnly.setText(getResources().getString(R.string.elevator_only));

        elevatorOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    pm.mSailsMapView.getRoutingManager().setRouteMode(com.sails.engine.PathRoutingManager.ELEVATOR_ONLY);

                    sp1.edit().putBoolean("elevatorOnly",true).commit();
                }else{
                    pm.mSailsMapView.getRoutingManager().setRouteMode(com.sails.engine.PathRoutingManager.NORMAL_ROUTING);
                    sp1.edit().putBoolean("elevatorOnly",false).commit();
                }
            }
        });
    }
    public LocationRegion getStart() {
        return start;
    }
    public LocationRegion getEnd() {
        return end;
    }
    public void setStartPoint(LocationRegion lr) {
        this.start = lr;
        startName.setText(start.getName());
        startName.setTextColor(Color.parseColor("#555555"));
        startImage.setImageResource(R.drawable.start);
        beginNavi.setText(getResources().getString(R.string.start_preview));
        beginNavi.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        if (isSetStartPoint)
            show();
    }
    public boolean isSetStartPoint() {
        if(this.start==null)
            return false;
        return true;
    }
    public boolean isSetEndPoint() {
        if(this.end==null)
            return false;
        return true;
    }
    public void setEndPoint(LocationRegion lr) {
        this.end = lr;
        endName.setText(end.getName());
        endName.setTextColor(Color.parseColor("#555555"));
        if (isSetEndPoint)
            show();
    }

    public void exchangeStartEnd() {
        if(start==Here)
            return;
//        if (pm.mSails.isInThisBuilding()) {
//            if (this.start == null || this.end == null)
//                return;
//        }

        LocationRegion lr = this.start;
        this.start = this.end;
        this.end = lr;
        if (start != null) {
            startName.setText(this.start.getName());
            startName.setTextColor(Color.parseColor("#555555"));
        } else {
            startName.setText(getResources().getString(R.string.set_start_point));
            startName.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
        if (end != null) {
            endName.setText(this.end.getName());
            endName.setTextColor(Color.parseColor("#555555"));//getResources().getColor(android.R.color.darker_gray));
        } else {
            endName.setText(getResources().getString(R.string.set_end_point));
            endName.setTextColor(getResources().getColor(android.R.color.holo_red_light));
        }
        ((MainActivity)getContext()).placeholderFragment.showArrangeModeMarker();
    }

    public void show() {

        if (!isSetStartPoint && !isSetEndPoint) {
            if (pm.mSails.isInThisBuilding()) {
                startName.setText(getResources().getString(R.string.myposition));
                startName.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                startImage.setImageResource(R.drawable.mylocation);
                beginNavi.setText(getResources().getString(R.string.start_navi));
                start=Here;
            } else {
                startName.setText(getResources().getString(R.string.set_start_point));
                startName.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                startImage.setImageResource(R.drawable.start);
                beginNavi.setText(getResources().getString(R.string.start_preview));
                beginNavi.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        }
        if(start!=Here) {
            beginNavi.setText(getResources().getString(R.string.start_preview));
            beginNavi.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            ((MainActivity)getContext()).placeholderFragment.cancelNaviText.setText(getResources().getString(R.string.cancel_preview));
        } else {
            beginNavi.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));

        }
        isSetEndPoint = false;
        isSetStartPoint =false;
        this.setVisibility(VISIBLE);
        ((MainActivity)getContext()).setMode(((MainActivity)getContext()).placeholderFragment.ARRANGE_MODE);
    }

    public void dismiss() {
        this.setVisibility(INVISIBLE);
    }

    public void cancel() {
        this.setVisibility(INVISIBLE);
        start = null;
        end = null;
        isSetEndPoint=false;
        isSetStartPoint=false;
    }

    OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == startName) {
                isSetStartPoint = true;
                searchManager.openView();
                Toast.makeText(getContext(),R.string.start_info, Toast.LENGTH_LONG).show();
            } else if (v == endName) {
                isSetEndPoint = true;
                searchManager.openView();
                Toast.makeText(getContext(),R.string.end_info, Toast.LENGTH_LONG).show();
            } else if (v == exchange) {
                exchangeStartEnd();
            } else if (v == beginNavi) {
                if (start == null) {
                    Toast t = Toast.makeText(getContext(), getResources().getString(R.string.set_start_point), Toast.LENGTH_SHORT);
                    t.show();
                    return;
                } else if (end == null) {
                    Toast t = Toast.makeText(getContext(), getResources().getString(R.string.set_end_point), Toast.LENGTH_SHORT);
                    t.show();
                    return;
                }
                if(start==Here) {
                    ((MainActivity)getContext()).placeholderFragment.cancelNaviText.setText(getResources().getString(R.string.cancel_navi));

                } else {
                    ((MainActivity)getContext()).placeholderFragment.cancelNaviText.setText(getResources().getString(R.string.cancel_preview));

                }
                Runnable runnable=new Runnable() {
                    @Override
                    public void run() {
                        List<GeoPoint> geoPointList=pm.getCurrentFloorRoutingGeoPointList();
                        if(getContext()!=null)
                            ((MainActivity)getContext()).mSailsMapView.autoSetMapZoomAndView(geoPointList);

                    }
                };
                new Handler().postDelayed(runnable, 1000);
                pm.setStart(start);
                pm.setTarget(end);
                pm.mSails.clearRouteCache();
                pm.enableHandler();
                setVisibility(INVISIBLE);
            }
        }
    };

}
