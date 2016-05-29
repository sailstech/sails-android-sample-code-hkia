package com.sails.hkiademo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
//import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import com.sails.sailscloud.BuildingSelectDialog;
//import com.sails.sailscloud.SignInActivity;
//import com.sails.cloud.SAILSBuilding;
//import com.sails.cloud.SAILSCloud;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;
import com.sails.engine.Beacon;
import com.sails.engine.LocationRegion;
import com.sails.engine.SAILS;
import com.sails.engine.SAILSMapView;
import com.sails.engine.core.model.GeoPoint;
import com.sails.engine.overlay.Marker;
import com.sails.engine.overlay.ScreenDensity;
import com.sails.poi.POIAssetsAdapter;
import com.sails.service.Version;
import com.sails.poi.POIActivity;
import com.sails.ui.DirectionIndicator;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;



public class MainActivity extends Activity implements PopupMenu.OnMenuItemClickListener, LocationListener {

    static String TOKEN="831794496a0f4de9aa0651d97610733f";
    static String BUILDING_ID="57317a41e62e7a7b59000459";

    boolean trueNaviMode=false;

    static boolean BLE=true;
    static boolean TEST=false;
    static boolean BLUEDOT=true;
    private static final String TAG = "MainActivity";
    public static boolean isForeground = false;     // Eddie Hua
    PlaceholderFragment placeholderFragment = new PlaceholderFragment();
    static SAILSMapView mSailsMapView = null;
    static SAILS mSails;
    static PathRoutingManager mRoutingHandler;
    static AnimationController amc;
    static StartEndManager mStartEndManager;
    static SearchManager mSearchManager;
    static String LANGUAGE="en";
//    static String LANGUAGE="zh_TW";
    static boolean FLOOR_GUIDE=false;
    private static String sel_type, sel_subtype;
    private String POI_Id="0205001";

    public void setMode(int mode) {
        placeholderFragment.mode = mode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Locale locale= new Locale("en");
        LocationRegion.FONT_LANGUAGE=LocationRegion.NORMAL;
//        if(LANGUAGE.equals("en")) {
//            LocationRegion.FONT_LANGUAGE=LocationRegion.ENGLISH;
//            locale = new Locale("en");
//        }
//        else if(LANGUAGE.equals("zh_TW")) {
//            LocationRegion.FONT_LANGUAGE = LocationRegion.CHINESE_BIG5;
//            locale = new Locale("zh","TW");
//        }
//        else if(LANGUAGE.equals("zh_CH")) {
//            LocationRegion.FONT_LANGUAGE = LocationRegion.CHINESE_SM;
//            locale = new Locale(LANGUAGE);
//        }

        loadPOIListProcedure();
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        // ------ Eddie Try : ACTION_VIEW Geo URI ------
        Intent intent = getIntent();
        if (intent != null) {
            String strAction = intent.getAction();
            if (strAction != null && 0 == strAction.compareTo(Intent.ACTION_VIEW)) {
                Uri uri = intent.getData();

                //Log.i(TAG, "ACTION_VIEW Uri = " + uri.toString());
                //Log.i(TAG, "ACTION_VIEW Uri Scheme = " + uri.getScheme());
                String strGeoPos = uri.getSchemeSpecificPart();
                //Log.i(TAG, "ACTION_VIEW Uri SchemeSpecificPart = " + strGeoPos);
                String[] geoPos = strGeoPos.split(",");
                if (geoPos != null && geoPos.length == 2) {
                    double dLat = Double.valueOf(geoPos[0]);
                    double dLong = Double.valueOf(geoPos[1]);
                    Log.i(TAG, "ACTION_VIEW Lat = " + dLat + " Long = " + dLong);
                }
                // 要前往指定座標
            }
            //Log.i(TAG, "MainActivity onCreate : Action = " + strAction);
        }
        // ---------

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (savedInstanceState == null) {

            getFragmentManager().beginTransaction()
                    .add(R.id.container, placeholderFragment)
                    .commit();

        }
        ScreenDensity.density = getResources().getDisplayMetrics().density;
        amc = new AnimationController();
    }

    private void loadPOIListProcedure() {
        POIAssetsAdapter.Import(this,"poi.json");
    }


    public static final int SIGN_IN = 1;


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        SharedPreferences sp = getSharedPreferences("signin", MODE_PRIVATE);
//        final String username = sp.getString("username", "");
//        final String password = sp.getString("password", "");
//        SharedPreferences sp2 = getSharedPreferences("user_cookie", MODE_PRIVATE);
//        final String cookie = sp2.getString("cookie", "");
//        if (username.length() == 0 || password.length() == 0) {
//            Intent i = new Intent(MainActivity.this, SignInActivity.class);
//            if (savedInstanceState == null)
//                startActivityForResult(i, SIGN_IN);
//        } else {
//            placeholderFragment.progressdg = ProgressDialog.show(this, "", getString(R.string.processing), true, false);
//            sailsUser = new SAILSCloud.SAILSUser(username, password);
//            sailsUser.setCookie(cookie);
//            sailsCloud.setUser(sailsUser);
//            SharedPreferences sp1 = getSharedPreferences("saved_cloud_building", MODE_PRIVATE);
//            BUILDING_ID = sp1.getString("buildingID", "0");
//            SharedPreferences sp3 = getSharedPreferences("user_token", MODE_PRIVATE);
//            TOKEN = sp3.getString("token", "");
//            placeholderFragment.openBuilding(TOKEN, BUILDING_ID);
//        }
        try {
            ((TextView)findViewById(R.id.tvVersion)).setText("V"+getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
            if(TEST)
                findViewById(R.id.tvVersion).setVisibility(View.VISIBLE);
            else
                findViewById(R.id.tvVersion).setVisibility(View.INVISIBLE);

        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        placeholderFragment.progressdg = ProgressDialog.show(this, "", getString(R.string.processing), true, false);
        if(!BLE)
            BUILDING_ID="57317a41e62e7a7b59000459";
        placeholderFragment.openBuilding(TOKEN, BUILDING_ID);

    }



    boolean locationStart = false;
    boolean isPathHandlerEnabled = false;

    @Override
    protected void onResume() {
        super.onResume();
        isForeground = true;
        if (Version.DEBUG)
            Log.i(TAG, "MainActivity onResume");

        if (mSails != null && locationStart) {
            mSails.startLocatingEngine();
        }
        if (isPathHandlerEnabled) {
            mRoutingHandler.enable = true;
            mRoutingHandler.sleep(2000);
        }
        // Eddie Hua : Start GPS
        //SailsService.startGps(this, 10000, 0, this); // 因為無法　標註gps座標，所以先mark
        //BeaconMsgDialog.showMsg(this, "Test Beacon Message", "http://m.myqr.com.tw/5680-TEX4xN");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Version.DEBUG)
            Log.i(TAG, "MainActivity onPause");
        if (mSails != null) {
            if (mSails.isLocationEngineStarted()) {
                locationStart = true;
            } else {
                locationStart = false;
            }
            isPathHandlerEnabled = false;
            if (mRoutingHandler.enable) {
                isPathHandlerEnabled = true;
                mRoutingHandler.enable = false;
            }
            mSails.stopLocatingEngine();
        }
        mSailsMapView.onPause();

        // EddieHua : Stop GPS
        //SailsService.stopGps(this, this);
        // Start Sails Service
        //SailsService.startIntervalCheck(this, Version.DEBUG?40 : 80);
        isForeground = false;
    }

    public void onFunctionClick(View v) {
        if (this.placeholderFragment.mode == PlaceholderFragment.ARRANGE_MODE) {
            this.placeholderFragment.mode = PlaceholderFragment.NORMAL_MODE;
            mStartEndManager.cancel();
        }
        placeholderFragment.functionClickProcedure();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        setupMenuItemTitle(menu);
        return true;
    }

    public void onZoomInClick(View v) {
        mSailsMapView.zoomIn();
//        ((FrameLayout)placeholderFragment.rootView.findViewById(R.id.frameLayout)).removeView(mSailsMapView);
    }

    public void onZoomOutClick(View v) {
        mSailsMapView.zoomOut();
//        ((FrameLayout)placeholderFragment.rootView.findViewById(R.id.frameLayout)).addView(mSailsMapView);

    }

    public void onSettingClick(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        popup.setOnMenuItemClickListener(this);
        inflater.inflate(R.menu.main, popup.getMenu());
        setupMenuItemTitle(popup.getMenu());
//            if(mSearchManager.isReady)
//                popup.getMenu().findItem(R.id.action_promote).setVisible(true);
//            else
//                popup.getMenu().findItem(R.id.action_promote).setVisible(false);
        popup.show();
    }
    private void setupMenuItemTitle(Menu menu) {
        if(mSails.isLocationEngineStarted()) {
            menu.findItem(R.id.action_enable_location).setTitle(R.string.action_disable_location);
        } else {
            menu.findItem(R.id.action_enable_location).setTitle(R.string.action_enable_location);
        }
    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_enable_location:
                engineStartStopProcedure();
                return true;
            case R.id.action_aboutus:
                Intent aboutus = new Intent(this, AboutUsActivity.class);
                startActivity(aboutus);
                return true;
            default:
                return false;
        }
    }

