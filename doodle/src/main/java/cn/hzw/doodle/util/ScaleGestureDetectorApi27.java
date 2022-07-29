package cn.hzw.doodle.util;


import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.view.GestureDetector.SimpleOnGestureListener;

import com.chenzhouli.doodle.R;
import cn.hzw.doodle.util.listener.OnScaleGestureListener;

public class ScaleGestureDetectorApi27 {
    private static final String TAG = "ScaleGestureDetectorApi27";
    private final Context mContext;
    private final OnScaleGestureListener mListener;
    private float mFocusX;
    private float mFocusY;
    private boolean mQuickScaleEnabled;
    private boolean mStylusScaleEnabled;
    private float mCurrSpan;
    private float mPrevSpan;
    private float mInitialSpan;
    private float mCurrSpanX;
    private float mCurrSpanY;
    private float mPrevSpanX;
    private float mPrevSpanY;
    private long mCurrTime;
    private long mPrevTime;
    private boolean mInProgress;
    private int mSpanSlop;
    private int mMinSpan;
    private final Handler mHandler;
    private float mAnchoredScaleStartX;
    private float mAnchoredScaleStartY;
    private int mAnchoredScaleMode;
    private static final long TOUCH_STABILIZE_TIME = 128L;
    private static final float SCALE_FACTOR = 0.5F;
    private static final int ANCHORED_SCALE_MODE_NONE = 0;
    private static final int ANCHORED_SCALE_MODE_DOUBLE_TAP = 1;
    private static final int ANCHORED_SCALE_MODE_STYLUS = 2;
    private GestureDetector mGestureDetector;
    private boolean mEventBeforeOrAboveStartingGestureEvent;

    public ScaleGestureDetectorApi27(Context context, OnScaleGestureListener listener) {
        this(context, listener, (Handler) null);
    }

    public ScaleGestureDetectorApi27(Context context, OnScaleGestureListener listener, Handler handler) {
        this.mAnchoredScaleMode = 0;
        this.mContext = context;
        this.mListener = listener;
        this.mSpanSlop = ViewConfiguration.get(context).getScaledTouchSlop() * 2;
        Resources res = context.getResources();
//        this.mMinSpan = res.getDimensionPixelSize(dimen.androids_api27_config_minScalingSpan);
        this.mMinSpan = 27;
        this.mHandler = handler;
        int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        if (targetSdkVersion > 18) {
            this.setQuickScaleEnabled(true);
        }

        if (targetSdkVersion > 22) {
            this.setStylusScaleEnabled(true);
        }

    }

    public boolean onTouchEvent(MotionEvent event) {
        this.mCurrTime = event.getEventTime();
        int action = event.getActionMasked();
        if (this.mQuickScaleEnabled) {
            this.mGestureDetector.onTouchEvent(event);
        }

        int count = event.getPointerCount();
        boolean isStylusButtonDown = (event.getButtonState() & 32) != 0;
        boolean anchoredScaleCancelled = this.mAnchoredScaleMode == 2 && !isStylusButtonDown;
        boolean streamComplete = action == 1 || action == 3 || anchoredScaleCancelled;
        if (action == 0 || streamComplete) {
            if (this.mInProgress) {
                this.mListener.onScaleEnd(this);
                this.mInProgress = false;
                this.mInitialSpan = 0.0F;
                this.mAnchoredScaleMode = 0;
            } else if (this.inAnchoredScaleMode() && streamComplete) {
                this.mInProgress = false;
                this.mInitialSpan = 0.0F;
                this.mAnchoredScaleMode = 0;
            }

            if (streamComplete) {
                return true;
            }
        }

        if (!this.mInProgress && this.mStylusScaleEnabled && !this.inAnchoredScaleMode() && !streamComplete && isStylusButtonDown) {
            this.mAnchoredScaleStartX = event.getX();
            this.mAnchoredScaleStartY = event.getY();
            this.mAnchoredScaleMode = 2;
            this.mInitialSpan = 0.0F;
        }

        boolean configChanged = action == 0 || action == 6 || action == 5 || anchoredScaleCancelled;
        boolean pointerUp = action == 6;
        int skipIndex = pointerUp ? event.getActionIndex() : -1;
        float sumX = 0.0F;
        float sumY = 0.0F;
        int div = pointerUp ? count - 1 : count;
        float focusX;
        float focusY;
        if (this.inAnchoredScaleMode()) {
            focusX = this.mAnchoredScaleStartX;
            focusY = this.mAnchoredScaleStartY;
            if (event.getY() < focusY) {
                this.mEventBeforeOrAboveStartingGestureEvent = true;
            } else {
                this.mEventBeforeOrAboveStartingGestureEvent = false;
            }
        } else {
            for (int i = 0; i < count; ++i) {
                if (skipIndex != i) {
                    sumX += event.getX(i);
                    sumY += event.getY(i);
                }
            }

            focusX = sumX / (float) div;
            focusY = sumY / (float) div;
        }

        float devSumX = 0.0F;
        float devSumY = 0.0F;

        for (int i = 0; i < count; ++i) {
            if (skipIndex != i) {
                devSumX += Math.abs(event.getX(i) - focusX);
                devSumY += Math.abs(event.getY(i) - focusY);
            }
        }

        float devX = devSumX / (float) div;
        float devY = devSumY / (float) div;
        float spanX = devX * 2.0F;
        float spanY = devY * 2.0F;
        float span;
        if (this.inAnchoredScaleMode()) {
            span = spanY;
        } else {
            span = (float) Math.hypot((double) spanX, (double) spanY);
        }

        boolean wasInProgress = this.mInProgress;
        this.mFocusX = focusX;
        this.mFocusY = focusY;
        if (!this.inAnchoredScaleMode() && this.mInProgress && (span < (float) this.mMinSpan || configChanged)) {
            this.mListener.onScaleEnd(this);
            this.mInProgress = false;
            this.mInitialSpan = span;
        }

        if (configChanged) {
            this.mPrevSpanX = this.mCurrSpanX = spanX;
            this.mPrevSpanY = this.mCurrSpanY = spanY;
            this.mInitialSpan = this.mPrevSpan = this.mCurrSpan = span;
        }

        int minSpan = this.inAnchoredScaleMode() ? this.mSpanSlop : this.mMinSpan;
        if (!this.mInProgress && span >= (float) minSpan && (wasInProgress || Math.abs(span - this.mInitialSpan) > (float) this.mSpanSlop)) {
            this.mPrevSpanX = this.mCurrSpanX = spanX;
            this.mPrevSpanY = this.mCurrSpanY = spanY;
            this.mPrevSpan = this.mCurrSpan = span;
            this.mPrevTime = this.mCurrTime;
            this.mInProgress = this.mListener.onScaleBegin(this);
        }

        if (action == 2) {
            this.mCurrSpanX = spanX;
            this.mCurrSpanY = spanY;
            this.mCurrSpan = span;
            boolean updatePrev = true;
            if (this.mInProgress) {
                updatePrev = this.mListener.onScale(this);
            }

            if (updatePrev) {
                this.mPrevSpanX = this.mCurrSpanX;
                this.mPrevSpanY = this.mCurrSpanY;
                this.mPrevSpan = this.mCurrSpan;
                this.mPrevTime = this.mCurrTime;
            }
        }

        return true;
    }

