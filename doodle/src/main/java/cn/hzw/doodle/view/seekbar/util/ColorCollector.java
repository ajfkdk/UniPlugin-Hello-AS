package cn.hzw.doodle.view.seekbar.util;


/**
 * for collecting each section track color
 */
public interface ColorCollector {
    /**
     * to collect each section track's color
     *
     * @param colorIntArr ColorInt the container for each section tracks' color.
     *                    this array's length will auto equals section track' count.
     * @return True if apply color , otherwise no change
     */
    boolean collectSectionTrackColor(int[] colorIntArr);
}