    private void engineStartStopProcedure() {
        if (mSails != null) {
            if(mSails.isLocationEngineStarted()) {
                mSailsMapView.setLocatorMarkerVisible(false);
                mSailsMapView.invalidate();
                isPathHandlerEnabled = false;
                if (mRoutingHandler.enable) {
                    isPathHandlerEnabled = true;
                    mRoutingHandler.enable = false;
                }
                mSails.stopLocatingEngine();
                mSailsMapView.setMode(SAILSMapView.GENERAL);
            } else {
                mSailsMapView.setLocatorMarkerVisible(true);
                mSails.startLocatingEngine();
                mSailsMapView.setMode(SAILSMapView.LOCATION_CENTER_LOCK|SAILSMapView.FOLLOW_PHONE_HEADING);
            }
        }
    }

    public void onSearchClick(View v) {
        if (mSearchManager != null && mSearchManager.isReady) {
            if (this.placeholderFragment.mode == PlaceholderFragment.ARRANGE_MODE) {
                this.placeholderFragment.mode = PlaceholderFragment.NORMAL_MODE;
                mStartEndManager.cancel();
            }
            this.placeholderFragment.mode = placeholderFragment.NORMAL_MODE;
            placeholderFragment.functionManger.closeFunctionView();
            mSearchManager.openView();
        }

    }

    @Override
    public void onBackPressed() {
        if (placeholderFragment.showListView) {
            placeholderFragment.closeListView();
            return;
        }
        if (placeholderFragment.functionManger.isOpened()) {
            placeholderFragment.functionManger.closeFunctionView();
            return;
        }
        if (mSearchManager != null && mSearchManager.isViewShow()) {
            mSearchManager.closeView();
            return;
        }
        if (mRoutingHandler.isRoutingEnable()) {
            mRoutingHandler.disableHandler();
            placeholderFragment.cancelNaviLayout.setVisibility(View.INVISIBLE);
            placeholderFragment.floorIndicator.setVisibility(View.INVISIBLE);
            placeholderFragment.searchBarLinearLayout.setVisibility(View.VISIBLE);
            placeholderFragment.floorRelativeLayout.setVisibility(View.VISIBLE);
            return;
        }
        if (mStartEndManager.getVisibility() == View.VISIBLE) {
            mStartEndManager.cancel();
            mSailsMapView.getMarkerManager().clear();
            this.placeholderFragment.mode = PlaceholderFragment.NORMAL_MODE;
            return;
        }

//        super.onBackPressed();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
//            case R.id.action_settings:
//                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onArrangePathClick(View v) {
        // Eddie Hua :
//        if (v.getTag() != null) {
//            Beacon beacon = (Beacon) v.getTag();
//            Log.i(TAG, "onArrangePathClick = " + beacon.store_link);
//            if (beacon.store_link != null) {
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse(beacon.store_link));
//                startActivity(i);
//            }
//        }
        Log.i(TAG, "onArrangePathClick placeholderFragment.mode = " + placeholderFragment.mode);

