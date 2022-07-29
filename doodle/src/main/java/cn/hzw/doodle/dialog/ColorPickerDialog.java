package cn.hzw.doodle.dialog;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;


import com.chenzhouli.doodle.R;

import cn.hzw.doodle.core.IDoodle;
import cn.hzw.doodle.util.DrawUtil;
import cn.hzw.doodle.util.Util;

public class ColorPickerDialog extends Dialog {

    Context context;
    private OnColorChangedListener mListener;

    /**
     * @param context
     * @param listener 回调
     */
    public ColorPickerDialog(Context context, OnColorChangedListener listener, int themeResId) {
        super(context, themeResId);
        this.context = context;
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void show(final IDoodle iDoodle, Drawable drawable, int maxSize) {
        super.show();
        int height = Util.dp2px(context, 400);
        int width = Util.dp2px(context, 400);
        final ViewGroup viewGroup = (ViewGroup) View.inflate(context, R.layout.doodle_color_selector_dialog, null);
        ViewGroup container = (ViewGroup) viewGroup.findViewById(R.id.doodle_color_selector_container);
        final ColorPickerView colorPickerView = new ColorPickerView(context, Color.BLACK, height, width, null);
        if (drawable instanceof BitmapDrawable) {
            colorPickerView.setDrawable((BitmapDrawable) drawable);
        } else if (drawable instanceof ColorDrawable) {
            colorPickerView.setColor(((ColorDrawable) drawable).getColor());
        }
        container.addView(colorPickerView, 0, new ViewGroup.LayoutParams(height, width));
        viewGroup.findViewById(R.id.dialog_enter_btn_01).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        viewGroup.findViewById(R.id.dialog_enter_btn_02).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (colorPickerView.getDrawable() != null) {
                    mListener.colorChanged(colorPickerView.getDrawable(), (int) iDoodle.getSize());
                } else {
                    mListener.colorChanged(colorPickerView.getColor(), (int) iDoodle.getSize());
                }
                dismiss();
            }
        });
        setContentView(viewGroup, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setCanceledOnTouchOutside(false);
        DrawUtil.assistActivity(getWindow());
    }

    //        View.OnClickListener listener = new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ImageView imageView = (ImageView) v;
//                colorPickerView.setDrawable((BitmapDrawable) imageView.getDrawable());
//            }
//        };
//        ViewGroup shaderContainer = (ViewGroup) viewGroup.findViewById(R.id.doodle_shader_container);
//        for (int i = 0; i < shaderContainer.getChildCount(); i++) {
//            shaderContainer.getChildAt(i).setOnClickListener(listener);
//        }


    public interface OnColorChangedListener {
        /**
         * 回调函数
         *
         * @param color 选中的颜色
         */
        void colorChanged(int color, int size);

        void colorChanged(Drawable color, int size);
    }
}  
