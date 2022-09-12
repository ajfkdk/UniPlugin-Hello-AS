package cn.hzw.doodle;

import static com.kevin.UCrop.REQUEST_CROP;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chenzhouli.ExeceptionTool.ResolveExeception;
import com.chenzhouli.doodle.R;
import com.kevin.UCrop;
import com.kevin.UCropActivity;
import com.kevin.crop.util.UriUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hzw.doodle.core.IDoodle;
import cn.hzw.doodle.core.IDoodleColor;
import cn.hzw.doodle.core.IDoodleItemListener;
import cn.hzw.doodle.core.IDoodlePen;
import cn.hzw.doodle.core.IDoodleSelectableItem;
import cn.hzw.doodle.core.IDoodleShape;
import cn.hzw.doodle.core.IDoodleTouchDetector;
import cn.hzw.doodle.dialog.ColorPickerDialog;
import cn.hzw.doodle.dialog.DialogController;
import cn.hzw.doodle.dialog.EditTextDoodleDialog;
import cn.hzw.doodle.dialog.OridinryDooDleDialog;
import cn.hzw.doodle.imagepicker.ImageSelectorView;
import cn.hzw.doodle.util.ImageUtils;
import cn.hzw.doodle.util.LogUtil;
import cn.hzw.doodle.util.StatusBarUtil;
import cn.hzw.doodle.util.Util;

public class DoodleActivity extends DoolBaseActivity {
    public static final String TAG = "Doodle";
    public final static int DEFAULT_MOSAIC_SIZE = 20; // 默认马赛克大小
    public final static int DEFAULT_COPY_SIZE = 20; // 默认仿制大小
    public final static int DEFAULT_TEXT_SIZE = 18; // 默认文字大小
    public final static int DEFAULT_BITMAP_SIZE = 80; // 默认贴图大小

    public static final int RESULT_ERROR = -111; // 出现错误
    private Button btn_reduce;
    private Button btn_add;

