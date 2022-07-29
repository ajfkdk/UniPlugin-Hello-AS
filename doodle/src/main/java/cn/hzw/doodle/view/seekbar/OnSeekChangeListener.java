package cn.hzw.doodle.view.seekbar;

public interface OnSeekChangeListener {
    void onSeeking(SeekParams seekParams);

    void onStartTrackingTouch(IndicatorSeekBar seekBar);

    void onStopTrackingTouch(IndicatorSeekBar seekBar);
}