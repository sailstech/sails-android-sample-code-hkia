package com.sails.hkiademo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sails.engine.LocationRegion;

import java.util.List;
import java.util.Map;

/**
 * Created by Richard on 2014/1/7.
 */
class ExpandableAdapter extends BaseExpandableListAdapter {
    /*final static int NORMAL=0;
    final static int ENGLISH=1;
    final static int CHINESE_BIG5=2;
    final static int CHINESE_SM=3;
    final static int JAPANESE=4;
    public static int FONT_LANGUAGE=CHINESE_BIG5;*/
    private Context context;
    List<Map<String, String>> groups;
    List<List<Map<String, LocationRegion>>> childs;

    public ExpandableAdapter(Context context, List<Map<String, String>> groups, List<List<Map<String, LocationRegion>>> childs) {
        this.context = context;
        this.groups = groups;
        this.childs = childs;
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childs.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childs.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.group, null);
        String text = ((Map<String, String>) getGroup(groupPosition)).get("group");
        TextView tv = (TextView) linearLayout.findViewById(R.id.group_tv);
        tv.setText(text);
//        linearLayout.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        tv.setTextColor(context.getResources().getColor(android.R.color.white));
        return linearLayout;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.child, null);
        LocationRegion lr = ((Map<String, LocationRegion>) getChild(groupPosition, childPosition)).get("child");
        TextView tv = (TextView) linearLayout.findViewById(R.id.child_tv);
        tv.setText(lr.getName());
        ImageView imageView = (ImageView)linearLayout.findViewById(R.id.child_iv);
        if(lr.type!=null&&lr.subtype!=null&&lr.type.equals("store")&&lr.subtype.equals("food"))
            imageView.setImageResource(R.drawable.food_small_dark);
        if(lr.type!=null&&lr.subtype!=null&&lr.type.equals("store")&&lr.subtype.equals("drink"))
            imageView.setImageResource(R.drawable.refreshment_small_dark);
        if(lr.type!=null&&lr.subtype!=null&&lr.type.equals("store")&&lr.subtype.equals("shopping"))
            imageView.setImageResource(R.drawable.shopping_small_dark);
        if(lr.type!=null&&lr.subtype!=null&&lr.type.equals("facility")&&lr.subtype.equals("toilet"))
            imageView.setImageResource(R.drawable.toilet_small_dark);
        if(lr.type!=null&&lr.type.equals("gateway"))
            imageView.setImageResource(R.drawable.exit_small_dark);
        if(lr.type!=null&&lr.subtype!=null&&lr.type.equals("facility")&&lr.subtype.equals("atm"))
            imageView.setImageResource(R.drawable.atm_small_dark);
        return linearLayout;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}