        if (placeholderFragment.mode == PlaceholderFragment.MULTIPLE_MARKER_WITH_SINGLE_MODE) {
            mSailsMapView.getMarkerManager().clear();
            if (placeholderFragment.highlightLocationRegion != null)
                mSailsMapView.getMarkerManager().setLocationRegionMarker(placeholderFragment.highlightLocationRegion, Marker.boundCenterBottom(getResources().getDrawable(R.drawable.map_destination)));
            placeholderFragment.highlightLocationRegion = null;
            placeholderFragment.mode = PlaceholderFragment.NORMAL_MODE;
        }
        if (placeholderFragment.mode == PlaceholderFragment.MULTIPLE_MARKER_MODE) {
            placeholderFragment.openListView(sel_type, sel_subtype);
        } else {
            mStartEndManager.show();
            placeholderFragment.notificationManager.closeNotification();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        boolean showlocatingmsg = false;

        FrameLayout frameLayoutFunction;
        //        Spinner spinnerMore;
        private View rootView;
        ExpandableListView eLV;
        ExpandableAdapter eAdapter;
        NotificationManager notificationManager;
        SlidingTransferManager mTransferLayout;
        ImageView left, right;

        ProgressDialog identifyIndoor;
        FunctionManager functionManger;
        LocationRegion startLocationRegion = null;
        LocationRegion endLocationRegion = null;
        LocationRegion highlightLocationRegion = null;
        List<LocationRegion> highlightLocationRegionList = null;
        LinearLayout searchBarLinearLayout;
        RelativeLayout floorRelativeLayout;
        RelativeLayout cancelNaviLayout;
        RelativeLayout slidingTransferLayout;
        RelativeLayout routeFailIntoLayout;
        ImageView cancelNaviIcon;
        TextView cancelNaviText;
        int mode = NORMAL_MODE;
        public static final int NORMAL_MODE = 0;
        public static final int MULTIPLE_MARKER_MODE = 1;
        public static final int MULTIPLE_MARKER_WITH_SINGLE_MODE = 2;
        public static final int NAVIGATION_MODE = 3;
        public static final int ARRANGE_MODE = 4;
        ImageView lockcenter;
        //        ImageView arrangePath;
        Button backNavi;
        AutoResizeTextView floorIndicator;
        private ProgressDialog progressdg;

        public PlaceholderFragment() {
        }




        void generateFloorSpinnerProcedure() {
            Activity activity = getActivity();
            if (activity == null)
                return;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSailsMapView == null)
                        return;
                    List<String> floorList = mSails.getFloorDescList();
                    if(FLOOR_GUIDE)
                        floorList.add(0,getString(R.string.floorguide));
                    final Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, floorList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(dataAdapter);
                }
            });
        }

        public void showArrangeModeMarker() {
            if (mStartEndManager.getStart() != null) {
                mSailsMapView.getMarkerManager().setLocationRegionMarker(mStartEndManager.getStart(), Marker.boundCenter(getResources().getDrawable(R.drawable.start_point)));
            }
            if (mStartEndManager.getEnd() != null) {
                mSailsMapView.getMarkerManager().setLocationRegionMarker(mStartEndManager.getEnd(), Marker.boundCenterBottom(getResources().getDrawable(R.drawable.map_destination)));
            }

        }
        SAILSMapView.OnRegionClickListener regionClickListener=new SAILSMapView.OnRegionClickListener() {
            @Override
            public void onClick(List<LocationRegion> locationRegions) {
                if (mode == MULTIPLE_MARKER_MODE || mode == MULTIPLE_MARKER_WITH_SINGLE_MODE || mode == NAVIGATION_MODE)
                    return;
                mSailsMapView.getMarkerManager().clear();
                if (mSails.isInThisBuilding())
                    mSailsMapView.getMarkerManager().setLocationRegionMarker(locationRegions.get(0), Marker.boundCenterBottom(getResources().getDrawable(R.drawable.destination)));
                else
                    mSailsMapView.getMarkerManager().setLocationRegionMarker(locationRegions.get(0), Marker.boundCenterBottom(getResources().getDrawable(R.drawable.map_destination)));
                if (mStartEndManager.isSetStartPoint) {
                    mStartEndManager.setStartPoint(locationRegions.get(0));
                } else if (mStartEndManager.isSetEndPoint) {
                    mStartEndManager.setEndPoint(locationRegions.get(0));
                } else {
                    if (mode != ARRANGE_MODE) {
//                            if (locationRegions.get(0).getName().length() != 0)
                        notificationManager.openNotification(locationRegions.get(0));
                    }
                    if (mode == ARRANGE_MODE) {
                        mStartEndManager.setStartPoint(locationRegions.get(0));
                    } else {
                        mStartEndManager.setEndPoint(locationRegions.get(0));
                    }

//                        mStartEndManager.setEndPoint(locationRegions.get(0));
                }
                if (mode == ARRANGE_MODE) {
                    showArrangeModeMarker();
                }

            }
        };
        void mapViewInitial() {
            //establish a connection of SAILS engine into SAILS MapView.
//            mSailsMapView.setSAILSEngine(mSails);
            //set location pointer icon.
//            LocationRegion.FONT_LANGUAGE = LocationRegion.NORMAL;
            Paint accuracyCircleFill = new Paint(Paint.ANTI_ALIAS_FLAG);
            accuracyCircleFill.setStyle(Paint.Style.FILL);
            accuracyCircleFill.setColor(Color.rgb(53, 179, 229));//);
            accuracyCircleFill.setAlpha(0);
            accuracyCircleFill.setStrokeWidth(0);
            accuracyCircleFill.setStrokeJoin(Paint.Join.ROUND);

            mSailsMapView.setLocationMarker(R.drawable.myloc_cir, R.drawable.myloc_arr, accuracyCircleFill, 100);
            //set location marker visible.
            //load first floor map in package.
            final Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);

            if (!mSails.getFloorNameList().isEmpty()) {
                mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(0));
                mSailsMapView.getMapViewPosition().setZoomLevel((byte)18);
            }

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                boolean first=true;
                int position=1;
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    mSailsMapView.clear();

                    if(FLOOR_GUIDE&&i==0&&position!=0) {
                        int p=position;
                        position=0;
                        spinner.setSelection(p);
                        List<LocationRegion> locationRegionList=mSails.findRegionByLabel("floor_guide");

                        if(locationRegionList!=null&&locationRegionList.size()>0&&!first)
                            showWebView(mSails.findRegionByLabel("floor_guide").get(0).url);
                        first=false;
                        return;
                    }
                    position=i;
                    if(FLOOR_GUIDE) {
                        i--;
                    }
