package com.chenzhouli.ExeceptionTool;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public  class ResolveExeception {
    public static final String PATH = Environment.getExternalStorageDirectory() + "/crash/log/";
    //可以根据自己需求来,比如获取手机厂商、型号、系统版本、内存大小等等
    public static  void dumpExceptionToFile(Throwable e) {
        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        long timeMillis = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timeMillis));
        File file = new File(PATH  + "QC.txt");
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.println(time);
            pw.print("Android版本号 :");
            pw.println(Build.VERSION.RELEASE);
            pw.print("手机型号 :");
            pw.println(Build.MODEL);
            pw.print("CUP架构 :");
            pw.println(Build.CPU_ABI);
            e.printStackTrace(pw);
            pw.close();
        } catch (IOException ex) {
            Log.e("TAG", "dump crash info error");
        }

    }

}