    private boolean inAnchoredScaleMode() {
        return this.mAnchoredScaleMode != 0;
    }

    public void setQuickScaleEnabled(boolean scales) {
        this.mQuickScaleEnabled = scales;
        if (this.mQuickScaleEnabled && this.mGestureDetector == null) {
            SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
                public boolean onDoubleTap(MotionEvent e) {
                    ScaleGestureDetectorApi27.this.mAnchoredScaleStartX = e.getX();
                    ScaleGestureDetectorApi27.this.mAnchoredScaleStartY = e.getY();
                    ScaleGestureDetectorApi27.this.mAnchoredScaleMode = 1;
                    return true;
                }
            };
            this.mGestureDetector = new GestureDetector(this.mContext, gestureListener, this.mHandler);
        }

    }

    public boolean isQuickScaleEnabled() {
        return this.mQuickScaleEnabled;
    }

    public void setStylusScaleEnabled(boolean scales) {
        this.mStylusScaleEnabled = scales;
    }

    public boolean isStylusScaleEnabled() {
        return this.mStylusScaleEnabled;
    }

    public boolean isInProgress() {
        return this.mInProgress;
    }

    public float getFocusX() {
        return this.mFocusX;
    }

    public float getFocusY() {
        return this.mFocusY;
    }

    public float getCurrentSpan() {
        return this.mCurrSpan;
    }

    public float getCurrentSpanX() {
        return this.mCurrSpanX;
    }

    public float getCurrentSpanY() {
        return this.mCurrSpanY;
    }

    public float getPreviousSpan() {
        return this.mPrevSpan;
    }

    public float getPreviousSpanX() {
        return this.mPrevSpanX;
    }

    public float getPreviousSpanY() {
        return this.mPrevSpanY;
    }

    public float getScaleFactor() {
        if (!this.inAnchoredScaleMode()) {
            return this.mPrevSpan > 0.0F ? this.mCurrSpan / this.mPrevSpan : 1.0F;
        } else {
            boolean scaleUp = this.mEventBeforeOrAboveStartingGestureEvent && this.mCurrSpan < this.mPrevSpan || !this.mEventBeforeOrAboveStartingGestureEvent && this.mCurrSpan > this.mPrevSpan;
            float spanDiff = Math.abs(1.0F - this.mCurrSpan / this.mPrevSpan) * 0.5F;
            return this.mPrevSpan <= 0.0F ? 1.0F : (scaleUp ? 1.0F + spanDiff : 1.0F - spanDiff);
        }
    }

    public long getTimeDelta() {
        return this.mCurrTime - this.mPrevTime;
    }

    public long getEventTime() {
        return this.mCurrTime;
    }

    public void setMinSpan(int minSpan) {
        this.mMinSpan = minSpan;
    }

    public void setSpanSlop(int spanSlop) {
        this.mSpanSlop = spanSlop;
    }

    public int getMinSpan() {
        return this.mMinSpan;
    }

    public int getSpanSlop() {
        return this.mSpanSlop;
    }

    public static class SimpleOnScaleGestureListener implements android.view.ScaleGestureDetector.OnScaleGestureListener {
        public SimpleOnScaleGestureListener() {
        }

        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    }


}
