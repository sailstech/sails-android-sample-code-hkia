package com.sails.hkiademo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;



import java.util.List;

/**
 * Created by Richard on 2014/1/11.
 */
public class FunctionManager {
    private boolean functionViewON=false;
    private Animation endAnimation=null;
    private Animation startAnimation=null;

    enum FunctionType {
        FOOD,EXIT, REFRESHMENT,ATM,STORE,SHOPPING, TOILET
    }
    final private FrameLayout frameLayout;
    private final Context context;
    private final int numberElementInRow;
    private final List<FunctionElement> functionElementList;
    public interface OnFunctionClickListener {
        void OnClick(FunctionType type);
    }
    OnFunctionClickListener onFunctionClickListener=null;
    public void setOnFunctionClickListener(OnFunctionClickListener f) {
        onFunctionClickListener=f;
    }
    public void setEndAnimation(Animation anim) {
        endAnimation=anim;
    }
    public void setStartAnimation(Animation anim) {
        startAnimation=anim;
    }
    FunctionManager(Context context,FrameLayout view,List<FunctionElement> functionElementList, int numberElementInRow) {
        this.frameLayout =view;
        this.numberElementInRow=numberElementInRow;
        this.functionElementList=functionElementList;
        this.context=context;
//        DisplayMetrics dm = new DisplayMetrics();
//        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);

        frameLayout.setBackgroundResource(R.drawable.function_layout);
        ScrollView scrollView=new ScrollView(context);
        frameLayout.addView(scrollView);
        LinearLayout linearLayout=new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(linearLayout);
        LinearLayout ll=new LinearLayout(context);
        int count=functionElementList.size()+functionElementList.size()%numberElementInRow;
        for(int i=0;i<count;i++) {
            if(i%numberElementInRow==0) {
                android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                ll.setLayoutParams(params);
                ll=new LinearLayout(context);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.addView(ll);
                ll.setWeightSum(numberElementInRow);
            }
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            LinearLayout ll2=new LinearLayout(context);
            ll2.setLayoutParams(params);
            int dpx=(int)context.getResources().getDimension(R.dimen.fm_3element_padding);
            if(numberElementInRow>=4) {
                dpx=(int)context.getResources().getDimension(R.dimen.fm_4element_padding);
            }
            ll2.setPadding(dpx,dpx,dpx,dpx);
            ll2.setOrientation(LinearLayout.VERTICAL);
            int j=i;
            if(i>=functionElementList.size()) {
                j=functionElementList.size()-1;
                ll2.setVisibility(View.INVISIBLE);
            } else {
                functionElementList.get(i).view=ll2;
                ll2.setOnClickListener(onClickListener);
            }
            ImageView iv=new ImageView(context);
            iv.setImageDrawable(functionElementList.get(j).drawable);
            iv.setAdjustViewBounds(true);
            TextView tv=new TextView(context);
            params = new android.widget.LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tv.setLayoutParams(params);
            tv.setText(functionElementList.get(j).label);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.getResources().getDimension(R.dimen.fm_text_size));
            tv.setTextColor(context.getResources().getColor(R.color.fm_text_color));
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            ll2.addView(iv);
            ll2.addView(tv);
            ll.addView(ll2);
        }
    }
    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(onFunctionClickListener==null)
                return;
            for(FunctionElement fe:functionElementList) {
                if(fe.view==view) {
                    onFunctionClickListener.OnClick(fe.type);
                }
            }
        }
    };
    public boolean isOpened() {
        return functionViewON;
    }
    public void openFunctionView() {
        frameLayout.setVisibility(View.VISIBLE);
        if(startAnimation!=null)
            frameLayout.startAnimation(startAnimation);
        functionViewON=true;
    }

    public void closeFunctionView() {
        if(functionViewON) {
            if(endAnimation!=null) {
                frameLayout.startAnimation(endAnimation);
                endAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        frameLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            } else {
                frameLayout.setVisibility(View.GONE);
            }
            functionViewON=false;
        }
    }

    static public class FunctionElement {
        public final FunctionType type;
        public final Drawable drawable;
        public final String label;
        View view=null;
        FunctionElement(FunctionType type, Drawable drawable, String label) {
            this.type=type;
            this.drawable=drawable;
            this.label=label;
        }


    }

}
