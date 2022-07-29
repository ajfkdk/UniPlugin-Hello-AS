package cn.hzw.doodle.util;


import android.content.Context;
import android.os.Build.VERSION;
import android.view.GestureDetector;
import android.view.MotionEvent;

import cn.hzw.doodle.util.listener.IOnTouchGestureListener;


public class TouchGestureDetector {
    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetectorApi27 mScaleGestureDetectorApi27;
    private final IOnTouchGestureListener mOnTouchGestureListener;
    private boolean mIsScrollAfterScaled = true;

    public TouchGestureDetector(Context context, IOnTouchGestureListener listener) {
        this.mOnTouchGestureListener = new OnTouchGestureListenerProxy(listener);
        this.mGestureDetector = new GestureDetector(context, this.mOnTouchGestureListener);
        this.mGestureDetector.setOnDoubleTapListener(this.mOnTouchGestureListener);
        this.mScaleGestureDetectorApi27 = new ScaleGestureDetectorApi27(context, mOnTouchGestureListener);
        if (VERSION.SDK_INT >= 19) {
            this.mScaleGestureDetectorApi27.setQuickScaleEnabled(false);
        }

    }

    public void setScaleMinSpan(int minSpan) {
        this.mScaleGestureDetectorApi27.setMinSpan(minSpan);
    }

    public void setScaleSpanSlop(int spanSLop) {
        this.mScaleGestureDetectorApi27.setSpanSlop(spanSLop);
    }

    public void setIsLongpressEnabled(boolean isLongpressEnabled) {
        this.mGestureDetector.setIsLongpressEnabled(isLongpressEnabled);
    }

    public boolean isLongpressEnabled() {
        return this.mGestureDetector.isLongpressEnabled();
    }

    public void setIsScrollAfterScaled(boolean scrollAfterScaled) {
        this.mIsScrollAfterScaled = scrollAfterScaled;
    }

    public boolean isScrollAfterScaled() {
        return this.mIsScrollAfterScaled;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == 1 || event.getAction() == 3 || event.getAction() == 4) {
            this.mOnTouchGestureListener.onUpOrCancel(event);
        }

        boolean ret = this.mScaleGestureDetectorApi27.onTouchEvent(event);
        if (!this.mScaleGestureDetectorApi27.isInProgress()) {
            ret |= this.mGestureDetector.onTouchEvent(event);
        }

        return ret;
    }

    public abstract static class OnTouchGestureListener implements IOnTouchGestureListener {
        public OnTouchGestureListener() {
        }

        public boolean onDown(MotionEvent e) {
            return false;
        }

        public void onUpOrCancel(MotionEvent e) {
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        public void onLongPress(MotionEvent e) {
        }

        public void onScrollBegin(MotionEvent e) {
        }

        public void onScrollEnd(MotionEvent e) {
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        public void onShowPress(MotionEvent e) {
        }

        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        public boolean onScale(ScaleGestureDetectorApi27 detector) {
            return false;
        }

        public boolean onScaleBegin(ScaleGestureDetectorApi27 detector) {
            return false;
        }

        public void onScaleEnd(ScaleGestureDetectorApi27 detector) {
        }
    }



    private class OnTouchGestureListenerProxy implements IOnTouchGestureListener {
        private IOnTouchGestureListener mListener;
        private boolean mHasScaled = false;
        private boolean mIsScrolling = false;
        private MotionEvent mLastScrollMotionEvent;

        public OnTouchGestureListenerProxy(IOnTouchGestureListener listener) {
            this.mListener = listener;
        }

        public boolean onDown(MotionEvent e) {
            this.mHasScaled = false;
            this.mIsScrolling = false;
            return this.mListener.onDown(e);
        }

        public void onUpOrCancel(MotionEvent e) {
            this.mListener.onUpOrCancel(e);
            if (this.mIsScrolling) {
                this.mIsScrolling = false;
                this.mLastScrollMotionEvent = null;
                this.onScrollEnd(e);
            }

        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return this.mListener.onFling(e1, e2, velocityX, velocityY);
        }

        public void onLongPress(MotionEvent e) {
            this.mListener.onLongPress(e);
        }

        public void onScrollBegin(MotionEvent e) {
            this.mListener.onScrollBegin(e);
        }

        public void onScrollEnd(MotionEvent e) {
            this.mListener.onScrollEnd(e);
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!TouchGestureDetector.this.mIsScrollAfterScaled && this.mHasScaled) {
                this.mIsScrolling = false;
                return false;
            } else {
                if (!this.mIsScrolling) {
                    this.mIsScrolling = true;
                    this.onScrollBegin(e1);
                }

                this.mLastScrollMotionEvent = MotionEvent.obtain(e2);
                return this.mListener.onScroll(e1, e2, distanceX, distanceY);
            }
        }

        public void onShowPress(MotionEvent e) {
            this.mListener.onShowPress(e);
        }

        public boolean onSingleTapUp(MotionEvent e) {
            return this.mListener.onSingleTapUp(e);
        }

        public boolean onDoubleTap(MotionEvent e) {
            return this.mListener.onDoubleTap(e);
        }

        public boolean onDoubleTapEvent(MotionEvent e) {
            return this.mListener.onDoubleTapEvent(e);
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            return this.mListener.onSingleTapConfirmed(e);
        }

        public boolean onScale(ScaleGestureDetectorApi27 detector) {
            return  mListener.onScale(detector);
        }

        public boolean onScaleBegin(ScaleGestureDetectorApi27 detector) {
            this.mHasScaled = true;
            if (this.mIsScrolling) {
                this.mIsScrolling = false;
                this.onScrollEnd(this.mLastScrollMotionEvent);
            }

            return this.mListener.onScaleBegin(detector);
        }

        public void onScaleEnd(ScaleGestureDetectorApi27 detector) {
            this.mListener.onScaleEnd(detector);
        }
    }
}
