package com.sails.ui;

/**
 * Created by richard on 2016/5/13.
 */
        import android.content.Context;
        import android.content.res.TypedArray;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Paint;
        import android.graphics.Path;
        import android.graphics.RectF;
        import android.graphics.Typeface;
        import android.os.Handler;
        import android.os.Message;
        import android.util.AttributeSet;
        import android.view.View;
        import android.view.animation.Animation;


        import java.lang.ref.WeakReference;

/**
 * Created by Richard on 2013/6/24.
 */
public class DirectionIndicator extends View {
    public static final int PHASE1 = 0;
    public static final int PHASE2 = 1;
    public static final int PHASE3 = 2;
    private int saveDirection = 0;
    public OnCloseEventListener mListener = null;

    public interface OnCloseEventListener {
        public void OnClose();
    }

    public void setOnCloseEventListener(OnCloseEventListener l) {
        mListener = l;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (parentHeight == 0) parentHeight = Integer.MAX_VALUE;
        if (parentWidth == 0) parentWidth = Integer.MAX_VALUE;
        if (parentWidth < parentHeight)
            setMeasuredDimension(parentWidth, parentWidth);
        else
            setMeasuredDimension(parentHeight, parentHeight);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }


    public int phase = PHASE1;
    private final Context context;
    private int width, height;
    private float radius;
    private static float INNER_RADIUS_RATIO = 0.75f;
    private static float OUTER_RADIUS_RATIO = 0.81f;
    private static int FAN_ANGLE = 75;
    private static int APPROACH_FAN_ANGLE = 120;
    private Animation startViewAnimate = null;
    private Animation endViewAnimate = null;
    private Handler handler = new Handler();
    private Paint innerCirclePaintPhase1 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint innerCirclePaintPhase2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint innerCirclePaintPhase3 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint outerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint phase1TextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint phase2TextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint phase3TextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int arrowAlpha1;
    private int arrowAlpha2;
    private int arrowAlpha3;
    private int arrowAlpha4;
    private String centerText = "30m";
    private String centerTextNext = "";
    //public int theme=0;
    public static int GREEN_THEME = 0;

    private static int GREEN_THEME_INNERCIRCLE_PHASE1 = Color.rgb(255, 255, 255);
    private static int GREEN_THEME_INNERCIRCLE_PHASE2 = Color.rgb(181, 203, 133);
    private static int GREEN_THEME_INNERCIRCLE_PHASE3 = Color.rgb(104, 128, 55);

