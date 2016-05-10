package com.sails.hkiademo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sails.engine.LocationRegion;

/**
 * Created by Richard on 2014/1/18.
 */
public class LocationRegionListAdapter extends ArrayAdapter<LocationRegion> {
    private final Context context;
    private final LocationRegion[] values;

    public LocationRegionListAdapter(Context context, LocationRegion[] values) {
        super(context, R.layout.search_rowlayout, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.search_rowlayout, parent, false);
        LocationRegion lr=values[position];
        TextView textViewMain = (TextView) rowView.findViewById(R.id.child_tv);
        TextView textViewSub = (TextView) rowView.findViewById(R.id.child_subtv);
        textViewMain.setText(lr.getName());
        textViewSub.setText(lr.getFloorDescription());

        ImageView imageView = (ImageView) rowView.findViewById(R.id.child_iv);
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
        return rowView;
    }
}
