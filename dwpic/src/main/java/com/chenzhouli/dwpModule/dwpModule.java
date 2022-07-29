package com.chenzhouli.dwpModule;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

public class dwpModule extends UniModule {
    public static int REQUEST_CODE = 1000;

    private String imgPath = "nothing";

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && data.hasExtra("imgPath")) {
            Log.e("TestModule", "原生页面返回----" + data.getStringExtra("imgPath"));
            imgPath = data.getStringExtra("imgPath");
            returnData(data.getStringExtra("imgPath"));
        } else {
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

            Intent intent = new Intent(mUniSDKInstance.getContext(), DrawPicActivity.class);
            intent.putExtra("imgPath", imgPath);
            ((Activity) mUniSDKInstance.getContext()).startActivityForResult(intent, REQUEST_CODE);
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
public void testAsyncFunc(JSONObject options, UniJSCallback callback) {
    Log.e("TAG", "testAsyncFunc--"+options);
    if(callback != null) {
        JSONObject data = new JSONObject();
        data.put("code", "success");
        callback.invoke(data);

        //callback.invokeAndKeepAlive(data);
    }
}

    //run JS thread
    @UniJSMethod (uiThread = false)
    public JSONObject testSyncFunc(){
        JSONObject data = new JSONObject();
        data.put("code", "success");
        return data;
    }


    //run JS thread
    @UniJSMethod (uiThread = false)
    public JSONObject testSyncFunc(int a,int b){
        JSONObject data = new JSONObject();
        data.put("code", a+b);
        return data;
    }
    @UniJSMethod (uiThread = true)
    public void gotoNativePage(){
        if(mUniSDKInstance != null && mUniSDKInstance.getContext() instanceof Activity) {
            Intent intent = new Intent(mUniSDKInstance.getContext(), NativePageActivity.class);
            ((Activity)mUniSDKInstance.getContext()).startActivityForResult(intent, REQUEST_CODE);
        }
    }

}
