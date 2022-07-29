package cn.hzw.doodle.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.io.Closeable;

public class Util {
    public Util() {
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int dp2px(Context context, float dp) {
        return (int) (context.getResources().getDisplayMetrics().density * dp + 0.5F);
    }

    public static void saveProperty(SharedPreferences sharedPreferences, String key, int value) {
        Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void saveProperty(SharedPreferences sharedPreferences, String key, boolean value) {
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void saveProperty(SharedPreferences sharedPreferences, String key, String value) {
        Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void clearProperties(SharedPreferences sharedPreferences) {
        Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException var2) {
                throw var2;
            } catch (Exception var3) {
            }
        }

    }
}