//                    i--;
                    if(i<0)
                        return;
                    mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(i));
                    mSailsMapView.getMapViewPosition().setZoomLevel((byte) 18);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            mSailsMapView.setOnRegionClickListener(regionClickListener);
            mSailsMapView.setOnClickNothingListener(new SAILSMapView.OnClickNothingListener() {
                @Override
                public void onClick() {
                    if (mode == MULTIPLE_MARKER_MODE || mode == MULTIPLE_MARKER_WITH_SINGLE_MODE || mode == NAVIGATION_MODE || mode == ARRANGE_MODE)
                        return;
                    mSailsMapView.getMarkerManager().clear();
                    notificationManager.closeNotification();
                }
            });

            //design some action in mode change call back.
            mSailsMapView.setOnModeChangedListener(new SAILSMapView.OnModeChangedListener() {
                @Override
                public void onModeChanged(int lockmode) {
                    if (((lockmode & SAILSMapView.LOCATION_CENTER_LOCK) == SAILSMapView.LOCATION_CENTER_LOCK) &&
                            ((lockmode & SAILSMapView.FOLLOW_PHONE_HEADING) == SAILSMapView.FOLLOW_PHONE_HEADING)) {
                        lockcenter.setImageDrawable(getResources().getDrawable(R.drawable.center3));
                    } else if ((lockmode & SAILSMapView.LOCATION_CENTER_LOCK) == SAILSMapView.LOCATION_CENTER_LOCK) {
                        lockcenter.setImageDrawable(getResources().getDrawable(R.drawable.center2));
                    } else {
                        if (mSails.isInThisBuilding() && mode == NAVIGATION_MODE)
                            backNavi.setVisibility(View.VISIBLE);

                        lockcenter.setImageDrawable(getResources().getDrawable(R.drawable.center1));
                    }
                }
            });
        }

        boolean showListView = false;


        void functionClickProcedure() {
            if (!functionManger.isOpened()) {
                functionManger.openFunctionView();
            } else {
                functionManger.closeFunctionView();
            }
        }

        private List<FunctionManager.FunctionElement> generateFunctionElementList() {
            List<FunctionManager.FunctionElement> functionElementList = new ArrayList<FunctionManager.FunctionElement>();
            FunctionManager.FunctionElement functionElement;

            Activity activity = getActivity();
            if (activity == null)
                return functionElementList;
            Resources res = activity.getResources();
            functionElement = new FunctionManager.FunctionElement(FunctionManager.FunctionType.EXIT,
                    res.getDrawable(R.drawable.exit), res.getString(R.string.exit));
            functionElementList.add(functionElement);
            functionElement = new FunctionManager.FunctionElement(FunctionManager.FunctionType.FOOD,
                    res.getDrawable(R.drawable.food), res.getString(R.string.food));
            functionElementList.add(functionElement);
            functionElement = new FunctionManager.FunctionElement(FunctionManager.FunctionType.REFRESHMENT,
                    res.getDrawable(R.drawable.refreshment), res.getString(R.string.refreshment));
            functionElementList.add(functionElement);
            functionElement = new FunctionManager.FunctionElement(FunctionManager.FunctionType.TOILET,
                    res.getDrawable(R.drawable.toilet), res.getString(R.string.toilet));
            functionElementList.add(functionElement);
            functionElement = new FunctionManager.FunctionElement(FunctionManager.FunctionType.SHOPPING,
                    res.getDrawable(R.drawable.shopping), res.getString(R.string.shopping));
            functionElementList.add(functionElement);
            functionElement = new FunctionManager.FunctionElement(FunctionManager.FunctionType.ATM,
                    res.getDrawable(R.drawable.atm), res.getString(R.string.atm));
            functionElementList.add(functionElement);
            return functionElementList;
        }

        void refreshExpandableMenuByFloor(String type, String subtype) {

            //1st stage groups
            List<Map<String, String>> groups = new ArrayList<Map<String, String>>();
            //2nd stage groups
            List<List<Map<String, LocationRegion>>> childs = new ArrayList<List<Map<String, LocationRegion>>>();
            for (String mS : mSails.getFloorNameList()) {
                Map<String, String> group_item = new HashMap<String, String>();
                group_item.put("group", mSails.getFloorDescList().get(mSails.getFloorNameList().indexOf(mS)));


                List<Map<String, LocationRegion>> child_items = new ArrayList<Map<String, LocationRegion>>();
                for (LocationRegion mlr : mSails.getFilteredLocationRegionList(mS, type, subtype)) {
                    if (mlr.getName() == null || mlr.getName().length() == 0)
                        continue;

                    Map<String, LocationRegion> childData = new HashMap<String, LocationRegion>();
                    childData.put("child", mlr);
                    child_items.add(childData);
                }
                if (child_items.size() != 0) {
                    groups.add(group_item);
                    childs.add(child_items);
                }
            }
            eAdapter = new ExpandableAdapter(getActivity(), groups, childs);
            eLV.setAdapter(eAdapter);
        }

        void setDestination(LocationRegion lr) {

            if (!lr.getFloorName().equals(mSailsMapView.getCurrentBrowseFloorName())) {
                mSailsMapView.loadFloorMap(lr.getFloorName());
            }
            mSailsMapView.setAnimationToZoom((byte) 19);
            mSailsMapView.getMarkerManager().clear();
            if (mSails.isInThisBuilding())
                mSailsMapView.getMarkerManager().setLocationRegionMarker(lr, Marker.boundCenterBottom(getActivity().getResources().getDrawable(R.drawable.destination)));
            else
                mSailsMapView.getMarkerManager().setLocationRegionMarker(lr, Marker.boundCenterBottom(getActivity().getResources().getDrawable(R.drawable.map_destination)));
            mSailsMapView.setAnimationMoveMapTo(new GeoPoint(lr.getCenterLatitude(), lr.getCenterLongitude()));
            mSailsMapView.setMode(mSailsMapView.getMode() & ~SAILSMapView.LOCATION_CENTER_LOCK);
            if (mStartEndManager.isSetStartPoint)
                mStartEndManager.setStartPoint(lr);
            else if (mStartEndManager.isSetEndPoint) {
                mStartEndManager.setEndPoint(lr);
            } else {
                mTransferLayout.closeInfo();
                notificationManager.openNotification(lr);
                mStartEndManager.setEndPoint(lr);
            }
            if (mode == ARRANGE_MODE) {
                showArrangeModeMarker();
            }
        }

        void openListView(String type, String subtype) {
            FrameLayout fl = (FrameLayout) rootView.findViewById(R.id.frameLayoutList);
            View expandView = View.inflate(getActivity(), R.layout.expantablelist, null);
            fl.removeAllViews();
            fl.addView(expandView);
            ((TextView) rootView.findViewById(R.id.textViewListTitle)).setText(mSails.getBuildingName());
            eLV = (ExpandableListView) fl.findViewById(R.id.expandableListView);
            refreshExpandableMenuByFloor(type, subtype);
            eLV.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long l) {
                    final LocationRegion lr = eAdapter.childs.get(i).get(i2).get("child");
                    closeListView();
                    setDestination(lr);
                    mode = NORMAL_MODE;
                    return false;
                }
            });
            rootView.findViewById(R.id.linearLayoutListTitle).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    closeListView();
                }
            });
            fl.setVisibility(View.VISIBLE);
            Animation startAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.showlist);
            fl.startAnimation(startAnim);
            showListView = true;
        }

        private void closeListView() {
            final FrameLayout fl = (FrameLayout) rootView.findViewById(R.id.frameLayoutList);
            Animation endAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.showlist);
            endAnim.setInterpolator(new SAILSAnimation.ReverseInterpolator());
            endAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    fl.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            fl.startAnimation(endAnim);
            showListView = false;
        }

        public boolean setMarkers(String type, String subtype) {
            mode = PlaceholderFragment.MULTIPLE_MARKER_MODE;
            notificationManager.closeNotification();
            mSailsMapView.getMarkerManager().clear();
            int count = 0;
            final List<GeoPoint> geoPointList = new ArrayList<GeoPoint>();
            highlightLocationRegionList = new ArrayList<LocationRegion>();

            for (String floor : mSails.getFloorNameList()) {
                for (LocationRegion lr : mSails.getFilteredLocationRegionList(floor, type, subtype)) {
                    if (mSailsMapView.getCurrentBrowseFloorName().equals(floor)) {
                        geoPointList.add(new GeoPoint(lr.getCenterLatitude(), lr.getCenterLongitude()));
                        count++;
                    }
                    highlightLocationRegionList.add(lr);
                    mSailsMapView.getMarkerManager().setLocationRegionMarker(lr, Marker.boundCenterBottom(getResources().getDrawable(R.drawable.goal_pink)));

                }
            }
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mSailsMapView.autoSetMapZoomAndView(geoPointList);

                }
            };
            new Handler().postDelayed(runnable, 500);
            notificationManager.openNotification(highlightLocationRegionList);
            if (count == 0) {
                return false;
            }

            return true;

        }

        private void onClickFunction(FunctionManager.FunctionType type) {
            if (type == FunctionManager.FunctionType.EXIT) {
                sel_type = "gateway";
                sel_subtype = null;
                openListView(sel_type, sel_subtype);
                return;
            }
            if (type == FunctionManager.FunctionType.FOOD) {
                sel_type = "store";
                sel_subtype = "food";
                if (!setMarkers(sel_type, sel_subtype))
                    Toast.makeText(getActivity(), String.format(getString(R.string.no_location_region), getString(R.string.food)), Toast.LENGTH_LONG).show();
                return;
            }
            if (type == FunctionManager.FunctionType.REFRESHMENT) {
                sel_type = "store";
                sel_subtype = "drink";
                if (!setMarkers(sel_type, sel_subtype))
                    Toast.makeText(getActivity(), String.format(getString(R.string.no_location_region), getString(R.string.refreshment)), Toast.LENGTH_LONG).show();

                return;
            }
            if (type == FunctionManager.FunctionType.TOILET) {
                sel_type = "facility";
                sel_subtype = "toilet";
                if (!setMarkers(sel_type, sel_subtype))
                    Toast.makeText(getActivity(), String.format(getString(R.string.no_location_region), getString(R.string.toilet)), Toast.LENGTH_LONG).show();

                return;
            }
            if (type == FunctionManager.FunctionType.SHOPPING) {
                sel_type = "store";
                sel_subtype = "shopping";
                if (!setMarkers(sel_type, sel_subtype))
                    Toast.makeText(getActivity(), String.format(getString(R.string.no_location_region), getString(R.string.shopping)), Toast.LENGTH_LONG).show();

                return;
            }
            if (type == FunctionManager.FunctionType.ATM) {
                sel_type = "facility";
                sel_subtype = "atm";
                if (!setMarkers(sel_type, sel_subtype))
                    Toast.makeText(getActivity(), String.format(getString(R.string.no_location_region), getString(R.string.atm)), Toast.LENGTH_LONG).show();

                return;
            }

        }

        void openBuilding(String token, String buildingId) {

            mSails.loadCloudBuilding(token, buildingId, new SAILS.OnFinishCallback() {
                @Override
                public void onSuccess(String response) {
                    if (getActivity() == null)
                        return;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mSearchManager.isReady = false;
                            mSearchManager.createLocationRegionDB();
                            if (getActivity() == null)
                                return;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mSearchManager.setFunctionElementList(generateFunctionElementList(), 3);
                                    mSearchManager.clear();
                                    mSearchManager.isReady = true;


                                }
                            });
                            mSearchManager.setOnFunctionClickListener(new FunctionManager.OnFunctionClickListener() {
                                @Override
                                public void OnClick(FunctionManager.FunctionType type) {
                                    mSearchManager.closeView();
                                    onClickFunction(type);
                                }
                            });
                            mSearchManager.setOnLocationRegionClick(new SearchManager.OnLocationRegionClick() {
                                @Override
                                public void onClick(LocationRegion lr) {
                                    setDestination(lr);

                                }
                            });
                            mSails.startLocatingEngine();

                        }
                    }).start();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapViewInitial();
                            generateFloorSpinnerProcedure();
                            progressdg.dismiss();
                        }
                    });

                }

                @Override
                public void onFailed(String response) {
                    progressdg.dismiss();
                }
            });
            mSails.setOnLocationChangeEventListener(new SAILS.OnLocationChangeEventListener() {
                boolean first=false;
                @Override
                public void OnLocationChange() {
                    if (mSailsMapView.isCenterLock() && !mSailsMapView.isInLocationFloor() && mSails.isLocationFix()) {
                        //set the map that currently location engine recognize.
                        mSailsMapView.loadCurrentLocationFloorMap();
                        mSailsMapView.setAnimationToZoom((byte) 19);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mTransferLayout.setLockCenterTrigger();
                                mTransferLayout.setPagerPositionByLocatingFloorName(mSails.getFloor());
                            }
                        }, 2000);
                    }

                    if (!showlocatingmsg && mSails.isInThisBuilding() && mSails.isLocationFix()) {
                        identifyIndoor = ProgressDialog.show(getActivity(), "", getString(R.string.positioning), true, true);
                        showlocatingmsg = true;
                    }

                    if (mSails.isLocationFix()) {

                        if(!first) {
                            mSailsMapView.setMode(mSailsMapView.getMode() | SAILSMapView.LOCATION_CENTER_LOCK);
                            first=true;
                        }

                        identifyIndoor.dismiss();
                    }


                }
            });
            mSails.setOnBTLEPushEventListener(new SAILS.OnBTLEPushEventListener() {
//                LocationRegion saveLR=null;
                @Override
                public void OnPush(Beacon mB) {
                    if(mB.locationRegions==null||mB.locationRegions.size()==0)
                        return;
                    final List<LocationRegion> lr=mB.locationRegions;
//                    if(saveLR==lr.get(0))
//                        return;
//                    saveLR=lr.get(0);
                    if(lr!=null&&lr.size()>0){//&&lr.get(0).url!=null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                regionClickListener.onClick(lr);

//                                notificationManager.openNotification(lr.get(0));
                            }
                        });

                    }

                }

                @Override
                public void OnNothingPush() {

                }
            });
            mSails.setOnFloorChangeListener(new SAILS.OnFloorChangeListener() {
                @Override
                public void onFloorChanged(String floor) {
                    if (mRoutingHandler.isRoutingEnable())
                        mTransferLayout.updateInfo(mRoutingHandler.getResultGNlist(), true);
                }
            });
        }

        // Eddie Hua
        private void putBeaconPinMarkerOnMap(final Beacon beacon) {
            if (Version.DEBUG)
                Log.i(TAG, "putBeaconPinMarkerOnMap");
                /*
            PinMarkerManager pinMarkerMgr = mSailsMapView.getPinMarkerManager();
            pinMarkerMgr.addMarker(beacon.lon, beacon.lat, mSailsMapView.getCurrentBrowseFloorName(), getResources().getDrawable(R.drawable.beacon_pin_marker), mSails);
            pinMarkerMgr.setOnPinMarkerGenerateCallback(Marker.boundCenterBottom(getResources().getDrawable(R.drawable.beacon_pin_marker)), new PinMarkerManager.OnPinMarkerGenerateCallback() {
                @Override
                public void OnGenerate(MarkerManager.LocationRegionMarker locationRegionMarker) {
                    //locationRegionMarker.
                    Toast.makeText(getActivity(), "One PinMarker Generated.", Toast.LENGTH_SHORT).show();
                }
            });
            pinMarkerMgr.setOnPinMarkerClickCallback(new PinMarkerManager.OnPinMarkerClickCallback() {
                @Override
                public void OnClick(MarkerManager.LocationRegionMarker locationRegionMarker) {
                    Toast.makeText(getActivity(), beacon.push_name, Toast.LENGTH_SHORT).show();
                }
            });*/
        }
        public void showWebView(String url) {
            AlertDialog alert = new AlertDialog.Builder(getActivity()).create();
//                    alert.setTitle(R.string.mail_verify_title);

//                    alert.setNegativeButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                            dialog.dismiss();
//                        }
//                    });
            final WebView wv = new WebView(getActivity());
            if(!BLE) {
                wv.setWebChromeClient(new WebChromeClient());
                wv.clearCache(true);

            }
            File lFile=new File(Environment.getExternalStorageDirectory() + url+".html");
            if(LANGUAGE.equals("en"))
                lFile = new File(Environment.getExternalStorageDirectory() + url+"_en.html");
            wv. getSettings().setJavaScriptEnabled (true);
            wv. getSettings().setJavaScriptCanOpenWindowsAutomatically (false);
//                    wv. getSettings().setPl (true);
            wv. getSettings().setSupportMultipleWindows (false);
            wv. getSettings().setSupportZoom(false);
            wv. setVerticalScrollBarEnabled (false);
//                    wv. setHorizontalScrollBarEnabled (false);
            wv.loadUrl("file:///" + lFile.getAbsolutePath());
            alert.setView(wv);
            alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    try {
                        Class.forName("android.webkit.WebView").getMethod("onPause", (Class[]) null).invoke(wv, (Object[]) null);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            });

            alert.show();




        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final MainActivity activity = (MainActivity) getActivity();
            activity.placeholderFragment = this;


