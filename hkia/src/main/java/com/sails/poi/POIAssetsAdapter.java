package com.sails.poi;

import android.content.Context;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by richard on 2016/5/11.
 */
public class POIAssetsAdapter {
    static public List<POI> poiList=new ArrayList<>();
    static public void Clear() {
        poiList.clear();
        POI.Clear();
    }
    static public List<POI> Import(Context context, String fileName) {

        try {
            InputStream is = context.getAssets().open(fileName);
            poiList=(List<POI>)  LoganSquare.parseList(is, POI.class);
//            (List<POI>) LoganSquare.parse(is, POI.class);
//            poiList.addAll();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return poiList;
    }
}
