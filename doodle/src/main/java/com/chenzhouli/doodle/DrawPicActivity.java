package com.chenzhouli.doodle;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.DataProvider.sayHelloUtle;

import cn.hzw.doodle.DoodleActivity;
import cn.hzw.doodle.DoodleParams;
import cn.hzw.doodle.DoodleView;

public class DrawPicActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new sayHelloUtle().sayHello();
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
//        String fileName=current_image_url.substring(current_image_url.lastIndexOf("/")+1,current_image_url.lastIndexOf("."))+"_doodled";
////        指定文件名
//        params.mSavePath=current_image_url.substring(0,current_image_url.lastIndexOf("/")+1)+fileName + ".jpg";
        params.mSavePath=current_image_url;
//        申请获取存储权限
        requestPermission();
        // 启动涂鸦页面
        DoodleActivity.startActivityOnly(DrawPicActivity.this, params);
        finish();
    }
    private static final int REQUEST_CODE = 1024;
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 30 ){
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" +getApplication().getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else {
            // 先判断有没有权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this,"存储权限获取失败",Toast.LENGTH_LONG).show();;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
            } else {
                Toast.makeText(this,"存储权限获取失败",Toast.LENGTH_LONG).show();;

            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void openPermissionPage(Activity activity) {
        Toast.makeText(activity, "open:"+activity.toString(), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.parse("package:" + activity.getApplication().getPackageName()));
        activity.startActivityForResult(intent, REQUEST_CODE);
    }
}
