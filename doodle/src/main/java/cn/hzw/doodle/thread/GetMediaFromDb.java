package cn.hzw.doodle.thread;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

import cn.hzw.doodle.imagepicker.BeanImage;


public class GetMediaFromDb implements Runnable {

    Context context;
    GetMediaListener listener;
    private Handler handler = new Handler();

    public GetMediaFromDb(Context context, GetMediaListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public void run() {
        List<BeanImage> list = new ArrayList<BeanImage>();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.TITLE,
                        MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE, MediaStore.Images.Media.DATA}, null,
                new String[]{}, null);
        int num = cursor.getColumnCount();
        if (num < 1) {
            backInfoToView(false, null);
            return;
        }
        while (cursor.moveToNext()) {
            String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            BeanImage beanImage = new BeanImage(filePath, false);
            list.add(beanImage);
        }
        backInfoToView(true, list);
    }

    private void backInfoToView(final boolean b, final List<BeanImage> lists) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (listener == null) {
                    return;
                }
                listener.backMediaListener(b, lists);
            }
        });
    }


}
