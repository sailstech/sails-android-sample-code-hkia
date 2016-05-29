package com.sails.hkiademo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.sails.engine.LocationRegion;
import com.sails.engine.SAILS;
import com.sails.engine.SAILSMapView;
import com.sails.engine.core.model.GeoPoint;
import com.sails.engine.overlay.ListOverlay;
import com.sails.engine.overlay.Marker;
import com.sails.engine.overlay.PolygonalChain;

import com.sails.engine.overlay.PolylineWithArrow;
import com.sails.engine.overlay.ScreenDensity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rocky on 2014/1/11.
 */
class PathRoutingManager extends Handler {

    public boolean enable = false;

    WeakReference<Activity> pointer;
    SlidingTransferManager mSTL;
    StartEndManager mSEM;
    ListOverlay listOverlay = new ListOverlay();
    Paint routePaint = new Paint();
    Paint routeStrokePaint = new Paint();

    Context context;
    SAILS mSails;
    SAILSMapView mSailsMapView;

    LocationRegion lr = null;
    LocationRegion start = null; // start route location region
    List<LocationRegion> regionlist = null;
    List<SAILS.GeoNode> resultGNlist = null;
    private boolean updateSlidingTransferInfo;
    public int processCount = 0;
    public boolean isSuccess=false;
    public boolean isArrive=false;

    PathRoutingManager(Activity input, SAILS mSails, SAILSMapView mSailsMapView, SlidingTransferManager mSTL) {
        pointer = new WeakReference<Activity>(input);
        context = input;
        routePaint.setStyle(Paint.Style.STROKE);
//        routePaint.setAlpha(200);
        routePaint.setStrokeWidth(5 * ScreenDensity.density);
        routePaint.setStrokeJoin(Paint.Join.ROUND);
        routePaint.setStrokeCap(Paint.Cap.ROUND);
        routePaint.setFilterBitmap(true);
        routePaint.setAntiAlias(true);
        routeStrokePaint.setColor(Color.argb(255,255,255,255));//parseColor("#333333"));
        routeStrokePaint.setStyle(Paint.Style.STROKE);
//        routeStrokePaint.setAlpha(200);
        routeStrokePaint.setStrokeWidth(12 * ScreenDensity.density);
        routeStrokePaint.setStrokeJoin(Paint.Join.ROUND);
        routeStrokePaint.setStrokeCap(Paint.Cap.ROUND);
        routeStrokePaint.setFilterBitmap(true);
        routeStrokePaint.setAntiAlias(true);

//        routePaint.setShader(new LinearGradient(0, 0, 0, 20, Color.BLACK, Color.WHITE, Shader.TileMode.REPEAT));
        this.mSails = mSails;
        this.mSailsMapView = mSailsMapView;
        this.mSTL = mSTL;
        this.mSTL.importSAILS(mSails, mSailsMapView);
    }

    @Override
    public void handleMessage(Message msg) {

        if (enable) {
            if (pointer.get() != null) {
                if (regionlist == null) {
                    regionlist = mSails.getLocationRegionList(lr.getFloorName());
                }
                routingProcess();
                processCount++;
                //do something when after first routing process is success or not success
                if (processCount == 1) {
                    if (isSuccess) {
                        ((MainActivity) context).setMode(((MainActivity) context).placeholderFragment.NAVIGATION_MODE);
                        ((MainActivity) context).placeholderFragment.floorRelativeLayout.setVisibility(View.INVISIBLE);
                        ((MainActivity) context).placeholderFragment.searchBarLinearLayout.setVisibility(View.INVISIBLE);
                        ((MainActivity) context).placeholderFragment.cancelNaviLayout.setVisibility(View.VISIBLE);
                        ((MainActivity) context).placeholderFragment.floorIndicator.setVisibility(View.VISIBLE);
                        mSEM.cancel();
                        mSTL.showInfo();
                        ((MainActivity) context).getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    } else {
                        if(!isArrive){
                        mSTL.showRouteFailInfo();
                        disableHandler();
                        }
                    }
                }
            }
            this.sleep(100);
        }
    }

    void setStratEndManager(StartEndManager sm){
        this.mSEM=sm;
    }

    public void sleep(long delayMillis) {
        this.removeMessages(0);
        sendMessageDelayed(obtainMessage(0), delayMillis);
    }