//            progressdg = ProgressDialog.show(activity,"",getString(R.string.processing),true,false);
//            progressdg.show();
            rootView = inflater.inflate(R.layout.fragment_main, container, false);

            mSails = new SAILS(activity);
            mSails.setEnvironmentIsHighBeaconDensity(true);//v1.51

            // EddieHua : create location change call back.
            SharedPreferences sp=getActivity().getSharedPreferences("ecs",MODE_PRIVATE);
//            ((TextView)getActivity().findViewById(R.id.tvWelcome)).setText(sp.getString("welcome_msg","Welcome Dear Guest"));
            if(BLE)
                mSails.setMode(SAILS.BLE_GFP_IMU|SAILS.BLE_ADVERTISING);
//                mSails.setMode(SAILS.BLE_GFP_ONLY|SAILS.BLE_ADVERTISING);
            else
                mSails.setMode(SAILS.WIFI_GFP_IMU);

//            mSails.setOnBTLEPushEventListener(new SAILS.OnBTLEPushEventListener() {
//                @Override
//                public void OnPush(final Beacon beacon) {
//                    if (Version.DEBUG)
//                        Log.i(TAG, "OnPush Beacon : " + beacon.push_name);
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            //BeaconMsgDialog.showMsg(getActivity(), "123", "456", BeaconMsgDialog.CLOSE_ON_CLICK_OUTSIDE);
//                            BeaconMsgDialog.showBeacon(activity, beacon, BeaconMsgDialog.CLOSE_ON_CLICK_OUTSIDE);
//                            // 地圖上標註
//                            //putBeaconPinMarkerOnMap(beacon);
//                        }
//                    });
//                }
//
//                @Override
//                public void OnNothingPush() {
//                    if (Version.DEBUG) {
//                        Log.i(TAG, "OnNothingPush");
//                    }
//                    // 關閉 BeaconMsgDialog
//                    BeaconMsgDialog.hideMsg();
//                }
//
//            });
            mSails.setSmoothChangeFloor(true);
            mSails.setReverseFloorList(true);
            mSails.getCurrentInRegions();
            mSailsMapView = new SAILSMapView(activity);
            mSailsMapView.setOutsourcingPath(new File(Environment.getExternalStorageDirectory(), "ecsgroup").toString());
            mSailsMapView.setSAILSEngine(mSails);

            mTransferLayout = (SlidingTransferManager) rootView.findViewById(R.id.slidingTransferInfo);
            slidingTransferLayout = (RelativeLayout) rootView.findViewById(R.id.slidingTransferLayout);
            left = (ImageView) rootView.findViewById(R.id.imageViewLeft);
            right = (ImageView) rootView.findViewById(R.id.imageViewRight);
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.INVISIBLE);
            mRoutingHandler = new PathRoutingManager(getActivity(), mSails, mSailsMapView, mTransferLayout);
            mTransferLayout.setRoutingManager(mRoutingHandler);
            ((FrameLayout) rootView.findViewById(R.id.frameLayout)).addView(mSailsMapView);
            lockcenter = (ImageView) rootView.findViewById(R.id.lockcenter);
