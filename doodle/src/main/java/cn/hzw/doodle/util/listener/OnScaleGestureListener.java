package cn.hzw.doodle.util.listener;

import cn.hzw.doodle.util.ScaleGestureDetectorApi27;

public interface OnScaleGestureListener {
    boolean onScale(ScaleGestureDetectorApi27 var1);

    boolean onScaleBegin(ScaleGestureDetectorApi27 var1);

    void onScaleEnd(ScaleGestureDetectorApi27 var1);
}