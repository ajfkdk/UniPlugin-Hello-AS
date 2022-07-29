package com.chenzhouli.dwpModule;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class DrawPicActivity extends Activity {

    //自定义变量
    private Bitmap 原始图片;                         //原始图片
    private ImageView imageShow;                    //显示图片
    private Matrix matrix = new Matrix();
    //图片处理时显示备份
    private Bitmap alteredBitmap;          //图片
    private Canvas canvas;                    //画布
    private Paint 画刷;                          //画刷
    //    "/storage/emulated/0/DCIM/Camera/snapshot_1657175934713.jpg"  手机图片
    private String FilePathString = "/storage/emulated/0/DCIM/Camera/snapshot_1657175934713.jpg";

    private static final String TAG = "picture";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //去除title

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //去掉Activity上面的状态栏

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,

                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_drawpic);

        //从上一个instant获取图片路径的数据
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        FilePathString = (String) extras.get("imgPath");

        Button saveButton = findViewById(R.id.saveButton);
        imageShow = (ImageView) findViewById(R.id.imageView1);

        //载入图片尺寸大小没载入图片本身 true
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
        //图像真正解码 false
        bmpFactoryOptions.inJustDecodeBounds = false;
        原始图片 = getDiskBitmap(FilePathString);
        if (原始图片 == null) {
            Toast.makeText(DrawPicActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
            finish();
        }
        alteredBitmap = Bitmap.createBitmap(原始图片.getWidth(), 原始图片.getHeight(), 原始图片.getConfig());
        canvas = new Canvas(alteredBitmap);  //画布
        画刷 = new Paint(); //画刷
        画刷.setColor(Color.RED); //画刷颜色
        画刷.setStrokeWidth(20);
        画刷.setTextSize(30);
        画刷.setTypeface(Typeface.DEFAULT_BOLD);  //无线粗体
        matrix = new Matrix();
        canvas.drawBitmap(原始图片, matrix, 画刷);
        imageShow.setImageBitmap(alteredBitmap);

        //保存当前图片到相册
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alteredBitmap != null) {
                    //保存图片
                    String filePath = saveImageToGallery(DrawPicActivity.this, alteredBitmap);
                    Toast.makeText(DrawPicActivity.this, "图片已保存至" + filePath, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("imgPath",filePath);
                    setResult(dwpModule.REQUEST_CODE, intent);
//                    退出程序
                    finish();
                }

            }
        });


        //触屏缩放图片监听 注:XML中修改android:scaleType="matrix"
        imageShow.setOnTouchListener(new OnTouchListener() {

            //设置两个点 按下坐标(downx, downy)和抬起坐标(upx, upy)
            float downx = 0;
            float downy = 0;
            float upx = 0;
            float upy = 0;

            //触摸事件
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView view = (ImageView) v;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN://按下
                        downx = event.getX();
                        downy = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE://移动
                        upx = event.getX();
                        upy = event.getY();
                        canvas.drawLine(downx, downy, upx, upy, 画刷);
                        imageShow.invalidate();
                        downx = upx;
                        downy = upy;
                        break;
                    case MotionEvent.ACTION_UP://抬起
                        upx = event.getX();
                        upy = event.getY();
                        canvas.drawLine(downx, downy, upx, upy, 画刷);
                        imageShow.invalidate();//刷新
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                    default:
                        break;
                }
                return true;
            }  //end  onTouch
        });


    }

    //保存资源文件中的图片到本地相册,实时刷新
    public static String saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "DCIM");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        // 将图片保存到本地
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(file.getAbsolutePath())));
        //把file的文件路径返回出去
        return file.toString();
    }

    //    通过文件路径从本地获取图片
    private Bitmap getDiskBitmap(String pathString) {
//        申请获取存储权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        Bitmap bitmap = null;
        try {
            File file = new File(pathString);
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(pathString);
//                如果bitmap为空，弹出提示框，提示框结束后直接关闭程序
                if (bitmap == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("提示");
                    builder.setMessage("权限不够，请允许存储权限");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.show();
                }
//
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示");
                builder.setMessage("图片不存在");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }





}
