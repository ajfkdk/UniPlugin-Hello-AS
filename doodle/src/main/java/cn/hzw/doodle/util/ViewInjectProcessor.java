package cn.hzw.doodle.util;

import android.view.View;

import java.lang.reflect.Field;


public class ViewInjectProcessor {
    public ViewInjectProcessor() {
    }

    public static void process(Object object, View view) {
        Field[] fields = object.getClass().getDeclaredFields();
        Field[] var3 = fields;
        int var4 = fields.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            Field field = var3[var5];
            ViewInject viewInject = (ViewInject) field.getAnnotation(ViewInject.class);
            if (viewInject != null && view.getId() == viewInject.id()) {
                try {
                    field.setAccessible(true);
                    field.set(object, view);
                } catch (IllegalAccessException var9) {
                    var9.printStackTrace();
                }
            }
        }

    }
}
