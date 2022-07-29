package cn.hzw.doodle.imagepicker;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.chenzhouli.doodle.R;

import java.util.ArrayList;
import java.util.List;


import cn.hzw.doodle.dialog.OridinryDooDleDialog;
import cn.hzw.doodle.thread.GetMediaFromDb;
import cn.hzw.doodle.thread.GetMediaListener;

/**
 * 图片选择
 * Created by huangziwei on 16-3-14.
 */
public class ImageSelectorView extends FrameLayout implements View.OnClickListener, AdapterView.OnItemClickListener {


    private GridView mGridView;
    private List<BeanImage> mPathList = new ArrayList<BeanImage>();
    private ImageSelectorAdapter mAdapter;
    private TextView mBtnEnter;
    private Context context;
    private ImageSelectorListener mSelectorListener;

    public ImageSelectorView(Context context, ImageSelectorListener listener) {
        super(context);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.doodle_layout_image_selector, null);
        addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.mSelectorListener = listener;
        this.context = context;
        initView();
        scanImageData();
    }

    private void initView() {
        mGridView = (GridView) findViewById(R.id.doodle_list_image);
        mBtnEnter = (TextView) findViewById(R.id.btn_enter);
        mBtnEnter.setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        mAdapter = new ImageSelectorAdapter(context, mPathList);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
    }

    /**
     * 列数
     *
     * @param count
     */
    public void setColumnCount(int count) {
        mGridView.setNumColumns(count);
    }

    // 扫描系统数据库中的图片
    private synchronized void scanImageData() {
        mPathList.clear();
        mAdapter.refreshPathList(mPathList);
        GetMediaFromDb runnable = new GetMediaFromDb(context, new GetMediaListener() {
            @Override
            public void backMediaListener(boolean isTrue, List<BeanImage> listsContent) {
                if (!isTrue) {
                    return;
                }
                mPathList = listsContent;
                mAdapter.refreshPathList(mPathList);
            }
        });
        Thread thread = new Thread(runnable);
        thread.start();
    }


    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.btn_back) {
//            mSelectorListener.onCancel();
//        } else if (v.getId() == R.id.btn_enter) { // 确认选择
//            if (mAdapter.getSelectedSet().size() > 0) {
//                Intent intent = new Intent();
//                ArrayList<String> list = new ArrayList<String>();
//                for (String path : mAdapter.getSelectedSet()) {
//                    list.add(path);
//                }
//                mSelectorListener.onEnter(list);
//            }
//        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Log.e("cdl", "================点击得位置==" + position);
        final BeanImage beanImage = mPathList.get(position);
        OridinryDooDleDialog oridindialog = new OridinryDooDleDialog(context, 800, 800);
        oridindialog.setOnDialogClickListener(new OridinryDooDleDialog.OridinryDialogClick() {
            @Override
            public void sure() {
                ArrayList<String> list = new ArrayList<String>();
                list.add(beanImage.getFilePath());
                mSelectorListener.onEnter(list);
            }

            @Override
            public void noSure() {

            }
        });
        oridindialog.show("确定选择当前图片？", "确定", "取消");

//        for (int i = 0; i < mPathList.size(); i++) {
//            BeanImage beanImage = mPathList.get(position);
//            boolean isChhoice = beanImage.isChhoice;
//            if (i == position) {
//                beanImage.setChhoice(!isChhoice);
//            } else {
//                beanImage.setChhoice(false);
//            }
//        }
//        mAdapter.refreshPathList(mPathList);
    }

    public interface ImageSelectorListener {
        void onCancel();

        void onEnter(List<String> pathList);
    }
}