    private static int GREEN_THEME_OUTERCIRCLE = Color.rgb(235, 241, 222);
    private static int GREEN_THEME_PHASE1_TEXT = Color.rgb(133, 164, 70);
    private static int GREEN_THEME_PHASE2_TEXT = Color.rgb(104, 128, 55);
    private static int GREEN_THEME_PHASE3_TEXT = Color.rgb(255, 255, 255);
    private static int GREEN_THEME_ARROWS = Color.rgb(133, 164, 70);
    private static int GREEN_THEME_RING = Color.rgb(133, 164, 70);
    private float direction = 0;
    private static int[] TEXT_ALPHA_ANIMATE = {
            250, 230, 210, 190, 170, 150, 130, 110, 90, 70, 50, 30, 20, 0, 20, 30, 50, 70, 90, 110, 130, 150, 170, 190, 230, 250};
    private static int[][] ARROW_ALPHA_ANIMATE =
            {
                    {10, 0, 0, 0},
                    {20, 0, 0, 0},
                    {30, 0, 0, 0},
                    {40, 0, 0, 0},
                    {50, 10, 0, 0},
                    {60, 20, 0, 0},
                    {70, 30, 0, 0},
                    {80, 40, 0, 0},
                    {90, 50, 10, 0},
                    {100, 60, 20, 0},
                    {110, 70, 30, 0},
                    {120, 80, 40, 0},
                    {130, 90, 50, 10},
                    {140, 100, 60, 20},
                    {150, 110, 70, 30},
                    {160, 120, 80, 40},
                    {170, 130, 90, 50},
                    {180, 140, 100, 60},
                    {190, 150, 110, 70},
                    {200, 160, 120, 80},
                    {210, 170, 130, 90},
                    {220, 180, 140, 100},
                    {230, 190, 150, 110},
                    {240, 200, 160, 120},
                    {250, 210, 170, 130},
                    {240, 220, 180, 140},
                    {230, 230, 190, 150},
                    {220, 240, 200, 160},
                    {210, 250, 210, 170},
                    {200, 240, 220, 180},
                    {190, 230, 230, 190},
                    {180, 220, 240, 200},
                    {170, 210, 250, 210},
                    {160, 200, 240, 220},
                    {150, 190, 230, 230},
                    {140, 180, 220, 240},
                    {130, 170, 210, 250},
                    {120, 160, 200, 240},
                    {110, 150, 190, 230},
                    {100, 140, 180, 220},
                    {90, 130, 170, 210},
                    {80, 120, 160, 200},
                    {70, 110, 150, 190},
                    {60, 100, 140, 180},
                    {50, 90, 130, 170},
                    {40, 80, 120, 160},
                    {30, 70, 110, 150},
                    {20, 60, 100, 140},
                    {10, 50, 90, 130},
                    {0, 40, 80, 120},
                    {0, 30, 70, 110},
                    {0, 20, 60, 100},
                    {0, 10, 50, 90},
                    {0, 0, 40, 80},
                    {0, 0, 30, 70},
                    {0, 0, 20, 60},
                    {0, 0, 10, 50},
                    {0, 0, 0, 40},
                    {0, 0, 0, 30},
                    {0, 0, 0, 20},
                    {0, 0, 0, 10},
                    {0, 0, 0, 0}
            };
    int count=0;
    Runnable demo = new Runnable() {
        public void run() {
            if (count == 0) {
                String text = context.getString(R.string.di_follow_arrow);
                setPhase(PHASE1);
                setNextText(text);
                count++;
                handler.postDelayed(this, 3000);

            } else if (count == 1) {
                String text = "30m";
                setPhase(PHASE1);
                setNextText(text);
                count++;
                handler.postDelayed(this, 2000);

            } else if (count == 2) {
                String text = "1 min";
                setPhase(PHASE1);
                setNextText(text);
                count++;
                handler.postDelayed(this, 2000);

            } else if (count == 3) {
                String text = "28m";
                setPhase(PHASE1);
                setNextText(text);
                count++;
                handler.postDelayed(this, 5000);

            } else if (count == 4) {
                String text = "Approaching";
                setPhase(PHASE2);
                setNextText(text);
                count++;
                handler.postDelayed(this, 5000);

            } else if (count == 5) {
                String text = context.getString(R.string.di_goal);
                setPhase(PHASE3);
                setNextText(text);
                count++;
                handler.postDelayed(this, 1000);

            } else if (count == 6) {
                String text = context.getString(R.string.di_goal);
                setPhase(PHASE3);
                setNextText(text);
                count++;
                handler.postDelayed(this, 3000);

            } else if (count == 7) {
                String text = context.getString(R.string.di_enjoy);
                setNextText(text);
                count++;
                handler.postDelayed(this, 2000);
            } else if (count == 8) {
                String text = context.getString(R.string.di_goodbye1);
                setNextText(text);
                count++;
                handler.postDelayed(this, 2000);
            } else if (count == 9) {
                count = 0;
                stopAnimate();
                if (mListener != null)
                    mListener.OnClose();
            }
        }
    };
    Runnable runHere = new Runnable() {
        public void run() {

            if (count == 0) {
                String text = context.getString(R.string.di_goal);
                setPhase(PHASE3);
                setNextText(text);
                count++;
                handler.postDelayed(this, 1000);

            } else if (count == 1) {
                String text = context.getString(R.string.di_goal);
                setPhase(PHASE3);
                setNextText(text);
                count++;
                handler.postDelayed(this, 1000);

            } else if (count == 2) {
                String text = context.getString(R.string.di_goal);
                setPhase(PHASE3);
                setNextText(text);
                count++;
                handler.postDelayed(this, 3000);

            } else if (count == 3) {
                String text = context.getString(R.string.di_enjoy);
                setNextText(text);
                count++;
                handler.postDelayed(this, 2000);
            } else if (count == 4) {
                String text = context.getString(R.string.di_goodbye1);
                setNextText(text);
                count++;
                handler.postDelayed(this, 2000);
            } else if (count == 5) {
                count = 0;
                stopAnimate();
                if (mListener != null)
                    mListener.OnClose();
            }
        }
    };
    Runnable runNotHere = new Runnable() {
        public void run() {
            if (count == 0) {
                setPhase(PHASE1);
                String text = context.getString(R.string.di_nothere);
                setNextText(text);
                count++;
                handler.postDelayed(this, 2000);

            } else if (count == 1) {
                String text = context.getString(R.string.di_disnavi);
                setNextText(text);
                count++;
                handler.postDelayed(this, 2000);
            } else if (count == 2) {
                String text = context.getString(R.string.di_goodbye);
                setNextText(text);
                count++;
                handler.postDelayed(this, 2000);
            } else if (count == 3) {
                count = 0;
                stopAnimate();
                if (mListener != null)
                    mListener.OnClose();
            }
        }
    };

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        if (w > h)
            radius = h / 2;
        else
            radius = w / 2;
        setText(context.getString(R.string.di_welcome));
        centerTextNext = context.getString(R.string.di_welcome);

    }

    public void setStartViewAnimate(Animation a) {
        this.startViewAnimate = a;
    }

    public void setEndViewAnimate(Animation a) {
        this.endViewAnimate = a;
    }

    public DirectionIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        if (isInEditMode())
            return;