    /**
     * 启动涂鸦界面
     *
     * @param activity
     * @param params      涂鸦参数
     * @param requestCode startActivityForResult的请求码
     * @see DoodleParams
     */
    public static void startActivityForResult(Activity activity, DoodleParams params, int requestCode) {
        Intent intent = new Intent(activity, DoodleActivity.class);
        intent.putExtra(DoodleActivity.KEY_PARAMS, params);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 仅仅是启动activity
     * @param activity
     * @param params
     */
    public static void startActivityOnly(Activity activity, DoodleParams params) {
        Intent intent = new Intent(activity, DoodleActivity.class);
        intent.putExtra(DoodleActivity.KEY_PARAMS, params);
        activity.startActivity(intent);
    }

    /**
     * 启动涂鸦界面
     *
     * @param activity
     * @param imagePath   　图片路径
     * @param savePath    　保存路径
     * @param isDir       　保存路径是否为目录
     * @param requestCode 　startActivityForResult的请求码
     */
    @Deprecated
    public static void startActivityForResult(Activity activity, String imagePath, String savePath, boolean isDir, int requestCode) {
        DoodleParams params = new DoodleParams();
        params.mImagePath = imagePath;
        params.mSavePath = savePath;
        params.mSavePathIsDir = isDir;
        startActivityForResult(activity, params, requestCode);
    }

    @Deprecated
    public static void startActivityForResult(Activity activity, String imagePath, int requestCode) {
        DoodleParams params = new DoodleParams();
        params.mImagePath = imagePath;
        startActivityForResult(activity, params, requestCode);
    }

    public static final String KEY_PARAMS = "key_doodle_params";
    public static final String KEY_IMAGE_PATH = "key_image_path";

    private String mImagePath;

    private FrameLayout mFrameLayout;
    private IDoodle mDoodle;
    private DoodleView mDoodleView;


    private View mSettingsPanel;
    private View mSelectedEditContainer;
    private TextView mItemScaleTextView;
    private View mBtnColor, mColorContainer;
    private SeekBar mEditSizeSeekBar;
    private View mShapeContainer, mPenContainer, mSizeContainer;
    private View mBtnUndo;
    private View mMosaicMenu;
    private View mEditBtn;

    private AlphaAnimation mViewShowAnimation, mViewHideAnimation; // view隐藏和显示时用到的渐变动画
    //参数
    private DoodleParams mDoodleParams;

    // 触摸屏幕超过一定时间才判断为需要隐藏设置面板
    private Runnable mHideDelayRunnable;
    // 触摸屏幕超过一定时间才判断为需要显示设置面板
    private Runnable mShowDelayRunnable;

    private DoodleOnTouchGestureListener mTouchGestureListener;
    private Map<IDoodlePen, Float> mPenSizeMap = new HashMap<>(); //保存每个画笔对应的最新大小

    private int mMosaicLevel = -1;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_PARAMS, mDoodleParams);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        mDoodleParams = savedInstanceState.getParcelable(KEY_PARAMS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initImageLoader(DoodleActivity.this);
        StatusBarUtil.setStatusBarTranslucent(this, true, false);
        if (mDoodleParams == null) {
            mDoodleParams = getIntent().getExtras().getParcelable(KEY_PARAMS);
        }
        if (mDoodleParams == null) {
            LogUtil.e("TAG", "mDoodleParams is null!");
            this.finish();
            return;
        }

        mImagePath = mDoodleParams.mImagePath;
        if (mImagePath == null) {
            LogUtil.e("TAG", "mImagePath is null!");
            this.finish();
            return;
        }

        LogUtil.d("TAG", mImagePath);
        if (mDoodleParams.mIsFullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        Bitmap bitmap = ImageUtils.createBitmapFromPath(mImagePath, this);
        if (bitmap == null) {
            LogUtil.e("TAG", "bitmap is null!");
            this.finish();
            return;
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.doodle_layout);
        mFrameLayout = (FrameLayout) findViewById(R.id.doodle_container);
        TextView buttonBack = findViewById(R.id.doodle_selectable_bottom);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoodleView.setEditMode(false);
            }
        });
        mDoodle = mDoodleView = new DoodleViewWrapper(this, bitmap, mDoodleParams.mOptimizeDrawing, new IDoodleListener() {
            @Override
            public void onSaved(IDoodle doodle, Bitmap bitmap, Runnable callback) throws FileNotFoundException { // 保存图片为jpg格式
                File doodleFile = null;
                File file = null;
                String savePath = mDoodleParams.mSavePath;
                boolean isDir = mDoodleParams.mSavePathIsDir;
                if (TextUtils.isEmpty(savePath)) {
                    File dcimFile = new File(Environment.getExternalStorageDirectory(), "DCIM");
                    doodleFile = new File(dcimFile, "Doodle");
                    //　保存的路径
                    file = new File(doodleFile, System.currentTimeMillis() + ".jpg");
                } else {
                    if (isDir) {
                        doodleFile = new File(savePath);
                        //　保存的路径
                        file = new File(doodleFile, System.currentTimeMillis() + ".jpg");
                    } else {
                        file = new File(savePath);
                        doodleFile = file.getParentFile();
                    }
                }
                doodleFile.mkdirs();

                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
//                    ImageUtils.addImage(getContentResolver(), file.getAbsolutePath());
                    Intent intent = new Intent();
                    intent.putExtra(KEY_IMAGE_PATH, file.getAbsolutePath());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    onError(DoodleView.ERROR_SAVE, e.getMessage());
//                   toast提示----------------滞留日志信息到本地----------------
                    Toast.makeText(DoodleActivity.this, e.toString(), Toast.LENGTH_LONG).show();
//滞留日志信息到本地
                    ResolveExeception.dumpExceptionToFile(e);
                    throw e;
                } finally {
                    Util.closeQuietly(outputStream);
                }
            }

            public void onError(int i, String msg) {
                setResult(RESULT_ERROR);
                finish();
            }

            @Override
            public void onReady(IDoodle doodle) {
                mEditSizeSeekBar.setMax(Math.min(mDoodleView.getWidth(), mDoodleView.getHeight()));
                float size = mDoodleParams.mPaintUnitSize > 0 ? mDoodleParams.mPaintUnitSize * mDoodle.getUnitSize() : 0;
                if (size <= 0) {
                    size = mDoodleParams.mPaintPixelSize > 0 ? mDoodleParams.mPaintPixelSize : mDoodle.getSize();
                }
                // 设置初始值
                mDoodle.setSize(size);
                // 选择画笔
                mDoodle.setPen(DoodlePen.BRUSH);
                mDoodle.setShape(DoodleShape.HAND_WRITE);
                mDoodle.setColor(new DoodleColor(mDoodleParams.mPaintColor));
                if (mDoodleParams.mZoomerScale <= 0) {
                    findViewById(R.id.btn_zoomer).setVisibility(View.GONE);
                }
                mDoodle.setZoomerScale(mDoodleParams.mZoomerScale);
                mTouchGestureListener.setSupportScaleItem(mDoodleParams.mSupportScaleItem);

                // 每个画笔的初始值
                mPenSizeMap.put(DoodlePen.BRUSH, mDoodle.getSize());
                mPenSizeMap.put(DoodlePen.MOSAIC, DEFAULT_MOSAIC_SIZE * mDoodle.getUnitSize());
                mPenSizeMap.put(DoodlePen.COPY, DEFAULT_COPY_SIZE * mDoodle.getUnitSize());
                mPenSizeMap.put(DoodlePen.ERASER, mDoodle.getSize());
                mPenSizeMap.put(DoodlePen.TEXT, DEFAULT_TEXT_SIZE * mDoodle.getUnitSize());
                mPenSizeMap.put(DoodlePen.BITMAP, DEFAULT_BITMAP_SIZE * mDoodle.getUnitSize());
            }
        }, null);

        mTouchGestureListener = new DoodleOnTouchGestureListener(mDoodleView, new DoodleOnTouchGestureListener.ISelectionListener() {
            // save states before being selected
            IDoodlePen mLastPen = null;
            IDoodleColor mLastColor = null;
            Float mSize = null;

            IDoodleItemListener mIDoodleItemListener = new IDoodleItemListener() {
                @Override
                public void onPropertyChanged(int property) {
                    if (mTouchGestureListener.getSelectedItem() == null) {
                        return;
                    }
                    if (property == IDoodleItemListener.PROPERTY_SCALE) {
                        mItemScaleTextView.setText(
                                (int) (mTouchGestureListener.getSelectedItem().getScale() * 100 + 0.5f) + "%");
                    }
                }
            };

            @Override
            public void onSelectedItem(IDoodle doodle, IDoodleSelectableItem selectableItem, boolean selected) {
                if (selected) {
                    if (mLastPen == null) {
                        mLastPen = mDoodle.getPen();
                    }
                    if (mLastColor == null) {
                        mLastColor = mDoodle.getColor();
                    }
                    if (mSize == null) {
                        mSize = mDoodle.getSize();
                    }
                    mDoodleView.setEditMode(true);
                    mDoodle.setPen(selectableItem.getPen());
                    mDoodle.setColor(selectableItem.getColor());
                    mDoodle.setSize(selectableItem.getSize());
                    mEditSizeSeekBar.setProgress((int) selectableItem.getSize());
                    mSelectedEditContainer.setVisibility(View.VISIBLE);
                    mSizeContainer.setVisibility(View.VISIBLE);
                    mItemScaleTextView.setText((int) (selectableItem.getScale() * 100 + 0.5f) + "%");
                    selectableItem.addItemListener(mIDoodleItemListener);
                } else {
                    selectableItem.removeItemListener(mIDoodleItemListener);

                    if (mTouchGestureListener.getSelectedItem() == null) { // nothing is selected. 当前没有选中任何一个item
                        if (mLastPen != null) {
                            mDoodle.setPen(mLastPen);
                            mLastPen = null;
                        }
                        if (mLastColor != null) {
                            mDoodle.setColor(mLastColor);
                            mLastColor = null;
                        }
                        if (mSize != null) {
                            mDoodle.setSize(mSize);
                            mSize = null;
                        }
                        mSelectedEditContainer.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCreateSelectableItem(IDoodle doodle, float x, float y) {
                if (mDoodle.getPen() == DoodlePen.TEXT) {
                    createDoodleText(null, x, y);
                } else if (mDoodle.getPen() == DoodlePen.BITMAP) {
                    createDoodleBitmap(null, x, y);
                }
            }
        }) {
            @Override
            public void setSupportScaleItem(boolean supportScaleItem) {
                super.setSupportScaleItem(supportScaleItem);
                if (supportScaleItem) {
                    mItemScaleTextView.setVisibility(View.VISIBLE);
                } else {
                    mItemScaleTextView.setVisibility(View.GONE);
                }
            }
        };
        IDoodleTouchDetector detector = new DoodleTouchDetector(getApplicationContext(), mTouchGestureListener);
        mDoodleView.setDefaultTouchDetector(detector);

        mDoodle.setIsDrawableOutside(mDoodleParams.mIsDrawableOutside);
        mFrameLayout.addView(mDoodleView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mDoodle.setDoodleMinScale(mDoodleParams.mMinScale);
        mDoodle.setDoodleMaxScale(mDoodleParams.mMaxScale);
        initView();
        initListener();
    }

    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app
        ImageLoader.getInstance().init(config.build());
    }

    private boolean canChangeColor(IDoodlePen pen) {
        return pen != DoodlePen.ERASER
                && pen != DoodlePen.BITMAP
                && pen != DoodlePen.COPY
                && pen != DoodlePen.MOSAIC;
    }

    // 添加文字
    private void createDoodleText(final DoodleText doodleText, final float x, final float y) {
        if (isFinishing()) {
            return;
        }
        EditTextDoodleDialog editTextDoodleDialog = new EditTextDoodleDialog(DoodleActivity.this, setScreenWidth, setScreenHeight);
        editTextDoodleDialog.setOnDialogClickListener(new EditTextDoodleDialog.EditTextDialogListener() {
            @Override
            public void commit(String content) {
                String text = content;
                if (TextUtils.isEmpty(text)) {
                    return;
                }
                if (doodleText == null) {
                    IDoodleSelectableItem item = new DoodleText(mDoodle, text, mDoodle.getSize(), mDoodle.getColor().copy(), x, y);
                    mDoodle.addItem(item);
                    mTouchGestureListener.setSelectedItem(item);
                } else {
                    doodleText.setText(text);
                }
                mDoodle.refresh();
            }
        });
        String desc = doodleText == null ? null : doodleText.getText();
        editTextDoodleDialog.show("请输入文本", desc, "提交");
        if (doodleText == null) {
            mSettingsPanel.removeCallbacks(mHideDelayRunnable);
        }
    }

    // 添加贴图
    private void createDoodleBitmap(final DoodleBitmap doodleBitmap, final float x, final float y) {
        DialogController.showSelectImageDialog(this, 8, new ImageSelectorView.ImageSelectorListener() {
            @Override
            public void onCancel() {
            }

            @Override
            public void onEnter(List<String> pathList) {
                Bitmap bitmap = ImageUtils.createBitmapFromPath(pathList.get(0), mDoodleView.getWidth() / 4, mDoodleView.getHeight() / 4);

                if (doodleBitmap == null) {
                    IDoodleSelectableItem item = new DoodleBitmap(mDoodle, bitmap, mDoodle.getSize(), x, y);
                    mDoodle.addItem(item);
                    mTouchGestureListener.setSelectedItem(item);
                } else {
                    doodleBitmap.setBitmap(bitmap);
                }
                mDoodle.refresh();
            }
        });
    }


    private void initView() {
        mBtnUndo = findViewById(R.id.btn_undo);

        mSelectedEditContainer = findViewById(R.id.doodle_selectable_edit_container);
        mSelectedEditContainer.setVisibility(View.GONE);
        mItemScaleTextView = (TextView) findViewById(R.id.item_scale);
        mItemScaleTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mTouchGestureListener.getSelectedItem() != null) {
                    mTouchGestureListener.getSelectedItem().setScale(1);
                }
                return true;
            }
        });

        mSettingsPanel = findViewById(R.id.doodle_panel);
        mShapeContainer = findViewById(R.id.shape_container);
        mPenContainer = findViewById(R.id.pen_container);
        mSizeContainer = findViewById(R.id.size_container);
        mMosaicMenu = findViewById(R.id.mosaic_menu);
        mEditBtn = findViewById(R.id.doodle_selectable_edit);
        mBtnColor = findViewById(R.id.btn_set_color);
        mColorContainer = findViewById(R.id.btn_set_color_container);

        btn_reduce = (Button) findViewById(R.id.btn_reduce);
        btn_add = (Button) findViewById(R.id.btn_add);
        btn_reduce.setOnClickListener(clickListener);
        btn_add.setOnClickListener(clickListener);

        mEditSizeSeekBar = (SeekBar) findViewById(R.id.doodle_seekbar_size);
        mViewShowAnimation = new AlphaAnimation(0, 1);
        mViewShowAnimation.setDuration(150);
        mViewHideAnimation = new AlphaAnimation(1, 0);
        mViewHideAnimation.setDuration(150);
        mHideDelayRunnable = new Runnable() {
            public void run() {
                hideView(mSettingsPanel);
            }

        };
        mShowDelayRunnable = new Runnable() {
            public void run() {
                showView(mSettingsPanel);
            }
        };
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.btn_add) {
                int progress = mEditSizeSeekBar.getProgress();
                progress++;
                mEditSizeSeekBar.setProgress(progress);
            } else if (view.getId() == R.id.btn_reduce) {
                int progress = mEditSizeSeekBar.getProgress();
                if (progress < 1) {
                    return;
                }
                progress--;
                mEditSizeSeekBar.setProgress(progress);
            }
        }
    };


    private void initListener() {
        mEditSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress <= 0) {
                    mEditSizeSeekBar.setProgress(1);
                    return;
                }
                if ((int) mDoodle.getSize() == progress) {
                    return;
                }
                mDoodle.setSize(progress);
                if (mTouchGestureListener.getSelectedItem() != null) {
                    mTouchGestureListener.getSelectedItem().setSize(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mBtnUndo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!(DoodleParams.getDialogInterceptor() != null
                        && DoodleParams.getDialogInterceptor().onShow(DoodleActivity.this, mDoodle, DoodleParams.DialogType.CLEAR_ALL))) {


                    OridinryDooDleDialog oridinryDooDleDialog =
                            new OridinryDooDleDialog(DoodleActivity.this, setScreenWidth, setScreenHeight);
                    oridinryDooDleDialog.setOnDialogClickListener(new OridinryDooDleDialog.OridinryDialogClick() {
                        @Override
                        public void sure() {
                            mDoodle.clear();
                        }

                        @Override
                        public void noSure() {

                        }
                    });
                    oridinryDooDleDialog.show("是否清除当前画布?", "确定", "取消");
                }
                return true;
            }
        });

        mDoodleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 隐藏设置面板
                if (mDoodleParams.mChangePanelVisibilityDelay > 0) {
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_DOWN:
                            mSettingsPanel.removeCallbacks(mHideDelayRunnable);
                            mSettingsPanel.removeCallbacks(mShowDelayRunnable);
                            //触摸屏幕超过一定时间才判断为需要隐藏设置面板
                            mSettingsPanel.postDelayed(mHideDelayRunnable, mDoodleParams.mChangePanelVisibilityDelay);
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            mSettingsPanel.removeCallbacks(mHideDelayRunnable);
                            mSettingsPanel.removeCallbacks(mShowDelayRunnable);
                            //离开屏幕超过一定时间才判断为需要显示设置面板
                            mSettingsPanel.postDelayed(mShowDelayRunnable, mDoodleParams.mChangePanelVisibilityDelay);
                            break;
                    }
                }
                return false;
            }
        });
    }


    private ValueAnimator mRotateAnimator;

    public void onClick(final View v) throws FileNotFoundException {
        if (v.getId() == R.id.btn_pen_hand) {
            mDoodle.setPen(DoodlePen.BRUSH);
        } else if (v.getId() == R.id.btn_pen_mosaic) {
            mDoodle.setPen(DoodlePen.MOSAIC);
        } else if (v.getId() == R.id.btn_pen_copy) {
            mDoodle.setPen(DoodlePen.COPY);
        } else if (v.getId() == R.id.btn_pen_eraser) {
            mDoodle.setPen(DoodlePen.ERASER);
        } else if (v.getId() == R.id.btn_pen_text) {
            mDoodle.setPen(DoodlePen.TEXT);
        } else if (v.getId() == R.id.btn_pen_bitmap) {
            mDoodle.setPen(DoodlePen.BITMAP);
        } else if (v.getId() == R.id.doodle_btn_brush_edit) {
            mDoodleView.setEditMode(!mDoodleView.isEditMode());
        } else if (v.getId() == R.id.btn_undo) {
            mDoodle.undo();
        } else if (v.getId() == R.id.btn_zoomer) {
            mDoodleView.enableZoomer(!mDoodleView.isEnableZoomer());
        } else if (v.getId() == R.id.btn_set_color_container) {
            showColorFromPaint();
        } else if (v.getId() == R.id.doodle_btn_finish) {
            mDoodle.save();
        } else if (v.getId() == R.id.doodle_btn_back) {
            if (mDoodle.getAllItem() == null || mDoodle.getItemCount() == 0) {
                finish();
                return;
            }
            if (!(DoodleParams.getDialogInterceptor() != null
                    && DoodleParams.getDialogInterceptor().onShow(DoodleActivity.this,
                    mDoodle, DoodleParams.DialogType.SAVE))) {
                OridinryDooDleDialog oridinryDooDleDialog =
                        new OridinryDooDleDialog(DoodleActivity.this, setScreenWidth, setScreenHeight);
                oridinryDooDleDialog.setOnDialogClickListener(new OridinryDooDleDialog.OridinryDialogClick() {
                    @Override
                    public void sure() {
                        mDoodle.save();
                    }

                    @Override
                    public void noSure() {
                        finish();
                    }
                });
                oridinryDooDleDialog.show("是否保存当前画布?", "确定", "取消");
            }
        } else if (v.getId() == R.id.doodle_btn_rotate) {
            // 旋转图片
            if (mRotateAnimator == null) {
                mRotateAnimator = new ValueAnimator();
                mRotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        mDoodle.setDoodleRotation(value);
                    }
                });
                mRotateAnimator.setDuration(250);
            }
            if (mRotateAnimator.isRunning()) {
                return;
            }
            mRotateAnimator.setIntValues(mDoodle.getDoodleRotation(), mDoodle.getDoodleRotation() + 90);
            mRotateAnimator.start();
        } else if (v.getId() == R.id.doodle_selectable_edit) {
            if (mTouchGestureListener.getSelectedItem() instanceof DoodleText) {
                createDoodleText((DoodleText) mTouchGestureListener.getSelectedItem(), -1, -1);
            } else if (mTouchGestureListener.getSelectedItem() instanceof DoodleBitmap) {
                createDoodleBitmap((DoodleBitmap) mTouchGestureListener.getSelectedItem(), -1, -1);
            }
        } else if (v.getId() == R.id.doodle_selectable_remove) {
            mDoodle.removeItem(mTouchGestureListener.getSelectedItem());
            mTouchGestureListener.setSelectedItem(null);
        } else if (v.getId() == R.id.doodle_selectable_top) {
            mDoodle.topItem(mTouchGestureListener.getSelectedItem());
        } else if (v.getId() == R.id.doodle_selectable_bottom) {
            mDoodle.bottomItem(mTouchGestureListener.getSelectedItem());
        } else if (v.getId() == R.id.btn_hand_write) {
            mDoodle.setShape(DoodleShape.HAND_WRITE);
        } else if (v.getId() == R.id.btn_arrow) {
            mDoodle.setShape(DoodleShape.ARROW);
        } else if (v.getId() == R.id.btn_line) {
            mDoodle.setShape(DoodleShape.LINE);
        } else if (v.getId() == R.id.btn_holl_circle) {
            mDoodle.setShape(DoodleShape.HOLLOW_CIRCLE);
        } else if (v.getId() == R.id.btn_fill_circle) {
            mDoodle.setShape(DoodleShape.FILL_CIRCLE);
        } else if (v.getId() == R.id.btn_holl_rect) {
            mDoodle.setShape(DoodleShape.HOLLOW_RECT);
        } else if (v.getId() == R.id.btn_fill_rect) {
            mDoodle.setShape(DoodleShape.FILL_RECT);
        } else if (v.getId() == R.id.btn_mosaic_level1) {
            if (v.isSelected()) {
                return;
            }

            mMosaicLevel = DoodlePath.MOSAIC_LEVEL_1;
            mDoodle.setColor(DoodlePath.getMosaicColor(mDoodle, mMosaicLevel));
            v.setSelected(true);
            mMosaicMenu.findViewById(R.id.btn_mosaic_level2).setSelected(false);
            mMosaicMenu.findViewById(R.id.btn_mosaic_level3).setSelected(false);
            if (mTouchGestureListener.getSelectedItem() != null) {
                mTouchGestureListener.getSelectedItem().setColor(mDoodle.getColor().copy());
            }
        } else if (v.getId() == R.id.btn_mosaic_level2) {
            if (v.isSelected()) {
                return;
            }

            mMosaicLevel = DoodlePath.MOSAIC_LEVEL_2;
            mDoodle.setColor(DoodlePath.getMosaicColor(mDoodle, mMosaicLevel));
            v.setSelected(true);
            mMosaicMenu.findViewById(R.id.btn_mosaic_level1).setSelected(false);
            mMosaicMenu.findViewById(R.id.btn_mosaic_level3).setSelected(false);
            if (mTouchGestureListener.getSelectedItem() != null) {
                mTouchGestureListener.getSelectedItem().setColor(mDoodle.getColor().copy());
            }
        } else if (v.getId() == R.id.btn_mosaic_level3) {
            if (v.isSelected()) {
                return;
            }

            mMosaicLevel = DoodlePath.MOSAIC_LEVEL_3;
            mDoodle.setColor(DoodlePath.getMosaicColor(mDoodle, mMosaicLevel));
            v.setSelected(true);
            mMosaicMenu.findViewById(R.id.btn_mosaic_level1).setSelected(false);
            mMosaicMenu.findViewById(R.id.btn_mosaic_level2).setSelected(false);
            if (mTouchGestureListener.getSelectedItem() != null) {
                mTouchGestureListener.getSelectedItem().setColor(mDoodle.getColor().copy());
            }
        } else if (v.getId() == R.id.doodle_btn_crop) {
//            裁剪功能的实现
            Bitmap bitmap = mDoodle.saveAfterCrop();
            mySave(bitmap);
        }
