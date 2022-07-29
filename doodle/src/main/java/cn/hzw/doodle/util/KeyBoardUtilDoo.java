package cn.hzw.doodle.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


/***
 * 键盘的开启，隐藏
 */
public class KeyBoardUtilDoo {

    public static void showKeyBord(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
    }

    public static void hiddleBord(View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0); //强制隐藏键盘
    }


}
