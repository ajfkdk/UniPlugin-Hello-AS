package cn.hzw.doodle.util;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore.Images.Media;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

//import cn.forward.androids.Image.ImageCache;
//import cn.forward.androids.utils.ReflectUtil;
//import cn.forward.androids.utils.Util;

import java.io.File;
import java.io.IOException;

public class ImageUtils {
    private static final Config BITMAP_CONFIG;
    private static final int COLORDRAWABLE_DIMENSION = 2;
    private static final Uri STORAGE_URI;

    public ImageUtils() {
    }

    public static Uri addImage(ContentResolver cr, String path) {
        File file = new File(path);
        String name = file.getName();
        int i = name.lastIndexOf(".");
        String title = name.substring(0, i);
        String filename = title + name.substring(i);
        int[] degree = new int[1];
        return addImage(cr, title, System.currentTimeMillis(), (Location) null, file.getParent(), filename, degree);
    }

    private static Uri addImage(ContentResolver cr, String title, long dateTaken, Location location, String directory, String filename, int[] degree) {
        File file = new File(directory, filename);
        long size = file.length();
        ContentValues values = new ContentValues(9);
        values.put("title", title);
        values.put("_display_name", filename);
        values.put("datetaken", dateTaken);
        values.put("mime_type", "image/jpeg");
        values.put("orientation", degree[0]);
        values.put("_data", file.getAbsolutePath());
        values.put("_size", size);
        if (location != null) {
            values.put("latitude", location.getLatitude());
            values.put("longitude", location.getLongitude());
        }

        return cr.insert(STORAGE_URI, values);
    }

    public static Uri addVideo(ContentResolver cr, String title, long dateTaken, Location location, String directory, String filename) {
        String filePath = directory + "/" + filename;

        try {
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            new File(directory, filename);
        } catch (Exception var11) {
            var11.printStackTrace();
        }

        long size = (new File(directory, filename)).length();
        ContentValues values = new ContentValues(9);
        values.put("title", title);
        values.put("_display_name", filename);
        values.put("datetaken", dateTaken);
        values.put("mime_type", "video/3gpp");
        values.put("_data", filePath);
        values.put("_size", size);
        if (location != null) {
            values.put("latitude", location.getLatitude());
            values.put("longitude", location.getLongitude());
        }

        return cr.insert(STORAGE_URI, values);
    }

    public static Bitmap rotate(Bitmap bitmap, int degree, boolean isRecycle) {
        Matrix m = new Matrix();
        m.setRotate((float) degree, (float) bitmap.getWidth() / 2.0F, (float) bitmap.getHeight() / 2.0F);

        try {
            Bitmap bm1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            if (isRecycle) {
                bitmap.recycle();
            }

            return bm1;
        } catch (OutOfMemoryError var5) {
            var5.printStackTrace();
            return null;
        }
    }

    public static int getBitmapExifRotate(String path) {
        int degree = 0;
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(path);
        } catch (IOException var4) {
            var4.printStackTrace();
            return 0;
        }

        if (exif != null) {
            int ori = exif.getAttributeInt("Orientation", 0);
            switch (ori) {
                case 3:
                    degree = 180;
                    break;
                case 6:
                    degree = 90;
                    break;
                case 8:
                    degree = 270;
                    break;
                default:
                    degree = 0;
            }
        }

        return degree;
    }

    public static Bitmap rotateBitmapByExif(Bitmap bitmap, String path, boolean isRecycle) {
        int digree = getBitmapExifRotate(path);
        if (digree != 0) {
            bitmap = rotate(bitmap, digree, isRecycle);
        }

        return bitmap;
    }

    public static final Bitmap createBitmapFromPath(String path, Context context) {

        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int screenW = display.getWidth();
        int screenH = display.getHeight();
        return createBitmapFromPath(path, screenW, screenH);
    }

    public static final Bitmap createBitmapFromPath(String path, int maxWidth, int maxHeight) {
        Bitmap bitmap = null;
        Options options = null;
        if (path.endsWith(".3gp")) {
            return ThumbnailUtils.createVideoThumbnail(path, 1);
        } else {
            try {
                options = new Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, options);
                int width = options.outWidth;
                int height = options.outHeight;
                options.inSampleSize = computeBitmapSimple(width * height, maxWidth * maxHeight * 2);
                options.inPurgeable = true;
                options.inPreferredConfig = Config.RGB_565;
                options.inDither = false;
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(path, options);
                return rotateBitmapByExif(bitmap, path, true);
            } catch (OutOfMemoryError var7) {
                options.inSampleSize *= 2;
                bitmap = BitmapFactory.decodeFile(path, options);
                return rotateBitmapByExif(bitmap, path, true);
            } catch (Exception var8) {
                var8.printStackTrace();
                return null;
            }
        }
    }

    public static final Bitmap createBitmapFromPath(byte[] data, int maxWidth, int maxHeight) {
        Bitmap bitmap = null;
        Options options = null;

        try {
            options = new Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            int width = options.outWidth;
            int height = options.outHeight;
            options.inSampleSize = computeBitmapSimple(width * height, maxWidth * maxHeight * 2);
            options.inPurgeable = true;
            options.inPreferredConfig = Config.RGB_565;
            options.inDither = false;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            return bitmap;
        } catch (OutOfMemoryError var7) {
            options.inSampleSize *= 2;
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            return bitmap;
        } catch (Exception var8) {
            var8.printStackTrace();
            return null;
        }
    }

    public static int computeBitmapSimple(int realPixels, int maxPixels) {
        if (maxPixels <= 0) {
            return 1;
        } else {
            try {
                if (realPixels <= maxPixels) {
                    return 1;
                } else {
                    int scale;
                    for (scale = 2; realPixels / (scale * scale) > maxPixels; scale *= 2) {
                    }

                    return scale;
                }
            } catch (Exception var3) {
                return 1;
            }
        }
    }

    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        } else if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            try {
                Bitmap bitmap;
                if (drawable instanceof ColorDrawable) {
                    bitmap = Bitmap.createBitmap(2, 2, BITMAP_CONFIG);
                } else {
                    bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
                }

                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                return bitmap;
            } catch (Exception var3) {
                var3.printStackTrace();
                return null;
            }
        }
    }

    public static int[] optimizeMaxSizeByView(View view, int maxImageWidth, int maxImageHeight) {
        int width = maxImageWidth;
        int height = maxImageHeight;
        return new int[]{width, height};
    }

//    public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind, ImageCache imageCache) {
//        Bitmap bitmap = null;
//        String key = videoPath + width + height;
//        if (imageCache != null) {
//            bitmap = imageCache.getBitmap(key);
//            if (bitmap != null) {
//                return bitmap;
//            }
//        }
//
//        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
//        if (bitmap == null) {
//            return null;
//        } else {
//            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, 2);
//            if (imageCache != null && bitmap != null) {
//                imageCache.save(bitmap, key, CompressFormat.JPEG);
//            }
//        return bitmap;
//        }
//    }

    static {
        BITMAP_CONFIG = Config.ARGB_8888;
        STORAGE_URI = Media.EXTERNAL_CONTENT_URI;
    }
}
