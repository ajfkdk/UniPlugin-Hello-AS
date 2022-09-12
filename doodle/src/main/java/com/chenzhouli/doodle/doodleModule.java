package com.chenzhouli.doodle;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSONObject;

import cn.hzw.doodle.DoodleActivity;
import cn.hzw.doodle.DoodleParams;
import cn.hzw.doodle.DoodleView;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class doodleModule extends UniModule {
    public static int REQUEST_CODE = 1000;

    private String imgPath = "nothing";
    private Activity activity;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && data.hasExtra("imgPath")) {
            Log.e("TestModule", "原生页面返回----" + data.getStringExtra("imgPath"));
            imgPath = data.getStringExtra("imgPath");
            returnData(data.getStringExtra("imgPath"));
        }  else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //run JS thread
    @UniJSMethod(uiThread = false)
    public JSONObject returnData(Object data) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("data", data);
        return jsonObj;
    }

    @UniJSMethod(uiThread = true)
    public void drawPic(String imgPath) {
        if (mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            DoodleParams params = new DoodleParams();
            params.mIsFullScreen = true;
            // 图片路径
            params.mImagePath = imgPath;
            // 初始画笔大小
            params.mPaintUnitSize = DoodleView.DEFAULT_SIZE;
            // 画笔颜色
            params.mPaintColor = Color.RED;
            // 是否支持缩放item
            params.mSupportScaleItem = true;
//            //剪切获取current_image_url的最后一个文件名
            String fileName=imgPath.substring(imgPath.lastIndexOf("/")+1,imgPath.lastIndexOf("."))+"_doodled";
//           指定文件名
            params.mSavePath=imgPath.substring(0,imgPath.lastIndexOf("/")+1)+fileName + ".jpg";

//            params.mSavePath = imgPath;
            activity = (Activity) mUniSDKInstance.getContext();


//        申请获取存储权限
            requestPermission((Activity) mUniSDKInstance.getContext());
            DoodleActivity.startActivityOnly(activity, params);


        }
    }

    private static final int REQUEST_CODE_P = 1024;

    private void requestPermission(Activity InnerActivity) {
        // 先判断有没有权限
//       如果当前sdk大于30
        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + InnerActivity.getApplication().getPackageName()));
                InnerActivity.startActivityForResult(intent, REQUEST_CODE_P);
            }
        } else {
            //        申请获取存储权限
            if (ContextCompat.checkSelfPermission(mUniSDKInstance.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(InnerActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_P) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(activity, "存储权限获取失败", Toast.LENGTH_LONG).show();
                ;
            }
        }
    }


    //    -------备选方案
//run JS thread
    @UniJSMethod(uiThread = false)
    public JSONObject getimgPath() {
        JSONObject data = new JSONObject();
        data.put("data", imgPath);
        return data;
    }

    //    --test
//run ui thread
    @UniJSMethod(uiThread = true)
    public void requestMyPermission() {
        Activity pActivity = (Activity) mUniSDKInstance.getContext();
        Toast.makeText(mUniSDKInstance.getContext(), "requestPermission:"+pActivity.toString(), Toast.LENGTH_LONG).show();
        requestPermission(pActivity);
    }



    //run JS thread
    @UniJSMethod(uiThread = true)
    public void openPermissionPage() {
        Activity innerActivity=(Activity) mUniSDKInstance.getContext();
        Toast.makeText(innerActivity, "open:"+innerActivity.toString(), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.parse("package:" + innerActivity.getApplication().getPackageName()));
        innerActivity.startActivityForResult(intent, REQUEST_CODE_P);
    }


    //run JS thread
    @UniJSMethod(uiThread = false)
    public JSONObject testSyncFunc(int a, int b) {
        JSONObject data = new JSONObject();
        data.put("code", a + b);
        return data;
    }


}
