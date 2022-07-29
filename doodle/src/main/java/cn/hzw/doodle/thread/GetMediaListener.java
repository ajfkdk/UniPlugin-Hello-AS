package cn.hzw.doodle.thread;

import java.util.List;

import cn.hzw.doodle.imagepicker.BeanImage;


public interface GetMediaListener {

    void backMediaListener(boolean isTrue, List<BeanImage> lists);
}
