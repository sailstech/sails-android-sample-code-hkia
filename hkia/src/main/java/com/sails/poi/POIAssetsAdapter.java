package com.sails.poi;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by richard on 2016/5/11.
 */
public class POIAssetsAdapter {
    static public void read(Context context, String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
