package cn.hzw.doodle.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.chenzhouli.doodle.R;

import java.util.List;


import cn.hzw.doodle.imagepicker.ImageSelectorView;

public class DialogController {

    public static Dialog showSelectImageDialog(Activity activity, int showNum,
                                               final ImageSelectorView.ImageSelectorListener listener) {
        final Dialog dialog = getDialog(activity);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        dialog.show();
        ViewGroup container = (ViewGroup) View.inflate(activity, R.layout.doodle_create_bitmap, null);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setContentView(container);
        ViewGroup selectorContainer = (ViewGroup) dialog.findViewById(R.id.doodle_image_selector_container);
        ImageSelectorView selectorView = new ImageSelectorView(activity, new ImageSelectorView.ImageSelectorListener() {
            @Override
            public void onCancel() {
                dialog.dismiss();
                if (listener != null) {
                    listener.onCancel();
                }
            }

            @Override
            public void onEnter(List<String> pathList) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onEnter(pathList);
                }

            }
        });
        selectorView.setColumnCount(showNum);
        selectorContainer.addView(selectorView);
        return dialog;
    }

    private static Dialog getDialog(Activity activity) {
        boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
        Dialog dialog = null;
        if (fullScreen) {
            dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        } else {
            dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);
        }
        return dialog;
    }

}