//            lockcenter.setVisibility((!mSails.checkMode(SAILS.BLE_ADVERTISING)) ? View.VISIBLE : View.INVISIBLE);
            backNavi = (Button) rootView.findViewById(R.id.backNavibutton);
            backNavi.setVisibility(View.INVISIBLE);
//            arrangePath = (ImageView) rootView.findViewById(R.id.arrangepath);
            searchBarLinearLayout = (LinearLayout) rootView.findViewById(R.id.searchBarLinearLayout);
            floorRelativeLayout = (RelativeLayout) rootView.findViewById(R.id.floorRelativeLayout);
            cancelNaviLayout = (RelativeLayout) rootView.findViewById(R.id.cancelNaviLayout);
            cancelNaviIcon = (ImageView) rootView.findViewById(R.id.cancelNavi);
            cancelNaviText = (TextView) rootView.findViewById(R.id.cancelNaviText);
            cancelNaviLayout.setVisibility(View.INVISIBLE);
            floorIndicator = (AutoResizeTextView) rootView.findViewById(R.id.floorIndicator);
            floorIndicator.setVisibility(View.INVISIBLE);
            routeFailIntoLayout = (RelativeLayout) rootView.findViewById(R.id.routeFailInfoLayout);
            routeFailIntoLayout.setVisibility(View.INVISIBLE);


            View functionView = View.inflate(activity, R.layout.function_layout, null);//, container, false);


            frameLayoutFunction = ((FrameLayout) rootView.findViewById(R.id.frameLayoutFunction));
            functionManger = new FunctionManager(getActivity(), frameLayoutFunction, generateFunctionElementList(), 3);
            Animation endAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.showfunction);
            endAnim.setInterpolator(new SAILSAnimation.ReverseInterpolator());
            functionManger.setEndAnimation(endAnim);
            Animation startAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.showfunction);
            functionManger.setStartAnimation(startAnim);

            functionManger.setOnFunctionClickListener(new FunctionManager.OnFunctionClickListener() {
                @Override
                public void OnClick(FunctionManager.FunctionType type) {
                    functionManger.closeFunctionView();
                    onClickFunction(type);
                }
            });

            cancelNaviLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelNaviLayout.setVisibility(View.INVISIBLE);
                    searchBarLinearLayout.setVisibility(View.VISIBLE);
                    floorRelativeLayout.setVisibility(View.VISIBLE);
                    floorIndicator.setVisibility(View.INVISIBLE);
                    mRoutingHandler.disableHandler();
                }
            });