    public void enableHandler(LocationRegion lr) {
        mSailsMapView.getOverlays().clear();
        mSailsMapView.getDynamicOverlays().add(listOverlay);
        if (this.lr == null || lr.getFloorNumber() != this.lr.getFloorNumber())
            regionlist = null;

        if (this.lr != lr)
            updateSlidingTransferInfo = true;

        this.lr = lr;
        this.sleep(200);
        this.enable = true;

        if (mSails.isLocationEngineStarted() && mSailsMapView.isInLocationFloor() && mSails.isLocationFix()) {
            mSailsMapView.setAnimationMoveMapToMyLocation();
        } else {
            SAILS.GeoNode geo = new SAILS.GeoNode(lr.getCenterLongitude(), lr.getCenterLatitude());
            mSailsMapView.setAnimationMoveMapTo(new GeoPoint(geo.latitude, geo.longitude));
        }
    }

    public void setTarget(LocationRegion lr) {
        if (this.lr != lr)
            updateSlidingTransferInfo = true;
        this.lr = lr;
    }

    public void setStart(LocationRegion lr) {
        this.start = lr;
    }

    public void enableHandler() {
        mSailsMapView.getOverlays().clear();
        mSailsMapView.getOverlays().add(listOverlay);
        if (this.lr == null)
            return;
        else
            regionlist = null;

        this.sleep(200);
        this.enable = true;
        this.isArrive=false;

        if (start == StartEndManager.Here) {
            if (!mSailsMapView.isCenterLock()) {
                mSailsMapView.setMode(mSailsMapView.getMode() | SAILSMapView.LOCATION_CENTER_LOCK);
                mSailsMapView.setMode(mSailsMapView.getMode() | SAILSMapView.FOLLOW_PHONE_HEADING);
            } else
                mSailsMapView.setMode(mSailsMapView.getMode() | SAILSMapView.FOLLOW_PHONE_HEADING);
        } else {
            if (!start.getFloorName().equals(mSailsMapView.getCurrentBrowseFloorName())) {
                mSailsMapView.loadFloorMap(start.getFloorName());
                mSailsMapView.setAnimationToZoom((byte) 19);
            }

            SAILS.GeoNode geo = new SAILS.GeoNode(start.getCenterLongitude(), start.getCenterLatitude());
            mSailsMapView.setAnimationMoveMapTo(new GeoPoint(geo.latitude, geo.longitude));
        }
    }

    public void disableHandler() {
        this.enable = false;
        processCount=0;
        isSuccess=false;
        regionlist = null;
        resultGNlist = null;
        listOverlay.getOverlayItems().clear();
        mSailsMapView.getOverlays().clear();
        mSailsMapView.redraw();
        mSailsMapView.getMarkerManager().clear();
        mSTL.closeInfo();
        ((MainActivity) context).setMode(((MainActivity) context).placeholderFragment.NORMAL_MODE);
        ((MainActivity) context).placeholderFragment.slidingTransferLayout.setVisibility(View.INVISIBLE);
        ((MainActivity) context).placeholderFragment.left.setVisibility(View.VISIBLE);
        ((MainActivity) context).placeholderFragment.right.setVisibility(View.VISIBLE);
        ((MainActivity) context).getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ((MainActivity) context).placeholderFragment.backNavi.setVisibility(View.INVISIBLE);
    }

    public boolean isRoutingEnable() {
        return this.enable;
    }

    public List<SAILS.GeoNode> getResultGNlist() {
        return resultGNlist;
    }

    public void setShowPathByFloorNumber(int floornumaber) {

    }

    private List<List<GeoPoint>> currentFloorRoutingGeoPointList = new ArrayList<List<GeoPoint>>();

    public List<GeoPoint> getCurrentFloorRoutingGeoPointList() {
        List<GeoPoint> list=new ArrayList<GeoPoint>();
        for(List<GeoPoint> mL: currentFloorRoutingGeoPointList){
            for(GeoPoint mG: mL)
                list.add(mG);
        }
        return list;
    }

