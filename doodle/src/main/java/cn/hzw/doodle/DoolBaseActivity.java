package cn.hzw.doodle;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;

public class DoolBaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        getScreenSize();
    }

    public int setScreenWidth;
    public int setScreenHeight;

    private void getScreenSize() {
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        setScreenWidth = localDisplayMetrics.widthPixels;
        setScreenHeight = localDisplayMetrics.heightPixels;
    }

}
