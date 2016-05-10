package com.sails.hkiademo;

import android.view.animation.Interpolator;

/**
 * Created by Richard on 2014/1/7.
 */
public class SAILSAnimation {
    static class ReverseInterpolator implements Interpolator {
        @Override
        public float getInterpolation(float paramFloat) {
            return Math.abs(paramFloat - 1f);
        }
    }
}
