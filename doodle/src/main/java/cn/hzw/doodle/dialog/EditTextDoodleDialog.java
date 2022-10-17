package cn.hzw.doodle.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.DataProvider.GloalData;
import com.chenzhouli.doodle.R;
import cn.hzw.doodle.util.KeyBoardUtilDoo;

/***
 * 带一个输入框的
 */
public class EditTextDoodleDialog {
    private Activity context;
    private Dialog dialog;
    EditTextDialogListener dialogClick;
    public Button btn_modify, btn_del_all;
    public TextView dialog_title;
    EditText et_username_edit;
    public EditTextDoodleDialog(final Activity context, int width, int height) {
        this.context = context;
        dialog = new Dialog(context, R.style.MyDialog_doodle);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialog_view = View.inflate(context, R.layout.dialog_edit_commit_doodle, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        dialog.setContentView(dialog_view, params);
        btn_modify = (Button) dialog_view.findViewById(R.id.btn_modify);
        et_username_edit = (EditText) dialog_view.findViewById(R.id.et_username_edit);
        dialog_title = (TextView) dialog_view.findViewById(R.id.tv_dialog_title);
        btn_del_all = (Button) dialog_view.findViewById(R.id.btn_del_all);
        btn_del_all.setText(GloalData.getLanguage()?"Clear":"清空");
        btn_del_all.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                et_username_edit.setText("");
            }
        });

        btn_modify.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (dialogClick != null) {
                    String modifyName = et_username_edit.getText().toString().trim();
                    if (modifyName.contains(" ")) { //去掉空格
                        modifyName = modifyName.replace(" ", "");
                    }
                    dialogClick.commit(modifyName);
                }
                dissmiss();
            }
        });

        et_username_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    KeyBoardUtilDoo.showKeyBord(view);
                } else {
                    KeyBoardUtilDoo.hiddleBord(view);
                }
            }
        });
        RelativeLayout rela_bgg = (RelativeLayout) dialog_view.findViewById(R.id.rela_bgg);
        rela_bgg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyBoardUtilDoo.hiddleBord(view);
            }
        });
    }

    public void show(String title, String content, String commit) {
        dialog_title.setText(title);
        et_username_edit.setText(content);
        btn_modify.setText(commit);
        et_username_edit.setSelection(et_username_edit.getText().toString().length());
        dialog.show();
    }

    public void dissmiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void setOnDialogClickListener(EditTextDialogListener dc) {
        dialogClick = dc;
    }

    public interface EditTextDialogListener {
        void commit(String content);
    }
}