//        隐藏按钮功能
//        else if (v.getId() == R.id.doodle_btn_hide_panel) {
//            mSettingsPanel.removeCallbacks(mHideDelayRunnable);
//            mSettingsPanel.removeCallbacks(mShowDelayRunnable);
//            v.setSelected(!v.isSelected());
//            if (!mBtnHidePanel.isSelected()) {
//                showView(mSettingsPanel);
//            } else {
//                hideView(mSettingsPanel);
//            }
//        }
    }

    //    自定义保存图片
    public void mySave(Bitmap bitmap) throws FileNotFoundException {
        File doodleFile = null;
        File file = null;
        String savePath = mDoodleParams.mSavePath;
        boolean isDir = mDoodleParams.mSavePathIsDir;
        if (TextUtils.isEmpty(savePath)) {
            File dcimFile = new File(Environment.getExternalStorageDirectory(), "DCIM");
            doodleFile = new File(dcimFile, "Doodle");
            //　保存的路径
            file = new File(doodleFile, System.currentTimeMillis() + ".jpg");
        } else {
            if (isDir) {
                doodleFile = new File(savePath);
                //　保存的路径
                file = new File(doodleFile, System.currentTimeMillis() + ".jpg");
            } else {
                file = new File(savePath);
                doodleFile = file.getParentFile();
            }
        }
        doodleFile.mkdirs();

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream);
            ImageUtils.addImage(getContentResolver(), file.getAbsolutePath());
            Uri startUri = UriUtil.getUriFromPath(mDoodleParams.mSavePath);
            if (startUri != null) {
//                    关键入口，调用裁剪功能
                UCrop uCrop = UCrop.of(startUri, startUri);
//                UCrop uCrop = UCrop.of(startUri,null,filePath);
                uCrop.withTargetActivity(UCropActivity.class);
                uCrop.start(this, REQUEST_CROP);
            } else {
                Toast.makeText(this, R.string.toast_cannot_retrieve_selected_image, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            Util.closeQuietly(outputStream);
        }
    }
    /**
     * 选择画笔的颜色
     */
    private void showColorFromPaint() {
        DoodleColor color = null;
        if (mDoodle.getColor() instanceof DoodleColor) {
            color = (DoodleColor) mDoodle.getColor();
        }
        if (color == null) {
            return;
        }
        if (!(DoodleParams.getDialogInterceptor() != null
                && DoodleParams.getDialogInterceptor().onShow(DoodleActivity.this, mDoodle, DoodleParams.DialogType.COLOR_PICKER))) {
            boolean fullScreen = (getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
            int themeId;
            if (fullScreen) {
                themeId = android.R.style.Theme_Translucent_NoTitleBar_Fullscreen;
            } else {
                themeId = android.R.style.Theme_Translucent_NoTitleBar;
            }
            new ColorPickerDialog(DoodleActivity.this,
                    new ColorPickerDialog.OnColorChangedListener() {
                        public void colorChanged(int color, int size) {
                            mDoodle.setColor(new DoodleColor(color));
                            mDoodle.setSize(size);
                        }

                        @Override
                        public void colorChanged(Drawable color, int size) {
                            Bitmap bitmap = ImageUtils.getBitmapFromDrawable(color);
                            mDoodle.setColor(new DoodleColor(bitmap));
                            mDoodle.setSize(size);
                        }
                    }, themeId).show(mDoodleView, mBtnColor.getBackground(), Math.min(mDoodleView.getWidth(), mDoodleView.getHeight()));
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDoodleView.isEditMode()) {
                mDoodleView.setEditMode(false);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() { // 返回键监听
        findViewById(R.id.doodle_btn_back).performClick();
    }

    private void showView(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }

        view.clearAnimation();
        view.startAnimation(mViewShowAnimation);
        view.setVisibility(View.VISIBLE);
    }

    private void hideView(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            return;
        }
        view.clearAnimation();
        view.startAnimation(mViewHideAnimation);
        view.setVisibility(View.GONE);
    }

    /**
     * 包裹DoodleView，监听相应的设置接口，以改变UI状态
     */
    private class DoodleViewWrapper extends DoodleView {

        public DoodleViewWrapper(Context context, Bitmap bitmap, boolean optimizeDrawing, IDoodleListener listener, IDoodleTouchDetector defaultDetector) {
            super(context, bitmap, optimizeDrawing, listener, defaultDetector);

        }

        private Map<IDoodlePen, Integer> mBtnPenIds = new HashMap<>();

        {
            mBtnPenIds.put(DoodlePen.BRUSH, R.id.btn_pen_hand);
            mBtnPenIds.put(DoodlePen.MOSAIC, R.id.btn_pen_mosaic);
            mBtnPenIds.put(DoodlePen.COPY, R.id.btn_pen_copy);
            mBtnPenIds.put(DoodlePen.ERASER, R.id.btn_pen_eraser);
            mBtnPenIds.put(DoodlePen.TEXT, R.id.btn_pen_text);
            mBtnPenIds.put(DoodlePen.BITMAP, R.id.btn_pen_bitmap);
        }

        @Override
        public void setPen(IDoodlePen pen) {
            IDoodlePen oldPen = getPen();
            super.setPen(pen);

            mMosaicMenu.setVisibility(GONE);
            mEditBtn.setVisibility(View.GONE); // edit btn
            if (pen == DoodlePen.BITMAP || pen == DoodlePen.TEXT) {
                mEditBtn.setVisibility(View.VISIBLE); // edit btn
                mShapeContainer.setVisibility(GONE);
                if (pen == DoodlePen.BITMAP) {
                    mColorContainer.setVisibility(GONE);
                } else {
                    mColorContainer.setVisibility(VISIBLE);
                }
            } else if (pen == DoodlePen.MOSAIC) {
                mMosaicMenu.setVisibility(VISIBLE);
                mShapeContainer.setVisibility(VISIBLE);
                mColorContainer.setVisibility(GONE);
            } else {
                mShapeContainer.setVisibility(VISIBLE);
                if (pen == DoodlePen.COPY || pen == DoodlePen.ERASER) {
                    mColorContainer.setVisibility(GONE);
                } else {
                    mColorContainer.setVisibility(VISIBLE);
                }
            }
            setSingleSelected(mBtnPenIds.values(), mBtnPenIds.get(pen));

            if (mTouchGestureListener.getSelectedItem() == null) {
                mPenSizeMap.put(oldPen, getSize()); // save
                Float size = mPenSizeMap.get(pen); // restore
                if (size != null) {
                    mDoodle.setSize(size);
                }
                if (isEditMode()) {
                    mShapeContainer.setVisibility(GONE);
                    mColorContainer.setVisibility(GONE);
                    mMosaicMenu.setVisibility(GONE);
                }
            } else {
                mShapeContainer.setVisibility(GONE);
                return;
            }

            if (pen == DoodlePen.BRUSH) {
                Drawable colorBg = mBtnColor.getBackground();
                if (colorBg instanceof ColorDrawable) {
                    mDoodle.setColor(new DoodleColor(((ColorDrawable) colorBg).getColor()));
                } else {
                    mDoodle.setColor(new DoodleColor(((BitmapDrawable) colorBg).getBitmap()));
                }
            } else if (pen == DoodlePen.MOSAIC) {
                if (mMosaicLevel <= 0) {
                    mMosaicMenu.findViewById(R.id.btn_mosaic_level2).performClick();
                } else {
                    mDoodle.setColor(DoodlePath.getMosaicColor(mDoodle, mMosaicLevel));
                }
            } else if (pen == DoodlePen.COPY) {

            } else if (pen == DoodlePen.ERASER) {

            } else if (pen == DoodlePen.TEXT) {
                Drawable colorBg = mBtnColor.getBackground();
                if (colorBg instanceof ColorDrawable) {
                    mDoodle.setColor(new DoodleColor(((ColorDrawable) colorBg).getColor()));
                } else {
                    mDoodle.setColor(new DoodleColor(((BitmapDrawable) colorBg).getBitmap()));
                }
            } else if (pen == DoodlePen.BITMAP) {
                Drawable colorBg = mBtnColor.getBackground();
                if (colorBg instanceof ColorDrawable) {
                    mDoodle.setColor(new DoodleColor(((ColorDrawable) colorBg).getColor()));
                } else {
                    mDoodle.setColor(new DoodleColor(((BitmapDrawable) colorBg).getBitmap()));
                }
            }
        }

        private Map<IDoodleShape, Integer> mBtnShapeIds = new HashMap<>();

        {
            mBtnShapeIds.put(DoodleShape.HAND_WRITE, R.id.btn_hand_write);
            mBtnShapeIds.put(DoodleShape.ARROW, R.id.btn_arrow);
            mBtnShapeIds.put(DoodleShape.LINE, R.id.btn_line);
            mBtnShapeIds.put(DoodleShape.HOLLOW_CIRCLE, R.id.btn_holl_circle);
            mBtnShapeIds.put(DoodleShape.FILL_CIRCLE, R.id.btn_fill_circle);
            mBtnShapeIds.put(DoodleShape.HOLLOW_RECT, R.id.btn_holl_rect);
            mBtnShapeIds.put(DoodleShape.FILL_RECT, R.id.btn_fill_rect);

        }

        @Override
        public void setShape(IDoodleShape shape) {
            super.setShape(shape);
            setSingleSelected(mBtnShapeIds.values(), mBtnShapeIds.get(shape));
        }

        TextView mPaintSizeView = (TextView) DoodleActivity.this.findViewById(R.id.paint_size_text);

        @Override
        public void setSize(float paintSize) {
            super.setSize(paintSize);
            mEditSizeSeekBar.setProgress((int) paintSize);
            mPaintSizeView.setText("" + (int) paintSize);

            if (mTouchGestureListener.getSelectedItem() != null) {
                mTouchGestureListener.getSelectedItem().setSize(getSize());
            }
        }

        @Override
        public void setColor(IDoodleColor color) {
            IDoodlePen pen = getPen();
            super.setColor(color);

            DoodleColor doodleColor = null;
            if (color instanceof DoodleColor) {
                doodleColor = (DoodleColor) color;
            }
            if (doodleColor != null
                    && canChangeColor(pen)) {
                if (doodleColor.getType() == DoodleColor.Type.COLOR) {
                    mBtnColor.setBackgroundColor(doodleColor.getColor());
                } else if (doodleColor.getType() == DoodleColor.Type.BITMAP) {
                    mBtnColor.setBackgroundDrawable(new BitmapDrawable(doodleColor.getBitmap()));
                }

                if (mTouchGestureListener.getSelectedItem() != null) {
                    mTouchGestureListener.getSelectedItem().setColor(getColor().copy());
                }
            }

            if (doodleColor != null && pen == DoodlePen.MOSAIC
                    && doodleColor.getLevel() != mMosaicLevel) {
                switch (doodleColor.getLevel()) {
                    case DoodlePath.MOSAIC_LEVEL_1:
                        DoodleActivity.this.findViewById(R.id.btn_mosaic_level1).performClick();
                        break;
                    case DoodlePath.MOSAIC_LEVEL_2:
                        DoodleActivity.this.findViewById(R.id.btn_mosaic_level2).performClick();
                        break;
                    case DoodlePath.MOSAIC_LEVEL_3:
                        DoodleActivity.this.findViewById(R.id.btn_mosaic_level3).performClick();
                        break;
                }
            }
        }

        @Override
        public void enableZoomer(boolean enable) {
            super.enableZoomer(enable);
            DoodleActivity.this.findViewById(R.id.btn_zoomer).setSelected(enable);
            if (enable) {
                Toast.makeText(DoodleActivity.this, "x" + mDoodleParams.mZoomerScale, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public boolean undo() {
            mTouchGestureListener.setSelectedItem(null);
            return super.undo();
        }

        @Override
        public void clear() {
            super.clear();
            mTouchGestureListener.setSelectedItem(null);
        }

        View mBtnEditMode = DoodleActivity.this.findViewById(R.id.doodle_btn_brush_edit);
        Boolean mLastIsDrawableOutside = null;

        @Override
        public void setEditMode(boolean editMode) {
            if (editMode == isEditMode()) {
                return;
            }

            super.setEditMode(editMode);
            mBtnEditMode.setSelected(editMode);
            if (editMode) {
                Toast.makeText(DoodleActivity.this, R.string.doodle_edit_mode, Toast.LENGTH_SHORT).show();
                mLastIsDrawableOutside = mDoodle.isDrawableOutside(); // save
                mDoodle.setIsDrawableOutside(true);
//                mPenContainer.setVisibility(GONE);
//                mShapeContainer.setVisibility(GONE);
                mSizeContainer.setVisibility(GONE);
//                mColorContainer.setVisibility(GONE);
                mBtnUndo.setVisibility(GONE);
                mMosaicMenu.setVisibility(GONE);
            } else {
                if (mLastIsDrawableOutside != null) { // restore
                    mDoodle.setIsDrawableOutside(mLastIsDrawableOutside);
                }
                mTouchGestureListener.center(); // center picture
                if (mTouchGestureListener.getSelectedItem() == null) { // restore
                    setPen(getPen());
                }
                mTouchGestureListener.setSelectedItem(null);
                mPenContainer.setVisibility(VISIBLE);
                mSizeContainer.setVisibility(VISIBLE);
                mBtnUndo.setVisibility(VISIBLE);
            }
        }

        private void setSingleSelected(Collection<Integer> ids, int selectedId) {
            for (int id : ids) {
                if (id == selectedId) {
                    DoodleActivity.this.findViewById(id).setSelected(true);
                } else {
                    DoodleActivity.this.findViewById(id).setSelected(false);
                }
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        裁剪后的回调
        if (requestCode == REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            finish();
        } else {
            final Throwable cropError = UCrop.getError(data);
        }

    }



}
