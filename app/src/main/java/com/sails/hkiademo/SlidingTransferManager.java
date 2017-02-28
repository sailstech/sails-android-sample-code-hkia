package com.sails.hkiademo;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sails.engine.LocationRegion;
import com.sails.engine.SAILS;
import com.sails.engine.SAILSMapView;
import com.sails.engine.core.model.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rocky on 2014/1/11.
 */
public class SlidingTransferManager extends RelativeLayout implements ViewPager.OnPageChangeListener {

    static final String TAG=SlidingTransferManager.class.getSimpleName();

    static int UP = 1;
    static int DOWN = -1;
    static int TARGET = 0;
    ViewPager viewPager = null;
    MyPagerAdapter adapter = null;
    List<TransferItem> infoList = null;
    private PathRoutingManager pm = null;
    SAILS sails;
    SAILSMapView mapview;
    AnimationController amc;

    public SlidingTransferManager(Context context) {
        super(context);
    }

    public SlidingTransferManager(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void importSAILS(SAILS sails, SAILSMapView mapview) {
        this.sails = sails;
        this.mapview = mapview;
        amc = new AnimationController();
    }

    public void updateInfo(List<SAILS.GeoNode> gnlist, boolean forceUpdate) {

        List<TransferItem> newInfoList = new ArrayList<TransferItem>();

        if (gnlist == null)
            return;

        int i = -1;
        LocationRegion elevator_temp_region = null;
        TransferItem elevator_temp_item = null;
        for (SAILS.GeoNode mG : gnlist) {
            i++;
            if (i == gnlist.size() - 1)
                break;

            if (mG.floornumber == gnlist.get(i + 1).floornumber) {
                elevator_temp_region = null;
                elevator_temp_item = null;
                continue;
            }

            if (mG.BelongsRegion == null || gnlist.get(i + 1).BelongsRegion == null) {
                elevator_temp_region = null;
                elevator_temp_item = null;
                continue;
            }

            if (mG.BelongsRegion.type == null || gnlist.get(i + 1).BelongsRegion.type == null) {
                elevator_temp_region = null;
                elevator_temp_item = null;
                continue;
            }

            if (!mG.BelongsRegion.type.equals("transfer") || !gnlist.get(i + 1).BelongsRegion.type.equals("transfer")) {
                elevator_temp_region = null;
                elevator_temp_item = null;
                continue;
            }

            if (mG.BelongsRegion.subtype == null || gnlist.get(i + 1).BelongsRegion.subtype == null) {
                elevator_temp_region = null;
                elevator_temp_item = null;
                continue;
            }

            if (!mG.BelongsRegion.subtype.equals("escalator") && !mG.BelongsRegion.subtype.equals("stair") && !mG.BelongsRegion.subtype.equals("elevator")) {
                elevator_temp_region = null;
                elevator_temp_item = null;
                continue;
            }

            if (!gnlist.get(i + 1).BelongsRegion.subtype.equals("escalator") && !gnlist.get(i + 1).BelongsRegion.subtype.equals("stair") && !gnlist.get(i + 1).BelongsRegion.subtype.equals("elevator")) {
                elevator_temp_region = null;
                elevator_temp_item = null;
                continue;
            }

            int iconRes = 0;
            int direction = 0;
            String toFloorname = gnlist.get(i + 1).BelongsRegion.getFloorName();

            if (gnlist.get(i + 1).floornumber > mG.floornumber)
                direction = UP;
            else if (gnlist.get(i + 1).floornumber < mG.floornumber)
                direction = DOWN;

            if (mG.BelongsRegion.subtype.equals("escalator")) {
                if (direction == UP)
                    iconRes = R.drawable.escalator_up_inv;
                else
                    iconRes = R.drawable.escalator_down_inv;
            } else if (mG.BelongsRegion.subtype.equals("stair")) {
                if (direction == UP)
                    iconRes = R.drawable.stairs_up_inv;
                else
                    iconRes = R.drawable.stairs_down_inv;
            } else if (mG.BelongsRegion.subtype.equals("elevator")) {
                if (direction == UP)
                    iconRes = R.drawable.elevator_up_inv;
                else
                    iconRes = R.drawable.elevator_down_inv;
            }

            TransferItem item = new TransferItem(mG, iconRes, direction, toFloorname);

            //if the transfer is elevator and the same id, and only store start and end
            if (elevator_temp_region != null && elevator_temp_region.subtype.equals(mG.BelongsRegion.subtype)) {
                boolean find = false;
                //find if next elevator transfer has connect provious elevator transfer.
                for (Integer mI : elevator_temp_region.goToList) {
                    if (mI == mG.BelongsRegion.self) {
                        TransferItem newItem = elevator_temp_item;
                        newItem.toFloorname = item.toFloorname;
                        newInfoList.remove(elevator_temp_item);
                        newInfoList.add(newItem);
                        elevator_temp_item=newItem;
                        find = true;
                        break;
                    }
                }
                if (!find)
                    newInfoList.add(item);
            } else
                newInfoList.add(item);

            if (mG.BelongsRegion.subtype.equals("elevator")) {
                if (elevator_temp_item == null && elevator_temp_region == null) {
                    elevator_temp_item = item;
                }
                elevator_temp_region = mG.BelongsRegion;
            } else {
                elevator_temp_region = null;
                elevator_temp_item = null;
            }
        }

        //add destination
        SAILS.GeoNode gn = gnlist.get(gnlist.size() - 1);
        TransferItem item;
        if (sails.isInThisBuilding())
            item = new TransferItem(gn, R.drawable.destination, TARGET, gn.BelongsRegion==null?"":gn.BelongsRegion.getName());
        else
            item = new TransferItem(gn, R.drawable.map_destination, TARGET, gn.BelongsRegion==null?"":gn.BelongsRegion.getName());

        newInfoList.add(item);


        if (infoList == null) {
            if (newInfoList.isEmpty())
                return;
            else
                infoList = newInfoList;
        } else {
            //force update to new Infolist.
            if (forceUpdate)
                infoList = newInfoList;
            else {
                if(newInfoList.size()==0||infoList.size()==0)
                    return;
                //check if the transfer info need to be update or not, cuz the route path maybe not changed.
                if (newInfoList.get(0).fromBelongsRegion == infoList.get(0).fromBelongsRegion &&
                        newInfoList.get(newInfoList.size() - 1).fromBelongsRegion == infoList.get(infoList.size() - 1).fromBelongsRegion) {
                    //no need to refresh
                    return;
                }
                //need to refresh, update infoList
                infoList = newInfoList;
            }
        }

        adapter = new MyPagerAdapter();
        viewPager.setAdapter(adapter);
    }

    public void showInfo() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (infoList == null || infoList.isEmpty())
                    return;

                ((MainActivity) getContext()).placeholderFragment.left.setVisibility(INVISIBLE);
                if (infoList.size() == 1)
                    ((MainActivity) getContext()).placeholderFragment.right.setVisibility(INVISIBLE);
                else
                    ((MainActivity) getContext()).placeholderFragment.right.setVisibility(VISIBLE);
            }
        }, 500);

        this.setVisibility(VISIBLE);
        ((MainActivity) getContext()).placeholderFragment.routeFailIntoLayout.setVisibility(View.INVISIBLE);
        ((MainActivity) getContext()).placeholderFragment.slidingTransferLayout.setVisibility(VISIBLE);
        amc.slideFadeIn(((MainActivity) getContext()).placeholderFragment.slidingTransferLayout, 500, 0);
    }

    public void showRouteFailInfo() {
        ((MainActivity) getContext()).placeholderFragment.routeFailIntoLayout.setVisibility(View.VISIBLE);
        amc.scaleIn(((MainActivity) getContext()).placeholderFragment.routeFailIntoLayout, 500, 0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                amc.scaleOut(((MainActivity) getContext()).placeholderFragment.routeFailIntoLayout, 500, 0);

                ((MainActivity) getContext()).placeholderFragment.routeFailIntoLayout.setVisibility(View.INVISIBLE);

            }
        }, 4000);
    }

    public void closeInfo() {
        if (infoList == null)
            return;

        infoList.clear();
        adapter = null;
        viewPager.setAdapter(null);
        this.setVisibility(GONE);
//        ((MainActivity)getContext()).placeholderFragment.routeFailIntoLayout.setVisibility(View.INVISIBLE);
        ((MainActivity) getContext()).placeholderFragment.left.setVisibility(INVISIBLE);
        ((MainActivity) getContext()).placeholderFragment.right.setVisibility(INVISIBLE);
    }

    public void setRoutingManager(PathRoutingManager mRoutingHandler) {
        pm = mRoutingHandler;
    }

    public void setPagerPositionByLocatingFloorName(String floorName) {
        if (infoList == null || infoList.isEmpty() || viewPager == null || adapter == null) {
            isLockCenter = false;
            return;
        }

        if (infoList.get(viewPager.getCurrentItem()).fromFloorname.equals(floorName)) {
            isLockCenter = false;
            return;
        }

        for (int i = 0; i < infoList.size(); i++) {
            if (infoList.get(i).fromFloorname.equals(floorName)) {
                viewPager.setCurrentItem(i);
                break;
            }
        }
    }

    private class TransferItem {
        int iconRes;
        int direction;
        String toFloorname;
        double lon;
        double lat;
        int transfer_id;
        String fromFloorname;
        LocationRegion fromBelongsRegion;

        TransferItem(SAILS.GeoNode gn, int iconRes, int direction, String toFloorname) {
            this.lon = gn.longitude;
            this.lat = gn.latitude;
            this.iconRes = iconRes;
            this.direction = direction;
            this.toFloorname = toFloorname;
            this.transfer_id = gn.BelongsRegion==null?0:gn.BelongsRegion.self;
            this.fromFloorname = gn.BelongsRegion==null?"":gn.BelongsRegion.getFloorName();
            this.fromBelongsRegion = gn.BelongsRegion;
        }
    }

    @Override
    protected void onFinishInflate() {
        try {
            //get viewpager layout resource
            viewPager = (ViewPager) getChildAt(0);
            viewPager.setOnPageChangeListener(this);
        } catch (Exception e) {
            throw new IllegalStateException("The root child of PagerContainer must be a ViewPager");
        }
    }

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.sliding_transfer_info, null);
            ImageView transferIcon = (ImageView) view.findViewById(R.id.transferSubtypeIcon);
            TextView updownText = (TextView) view.findViewById(R.id.updown_remind_text);
            TextView floorname = (TextView) view.findViewById(R.id.entirefloorname);
            ImageView updownIcon = (ImageView) view.findViewById(R.id.updownIcon);
            transferIcon.setImageResource(infoList.get(position).iconRes);
            if (infoList.get(position).direction == UP) {
                updownText.setText(getResources().getString(R.string.up_remind));
                updownIcon.setImageResource(R.drawable.uparrow);
                floorname.setText(sails.getFloorDescription(infoList.get(position).toFloorname));
            } else if (infoList.get(position).direction == DOWN) {
                updownText.setText(getResources().getString(R.string.down_remind));
                updownIcon.setImageResource(R.drawable.downarrow);
                floorname.setText(sails.getFloorDescription(infoList.get(position).toFloorname));
            } else if (infoList.get(position).direction == TARGET) {
                updownText.setText(getResources().getString(R.string.target));
                updownIcon.setVisibility(GONE);
                floorname.setText(infoList.get(position).toFloorname);
            }


            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return infoList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }
    }

    private boolean isLockCenter = false;

    public void setLockCenterTrigger() {
        if (infoList == null || infoList.isEmpty() || viewPager == null || adapter == null)
            return;


        isLockCenter = true;
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(final int i) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (infoList.get(i) == null)
                    return;

                if (isLockCenter) {
                    isLockCenter = false;
                    return;
                }

                GeoPoint geoPoint = new GeoPoint(infoList.get(i).lat, infoList.get(i).lon);
                mapview.clear();
                mapview.loadFloorMap(infoList.get(i).fromFloorname);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (pm != null) {
                            List<GeoPoint> geoPointList = pm.getCurrentFloorRoutingGeoPointList();
                            if (getContext() != null) {
                                ((MainActivity) getContext()).mSailsMapView.autoSetMapZoomAndView(geoPointList);
//                                ((MainActivity) getContext()).mSailsMapView.setAnimationToZoom((byte)22);
                            }

                        }
                    }
                };
                new Handler().postDelayed(runnable, 1000);
//                mapview.setAnimationToZoom((byte)19);
//                mapview.setAnimationMoveMapTo(geoPoint);
            }
        }, 500);

        if (i == 0)
            ((MainActivity) getContext()).placeholderFragment.left.setVisibility(INVISIBLE);
        else
            ((MainActivity) getContext()).placeholderFragment.left.setVisibility(VISIBLE);

        if (i == infoList.size() - 1)
            ((MainActivity) getContext()).placeholderFragment.right.setVisibility(INVISIBLE);
        else
            ((MainActivity) getContext()).placeholderFragment.right.setVisibility(VISIBLE);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