//        TypedArray a = context.getTheme().obtainStyledAttributes(
//                attrs,
//                R.styleable.DirectionIndicator,
//                0, 0);
//        int theme = 0;
//        try {
//            theme = a.getInt(R.styleable.DirectionIndicator_theme, 0);
//        } finally {
//            a.recycle();
//        }

        setTheme(GREEN_THEME);
//        setTheme(context);
    }

    public DirectionIndicator(Context context) {
        super(context);
        this.context = context;
        setTheme(GREEN_THEME);

    }
    public void setArrowVisible(boolean visible) {
        mRefreshHandler.isArrowAnimate=visible;
    }
    public void setDirection(int dir) {
        mRefreshHandler.isArrowAnimate=true;
        this.saveDirection = -dir;
    }

    public void setPhase(int phase) {
        this.phase = phase;
        setText(centerText);
    }

    public void setTheme(int theme) {


        if (theme == GREEN_THEME) {
            innerCirclePaintPhase1.setColor(GREEN_THEME_INNERCIRCLE_PHASE1);
            innerCirclePaintPhase2.setColor(GREEN_THEME_INNERCIRCLE_PHASE2);
            innerCirclePaintPhase3.setColor(GREEN_THEME_INNERCIRCLE_PHASE3);
            outerCirclePaint.setColor(GREEN_THEME_OUTERCIRCLE);
            phase1TextPaint.setColor(GREEN_THEME_PHASE1_TEXT);
            phase1TextPaint.setTextAlign(Paint.Align.CENTER);
            if (!isInEditMode()) {
                Typeface typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
                phase1TextPaint.setTypeface(typeface);
                phase2TextPaint.setColor(GREEN_THEME_PHASE2_TEXT);
                phase2TextPaint.setTextAlign(Paint.Align.CENTER);
                phase2TextPaint.setTypeface(typeface);
                phase3TextPaint.setColor(GREEN_THEME_PHASE3_TEXT);
                phase3TextPaint.setTextAlign(Paint.Align.CENTER);
                phase3TextPaint.setTypeface(typeface);

            }
            arrowPaint.setColor(GREEN_THEME_ARROWS);
            arcPaint.setColor(GREEN_THEME_RING);

        }
    }

    public void setText(String text) {
        centerText = text;
        Paint p = new Paint();
        if (phase == PHASE1)
            p = phase1TextPaint;
        else if (phase == PHASE2)
            p = phase2TextPaint;
        else if (phase == PHASE3)
            p = phase3TextPaint;

        autoScaleTextSize(text, p, (int) (INNER_RADIUS_RATIO * radius * 0.8 * 2));
    }

    public void setNextText(String text) {
        centerTextNext = text;
        mRefreshHandler.isTextAnimate = true;
    }

    private void autoScaleTextSize(String text, Paint paint, int targetWidth) {
        int size = 500;
        float width = 10000;
        while (width > targetWidth) {
            size -= 5;
            paint.setTextSize(size);
            width = paint.measureText(text);
            if (size == 0)
                break;
        }
    }

    static private Path CreateSingleArc(int centerx, int centery, int radius, int fanangle, int direction) {
        Path p = new Path();
        double t1 = Math.toRadians(fanangle / 2 - direction);
        double t3 = Math.toRadians(-fanangle / 2 - direction);
        RectF oval = new RectF();
        oval.set((centerx - INNER_RADIUS_RATIO * radius), (centery - INNER_RADIUS_RATIO * radius), (centerx + INNER_RADIUS_RATIO * radius), (centery + INNER_RADIUS_RATIO * radius));
        p.addArc(oval, direction - 90 + fanangle / 2, -fanangle);
        p.lineTo((float) (-Math.sin(t3) * radius * OUTER_RADIUS_RATIO + centerx), (float) (centery - Math.cos(t3) * radius * OUTER_RADIUS_RATIO));
        RectF oval1 = new RectF();
        oval1.set((centerx - OUTER_RADIUS_RATIO * radius), (centery - OUTER_RADIUS_RATIO * radius), (centerx + OUTER_RADIUS_RATIO * radius), (centery + OUTER_RADIUS_RATIO * radius));
        p.addArc(oval1, direction - 90 - fanangle / 2, fanangle);
        p.lineTo((float) (-Math.sin(t1) * radius * INNER_RADIUS_RATIO + centerx), (float) (centery - Math.cos(t1) * radius * INNER_RADIUS_RATIO));
        return p;
    }

    static private Path CreateSingleArrows(int centerx, int centery, int radius, int fanangle, int direction) {
        double t1 = Math.toRadians(fanangle / 2 - direction);
        double t2 = Math.toRadians(0 - direction);
        double t3 = Math.toRadians(-fanangle / 2 - direction);
        Path p = new Path();
        p.setFillType(Path.FillType.WINDING);
        p.moveTo((float) (-Math.sin(t1) * radius * 0.97 + centerx), (float) (centery - Math.cos(t1) * radius * 0.97));
        p.lineTo((float) (-Math.sin(t2) * radius * 1.01 + centerx), (float) (centery - Math.cos(t2) * radius * 1.03));
        p.lineTo((float) (-Math.sin(t3) * radius * 0.97 + centerx), (float) (centery - Math.cos(t3) * radius * 0.97));
        p.lineTo((float) (-Math.sin(t3) * radius * 1.03 + centerx), (float) (centery - Math.cos(t3) * radius * 1.03));
        p.lineTo((float) (-Math.sin(t2) * radius * 1.08 + centerx), (float) (centery - Math.cos(t2) * radius * 1.08));
        p.lineTo((float) (-Math.sin(t1) * radius * 1.03 + centerx), (float) (centery - Math.cos(t1) * radius * 1.03));
        return p;
    }
    public void startDemoAnimate() {
        startAnimate();
        count=0;
        handler.postDelayed(demo, 2000);
    }
    public void startAnimate() {
        this.setVisibility(View.VISIBLE);
        setPhase(PHASE1);
        if (startViewAnimate != null) {
            startViewAnimate.reset();
            this.startAnimation(startViewAnimate);
        }

        setText(context.getResources().getString(R.string.di_welcome));
        mRefreshHandler.needRefresh = true;
        mRefreshHandler.sleep(0);
//        resetAnimate();
//        handler.postDelayed(runHere, 2000);

    }

    public void resetAnimate() {
        mRefreshHandler.reset();
        arrowAlpha1 = 0;
        arrowAlpha2 = 0;
        arrowAlpha3 = 0;
        arrowAlpha4 = 0;
        phase1TextPaint.setAlpha(255);
        phase2TextPaint.setAlpha(255);
        phase3TextPaint.setAlpha(255);
    }

    public void stopAnimate() {
        handler.removeCallbacks(runNotHere);
        handler.removeCallbacks(runHere);
        handler.removeCallbacks(demo);
        if (endViewAnimate != null) {
            endViewAnimate.reset();

            this.startAnimation(endViewAnimate);
            endViewAnimate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    setVisibility(View.GONE);
                    mRefreshHandler.stopRefresh();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
//            setVisibility(View.GONE);
            mRefreshHandler.stopRefresh();
        }

    }

    private RefreshHandler mRefreshHandler = new RefreshHandler(this);


    static class RefreshHandler extends Handler {
        public boolean needRefresh = true;
        public int period = 30;
        private int arrowcount = 0;
        private int textcount = 0;
        public boolean isArrowAnimate = false;
        public boolean isTextAnimate = false;
        private WeakReference<DirectionIndicator> mNavi;


        RefreshHandler(DirectionIndicator mNavi) {
            this.mNavi = new WeakReference<DirectionIndicator>(mNavi);
        }

        public void resetArrowAlpha() {
            arrowcount = 0;
        }

        public void reset() {
            arrowcount = 0;
            textcount = 0;
        }

        @Override
        public void handleMessage(Message msg) {
            DirectionIndicator di = mNavi.get();
            boolean refresh = false;
            if (di != null) {
                if (isTextAnimate) {
                    refresh = true;

                    int alpha = TEXT_ALPHA_ANIMATE[textcount];
                    di.phase1TextPaint.setAlpha(alpha);
                    di.phase2TextPaint.setAlpha(alpha);
                    di.phase3TextPaint.setAlpha(alpha);
                    if (alpha == 0) {
                        di.setText(di.centerTextNext);
                    }
                    textcount++;
                    if (textcount >= TEXT_ALPHA_ANIMATE.length) {
                        textcount = 0;
                        isTextAnimate = false;
                    }
                }
                if(arrowcount < ARROW_ALPHA_ANIMATE.length) {
                    refresh = true;
                    if (di.phase == PHASE1) {
                        di.arrowAlpha1 = ARROW_ALPHA_ANIMATE[arrowcount][0];
                        di.arrowAlpha2 = ARROW_ALPHA_ANIMATE[arrowcount][1];
                        di.arrowAlpha3 = ARROW_ALPHA_ANIMATE[arrowcount][2];
                        di.arrowAlpha4 = ARROW_ALPHA_ANIMATE[arrowcount][3];
                    }
                    arrowcount += 2;

                }
                if (isArrowAnimate) {
                    if (arrowcount >= ARROW_ALPHA_ANIMATE.length) {
                        arrowcount = 0;
                        di.direction = di.saveDirection;
                    }
                }
                if (refresh)
                    di.invalidate();

            }
            if (needRefresh)
                this.sleep(period);

            return;
        }

        public void stopRefresh() {
            this.needRefresh = false;
            this.isArrowAnimate = false;
            this.isTextAnimate = false;
        }

        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }

    ;

    private void createArrows(Canvas canvas) {
        Path p;
        p = CreateSingleArrows(width / 2, height / 2, (int) (INNER_RADIUS_RATIO * radius * 0.76), FAN_ANGLE, (int) direction);
        arrowPaint.setAlpha(arrowAlpha1);
        canvas.drawPath(p, arrowPaint);
        p = CreateSingleArrows(width / 2, height / 2, (int) (INNER_RADIUS_RATIO * radius * 0.88), FAN_ANGLE, (int) direction);
        arrowPaint.setAlpha(arrowAlpha2);
        canvas.drawPath(p, arrowPaint);
        p = CreateSingleArrows(width / 2, height / 2, (int) (INNER_RADIUS_RATIO * radius * 1.00), FAN_ANGLE, (int) direction);
        arrowPaint.setAlpha(arrowAlpha3);
        canvas.drawPath(p, arrowPaint);
        p = CreateSingleArrows(width / 2, height / 2, (int) (INNER_RADIUS_RATIO * radius * 1.12), FAN_ANGLE, (int) direction);
        arrowPaint.setAlpha(arrowAlpha4);
        canvas.drawPath(p, arrowPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(width / 2, height / 2, OUTER_RADIUS_RATIO * radius, outerCirclePaint);
        if (phase == PHASE1) {
            canvas.drawCircle(width / 2, height / 2, INNER_RADIUS_RATIO * radius, innerCirclePaintPhase1);
            canvas.drawText(centerText, width / 2, height / 2 - (phase1TextPaint.descent() + phase1TextPaint.ascent()) / 2, phase1TextPaint);
            createArrows(canvas);
        } else if (phase == PHASE2) {
            canvas.drawCircle(width / 2, height / 2, INNER_RADIUS_RATIO * radius, innerCirclePaintPhase2);
            canvas.drawText(centerText, width / 2, height / 2 - (phase2TextPaint.descent() + phase2TextPaint.ascent()) / 2, phase2TextPaint);
            canvas.drawPath(CreateSingleArc(width / 2, height / 2, (int) radius, APPROACH_FAN_ANGLE, (int) direction), arcPaint);
        } else if (phase == PHASE3) {
            canvas.drawCircle(width / 2, height / 2, INNER_RADIUS_RATIO * radius, innerCirclePaintPhase3);
            canvas.drawText(centerText, width / 2, height / 2 - (phase3TextPaint.descent() + phase3TextPaint.ascent()) / 2, phase3TextPaint);
        }

    }
}
