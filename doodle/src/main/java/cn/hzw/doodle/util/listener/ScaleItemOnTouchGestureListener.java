package cn.hzw.doodle.util.listener;

import cn.hzw.doodle.DoodleOnTouchGestureListener;
import cn.hzw.doodle.DoodleView;
import cn.hzw.doodle.core.IDoodleItem;
import cn.hzw.doodle.util.ScaleGestureDetectorApi27;

/**
 * Change selected item's size by scale gesture.
 * Note that this class extends DoodleOnTouchGestureListener and does not affect the original gesture logic.
 * <p>
 * 注意，这里继承了DoodleOnTouchGestureListener，不影响原有的手势逻辑
 */
public class ScaleItemOnTouchGestureListener extends DoodleOnTouchGestureListener {

    public ScaleItemOnTouchGestureListener(DoodleView doodle, ISelectionListener listener) {
        super(doodle, listener);
    }

    @Override
    public boolean onScale(ScaleGestureDetectorApi27 detector) {
        if (getSelectedItem() != null) {
            IDoodleItem item = getSelectedItem();
            item.setSize(item.getSize() * detector.getScaleFactor());
            return true;
        } else {
            return super.onScale(detector);
        }
    }
}