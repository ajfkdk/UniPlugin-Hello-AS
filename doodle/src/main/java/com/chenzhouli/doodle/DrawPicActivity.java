package com.chenzhouli.doodle;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import cn.hzw.doodle.DoodleActivity;
import cn.hzw.doodle.DoodleParams;
import cn.hzw.doodle.DoodleView;

public class DrawPicActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //获取上一级的传递参数
        Bundle bundle = getIntent().getExtras();
        String current_image_url = bundle.getString("imgPath");


        DoodleParams params = new DoodleParams();
        params.mIsFullScreen = true;
        // 图片路径
        params.mImagePath = current_image_url;
        // 初始画笔大小
        params.mPaintUnitSize = DoodleView.DEFAULT_SIZE;
        // 画笔颜色
        params.mPaintColor = Color.RED;
        // 是否支持缩放item
        params.mSupportScaleItem = true;
        //剪切获取current_image_url的最后一个文件名
        String fileName=current_image_url.substring(current_image_url.lastIndexOf("/")+1,current_image_url.lastIndexOf("."))+"_doodled";
//        指定文件名
        params.mSavePath=current_image_url.substring(0,current_image_url.lastIndexOf("/")+1)+fileName + ".jpg";

//        申请获取存储权限
        if (ContextCompat.checkSelfPermission(DrawPicActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DrawPicActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        // 启动涂鸦页面
        DoodleActivity.startActivityOnly(DrawPicActivity.this, params);

    }
}
