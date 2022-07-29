package cn.hzw.doodle.util.listener;

import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

public interface IOnTouchGestureListener extends OnGestureListener, GestureDetector.OnDoubleTapListener, OnScaleGestureListener {
    void onUpOrCancel(MotionEvent var1);

    void onScrollBegin(MotionEvent var1);

    void onScrollEnd(MotionEvent var1);
}