    public void routingProcess() {
        SAILS.RoutingInfo ri = null;

        if (mSails.isInThisBuilding()) {

            if (start == StartEndManager.Here) {
                if (mSails.amIHere(lr) && lr.getFloorNumber() == mSails.getFloorNumber()) {
                    isArrive=true;
                    disableHandler();
                    Toast toast = Toast.makeText(context, "Arrive.", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                routePaint.setColor(Color.argb(255,0,134,255));//parseColor("#333333"));

//                routePaint.setColor(0xFF35b3e5);
                mSailsMapView.getMarkerManager().clear();
                mSailsMapView.getMarkerManager().setLocationRegionMarker(lr, Marker.boundCenterBottom(pointer.get().getResources().getDrawable(R.drawable.destination)));

                //routing from current indoor position to destination.
                if (mSails.isLocationFix())
                    ri = mSails.route3D(lr);
            } else {
                routePaint.setColor(Color.argb(255,0,134,255));//parseColor("#333333"));


//                routePaint.setColor(0xFF85b038);
                mSailsMapView.getMarkerManager().clear();
                if(start!=null)
                mSailsMapView.getMarkerManager().setLocationRegionMarker(start, Marker.boundCenter(pointer.get().getResources().getDrawable(R.drawable.start_point)));
                mSailsMapView.getMarkerManager().setLocationRegionMarker(lr, Marker.boundCenterBottom(pointer.get().getResources().getDrawable(R.drawable.map_destination)));
                ri = mSails.route3D(start, lr);
            }
        } else {
            routePaint.setColor(Color.argb(255,0,134,255));//parseColor("#333333"));


//            routePaint.setColor(0xFF85b038);
            mSailsMapView.getMarkerManager().clear();
            if(start!=null)
                mSailsMapView.getMarkerManager().setLocationRegionMarker(start, Marker.boundCenter(pointer.get().getResources().getDrawable(R.drawable.start_point)));
            mSailsMapView.getMarkerManager().setLocationRegionMarker(lr, Marker.boundCenterBottom(pointer.get().getResources().getDrawable(R.drawable.map_destination)));
            ri = mSails.route3D(start, lr);
        }

        //update path draw
        if (ri!=null && ri.success) {
            isSuccess=true;
            resultGNlist = ri.getPathNodes();
            if (resultGNlist != null) {

                List<List<GeoPoint>> gplist = new ArrayList<List<GeoPoint>>();
                List<GeoPoint> list=new ArrayList<GeoPoint>();
                gplist.add(list);
                currentFloorRoutingGeoPointList.clear();
                boolean changeline=true;
                int index=0;
                for (SAILS.GeoNode mGN : resultGNlist) {

                    if (Math.abs(mGN.latitude) <= 90 && Math.abs(mGN.longitude) <= 180) {

                        if (mSailsMapView.getCurrentBrowseFloorName() == null || mGN.floorname == null)
                            continue;

                        if (mGN.floorname.equals(mSailsMapView.getCurrentBrowseFloorName())) {
                            gplist.get(index).add(new GeoPoint(mGN.latitude, mGN.longitude));
                            changeline = false;
                        } else {
                            if (!changeline) {
                                index++;
                                list=new ArrayList<GeoPoint>();
                                gplist.add(list);
                                changeline = true;
                            }
                        }
                    }
                }

                if (gplist.size() == currentFloorRoutingGeoPointList.size()) {
                    int j = 0;
                    difference:
                    {
                        for (List<GeoPoint> mL : gplist) {
                            int i = 0;

                            if (mL.size() == currentFloorRoutingGeoPointList.get(j).size()) {

                                for (GeoPoint gp : mL) {
                                    if (gp.latitude != currentFloorRoutingGeoPointList.get(j).get(i).latitude)
                                        break difference;
                                    if (gp.longitude != currentFloorRoutingGeoPointList.get(j).get(i).longitude)
                                        break difference;
                                    i++;
                                }
                            }
                            j++;
                        }
                        return;
                    }
                }

                listOverlay.getOverlayItems().clear();
                for (List<GeoPoint> mL : gplist) {
                    PolygonalChain mPCWay = new PolygonalChain(mL);
                    currentFloorRoutingGeoPointList.add(mL);
                    //polyline create one time only
                    PolylineWithArrow pathDraw = new PolylineWithArrow(new PolygonalChain(null), routePaint, 30);
                    PolylineWithArrow pathDrawStroke = new PolylineWithArrow(new PolygonalChain(null), routeStrokePaint, 30);
                    pathDraw.setPolygonalChain(mPCWay);
                    pathDrawStroke.setPolygonalChain(mPCWay);
                    listOverlay.getOverlayItems().add(pathDrawStroke);
                    listOverlay.getOverlayItems().add(pathDraw);
                }
                mSailsMapView.redraw();
                if (updateSlidingTransferInfo) {
                    mSTL.updateInfo(resultGNlist, true);
                    updateSlidingTransferInfo = false;
                } else
                    mSTL.updateInfo(resultGNlist, false);
            }
        }else
            isSuccess=false;
    }
}