//            frameLayoutFunction.addView(functionView);
            RelativeLayout frameLayoutNotification = (RelativeLayout) rootView.findViewById(R.id.frameLayoutNotification);
            notificationManager = new NotificationManager(frameLayoutNotification);
            notificationManager.setStartAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.shownotification));
            notificationManager.setEndAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.hidenotification));
            notificationManager.setMainTextView(R.id.textViewMainNotify);
            notificationManager.setSubTextView(R.id.textViewSubNotify);
            notificationManager.setInfoImageView(R.id.ivInfo);
            notificationManager.setOnInfoClickListener(new NotificationManager.OnInfoClickListener() {
                @Override
                public void onClick(LocationRegion lr) {

                    ((MainActivity)getActivity()).showPOIView(lr.url);
                }
            });
            //notificationManager.enableRouteButton(false);
            mSearchManager = new SearchManager(activity, mSails, (FrameLayout) rootView.findViewById(R.id.frameLayoutSearch));
            mStartEndManager = (StartEndManager) rootView.findViewById(R.id.start_end_frame);


            mStartEndManager.setControlRes(mRoutingHandler, mSearchManager, notificationManager, mTransferLayout);
            mSailsMapView.post(new Runnable() {

                @Override
                public void run() {
                    mSailsMapView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            functionManger.closeFunctionView();
                            return false;
                        }
                    });
                    mSailsMapView.setOnMapClickListener(new SAILSMapView.OnMapClickListener() {
                        int test = 0;

                        @Override
                        public void onClick(int x, int y) {
                            if (Version.DEBUG) {
                                switch (test) {
                                    case 0:
                                        BeaconMsgDialog.showMsg(getActivity(), "(TEST)促銷活動", "---", 1000);
                                        break;
                                    case 1:
                                        BeaconMsgDialog.showMsg(getActivity(), "(TEST)優惠訊息", "http://www.google.com", 5000);
                                        break;
                                    default:
                                        BeaconMsgDialog.showMsg(getActivity(), "(TEST)中友週年慶YO！\n化妝品滿2000送200\n購物券全館滿3000送300", "http://www.chungyo.com.tw/edm.php", BeaconMsgDialog.CLOSE_ON_CLICK_OUTSIDE);
                                        test = -1;
                                        break;
                                }
                                test++;
                            }
                            if (mode == MULTIPLE_MARKER_MODE || mode == MULTIPLE_MARKER_WITH_SINGLE_MODE) {
                                List<LocationRegion> lrlist = mSailsMapView.getMarkerManager().getLocationRegionByMarkerXY(x, y);
                                if (lrlist.size() == 0) {
                                    if (mode == MULTIPLE_MARKER_MODE) {
                                        mSailsMapView.getMarkerManager().clear();
                                        mSailsMapView.invalidate();
                                        notificationManager.closeNotification();
                                        mode = NORMAL_MODE;
                                    } else {
                                        if (highlightLocationRegion != null) {
                                            mSailsMapView.getMarkerManager().setLocationRegionMarker(highlightLocationRegion,
                                                    Marker.boundCenterBottom(getResources().getDrawable(R.drawable.goal_pink)));
                                            highlightLocationRegion = null;
                                        }
                                        mode = MULTIPLE_MARKER_MODE;
                                        notificationManager.openNotification(highlightLocationRegionList);
                                        mSailsMapView.invalidate();
                                    }
                                } else {
                                    if (mode == ARRANGE_MODE) {
                                        if (mStartEndManager.isSetStartPoint)
                                            mStartEndManager.setStartPoint(highlightLocationRegion);
                                        else if (mStartEndManager.isSetEndPoint)
                                            mStartEndManager.setEndPoint(highlightLocationRegion);
                                        highlightLocationRegion = null;
                                        showArrangeModeMarker();
                                        return;
                                    }
                                    if (highlightLocationRegion != null) {
                                        mSailsMapView.getMarkerManager().setLocationRegionMarker(highlightLocationRegion,
                                                Marker.boundCenterBottom(getResources().getDrawable(R.drawable.goal_pink)));
                                        highlightLocationRegion = null;
                                    }
                                    highlightLocationRegion = lrlist.get(0);
                                    if (mSails.isInThisBuilding())
                                        mSailsMapView.getMarkerManager().setLocationRegionMarker(highlightLocationRegion,
                                                Marker.boundCenterBottom(getResources().getDrawable(R.drawable.destination)));
                                    else
                                        mSailsMapView.getMarkerManager().setLocationRegionMarker(highlightLocationRegion,
                                                Marker.boundCenterBottom(getResources().getDrawable(R.drawable.map_destination)));

                                    if (mStartEndManager.isSetStartPoint)
                                        mStartEndManager.setStartPoint(highlightLocationRegion);
                                    else if (mStartEndManager.isSetEndPoint) {
                                        mStartEndManager.setEndPoint(highlightLocationRegion);
                                    } else {
                                        notificationManager.openNotification(highlightLocationRegion);
                                        mStartEndManager.setEndPoint(highlightLocationRegion);
                                    }
                                    mode = MULTIPLE_MARKER_WITH_SINGLE_MODE;
                                }
                            }
                        }
                    });

