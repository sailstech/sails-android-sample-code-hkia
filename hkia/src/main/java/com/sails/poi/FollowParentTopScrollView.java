package com.sails.poi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by richard on 2016/5/12.
 */
public class FollowParentTopScrollView extends ScrollView {
    // true if we can scroll (not locked)
    // false if we cannot scroll (locked)
    private boolean mScrollable = false;

    public FollowParentTopScrollView(Context context) {
        super(context);
    }


    public FollowParentTopScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setScrollingEnabled(boolean enabled) {
        mScrollable = enabled;
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // if we can scroll pass the event to the superclass
                if (mScrollable) return super.onTouchEvent(ev);
                // only continue to handle the touch event if scrolling enabled
                return mScrollable; // mScrollable is always false at this point
            default:
                return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        if (!mScrollable) return false;
        else return super.onInterceptTouchEvent(ev);
    }

}