//                    mSails.loadCloudBuilding("3c1d084e86b5442592c945989968a033","52c385271beed7346100017a",new SAILS.OnFinishCallback() {
//                    mSails.loadCloudBuilding("3c1d084e86b5442592c945989968a033", "528fea2cb4eb33531e000001", new SAILS.OnFinishCallback() {
//                        @Override
//                        public void onSuccess(String response) {
//                            if (getActivity() == null)
//                                return;
                    // Taipei Main Station
                    //openBuilding("3c1d084e86b5442592c945989968a033", "528fea2cb4eb33531e000001");
                    // Eddie Hua : Taichung 綠園道
                }
            });

            mSailsMapView.setOnFloorChangedListener(new SAILSMapView.OnFloorChangedListener() {
                @Override
                public void onFloorChangedBefore(String floorName) {
                }

                @Override
                public void onFloorChangedAfter(final String floorName) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);
                            if(FLOOR_GUIDE)
                                spinner.setSelection(mSails.getFloorNameList().indexOf(floorName)+1);
                            else
                                spinner.setSelection(mSails.getFloorNameList().indexOf(floorName));

                            floorIndicator.setText(mSails.getFloorDescription(floorName));

                            if (mode == MULTIPLE_MARKER_WITH_SINGLE_MODE) {
                                if (highlightLocationRegion != null) {
                                    mSailsMapView.getMarkerManager().setLocationRegionMarker(highlightLocationRegion,
                                            Marker.boundCenterBottom(getResources().getDrawable(R.drawable.goal_pink)));
                                    highlightLocationRegion = null;
                                }
                                mode = MULTIPLE_MARKER_MODE;
                            }
                            mSailsMapView.invalidate();
                        }
                    });
                }
            });


            RelativeLayout floorRelativeLayout = (RelativeLayout) rootView.findViewById(R.id.floorRelativeLayout);
            final Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);
            floorRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    spinner.performClick();
                }
            });

            backNavi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSailsMapView.setMode(mSailsMapView.getMode() | SAILSMapView.LOCATION_CENTER_LOCK);
                    mSailsMapView.setMode(mSailsMapView.getMode() | SAILSMapView.FOLLOW_PHONE_HEADING);
                    backNavi.setVisibility(View.INVISIBLE);
                }
            });

            lockcenter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //set map mode.
                    //FOLLOW_PHONE_HEADING: the map follows the phone's heading.
                    //LOCATION_CENTER_LOCK: the map locks the current location in the center of map.
                    //ALWAYS_LOCK_MAP: the map will keep the mode even user moves the map.
                    if (!mSails.isInThisBuilding()) {
                        Toast.makeText(getActivity(), R.string.not_in_this_building, Toast.LENGTH_SHORT).show();
                        mSailsMapView.setMode(NORMAL_MODE);
                        return;
                    }
                    if (mSailsMapView.isCenterLock()) {
                        if ((mSailsMapView.getMode() & SAILSMapView.FOLLOW_PHONE_HEADING) == SAILSMapView.FOLLOW_PHONE_HEADING)
                            //if map control mode is follow phone heading, then set mode to location center lock when button click.
                            mSailsMapView.setMode(mSailsMapView.getMode() & ~SAILSMapView.FOLLOW_PHONE_HEADING);
                        else {
                            //if map control mode is location center lock, then set mode to follow phone heading when button click.
                            mSailsMapView.setMode(mSailsMapView.getMode() | SAILSMapView.FOLLOW_PHONE_HEADING);
                            backNavi.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        //if map control mode is none, then set mode to loction center lock when button click.
                        mSailsMapView.setMode(mSailsMapView.getMode() | SAILSMapView.LOCATION_CENTER_LOCK);
                    }
                }
            });

//            arrangePath.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mStartEndManager.show();
//                    notificationManager.closeNotification();
//                }
//            });

            return rootView;
        }
    }

    // ------------------- Gps Location Manager
    @Override
    public void onLocationChanged(android.location.Location location) {
        // 緯度 Latitude,  經度 Longitude
        Log.i(TAG, location.getProvider() + " > 緯度 lat:" + location.getLatitude() + ", 經度 long:" + location.getLongitude());
        /*if (mSailsMapView != null) {
            PinMarkerManager pinMarkerMgr = mSailsMapView.getPinMarkerManager();
            pinMarkerMgr.addMarker(location.getLongitude(), location.getLatitude(), mSailsMapView.getCurrentBrowseFloorName(), getResources().getDrawable(R.drawable.destination), mSails);
        }*/
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    void runNaviModeProcedure() {
        trueNaviMode=true;
        int height=findViewById(R.id.frameLayout).getHeight()/2;
        ViewGroup.LayoutParams params=findViewById(R.id.di).getLayoutParams();
        params.height=height;
        findViewById(R.id.di).setLayoutParams(params);
        findViewById(R.id.rlNavigator).setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeInUp).playOn(findViewById(R.id.rlNavigator));
        YoYo.with(Techniques.FadeOutUp).playOn(findViewById(R.id.searchBarLinearLayout));
        YoYo.with(Techniques.FadeOutUp).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.searchBarLinearLayout).setVisibility(View.GONE);
                findViewById(R.id.floorRelativeLayout).setVisibility(View.GONE);

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).playOn(findViewById(R.id.floorRelativeLayout));
        YoYo.with(Techniques.FadeOut).playOn(findViewById(R.id.zoomin));
        YoYo.with(Techniques.FadeOut).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.zoomin).setVisibility(View.GONE);
                findViewById(R.id.zoomout).setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).playOn(findViewById(R.id.zoomout));
//        ((DirectionIndicator)findViewById(R.id.di)).setPhase(DirectionIndicator.PHASE1);
//        ((DirectionIndicator)findViewById(R.id.di)).startAnimate();
        ((DirectionIndicator)findViewById(R.id.di)).startDemoAnimate();//.startAnimate();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((DirectionIndicator)findViewById(R.id.di)).setDirection(0);

            }
        },2000);
        ((DirectionIndicator)findViewById(R.id.di)).setOnCloseEventListener(new DirectionIndicator.OnCloseEventListener() {
            @Override
            public void OnClose() {
                closeNaviModeProcedure();
            }
        });
    }
    void closeNaviModeProcedure() {
        trueNaviMode=false;
        ((DirectionIndicator)findViewById(R.id.di)).setArrowVisible(false);
        YoYo.with(Techniques.FadeOutDown).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.rlNavigator).setVisibility(View.GONE);

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).playOn(findViewById(R.id.rlNavigator));
        findViewById(R.id.searchBarLinearLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.floorRelativeLayout).setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeInDown).playOn(findViewById(R.id.searchBarLinearLayout));
        YoYo.with(Techniques.FadeInDown).playOn(findViewById(R.id.floorRelativeLayout));
        YoYo.with(Techniques.FadeIn).playOn(findViewById(R.id.zoomin));
        YoYo.with(Techniques.FadeIn).playOn(findViewById(R.id.zoomout));
        findViewById(R.id.zoomin).setVisibility(View.VISIBLE);
        findViewById(R.id.zoomout).setVisibility(View.VISIBLE);
        ((DirectionIndicator)findViewById(R.id.di)).stopAnimate();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        },2000);
    }
    void onTestButtonClick(View v) {
        if(trueNaviMode) {
            closeNaviModeProcedure();
        } else {
            runNaviModeProcedure();
        }


    }
    void showPOIView(String id) {

        Intent i=new Intent(this, POIActivity.class);
        i.putExtra("POI_Id",id);
        startActivity(i);

    }
}
