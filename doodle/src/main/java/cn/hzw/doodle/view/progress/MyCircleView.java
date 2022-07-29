package cn.hzw.doodle.view.progress;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.chenzhouli.doodle.R;


public class MyCircleView extends View {

    Paint mPaint;
    /**
     * 画笔的宽度
     */
    int PROGRESS_WIDTH;
    /**
     * 半径
     */
    int circleRadius;
    /**
     * 文字的描述
     */
    String textDesc;
    /**
     * 文字的大小尺寸
     */
    int TEXT_SIXE;

    int TEXT_COLOR;
    int progress = 0;
    private int PROGRESS_BG;
    private int PROGRESS_COLOR;
    boolean running = true;
    /** 开启线程绘制View */
    // MyThread myThread;
    /**
     * 画弧度递增的变量
     */
    private int sweepAngle = 0;

    public MyCircleView(Context context) {
        this(context, null);
    }

    public MyCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.MyView);
        PROGRESS_BG = ta.getColor(R.styleable.MyView_viewbg, 0xffffffff);
        PROGRESS_COLOR = ta.getColor(R.styleable.MyView_progresscolor, 0xff7fc6f8);
        TEXT_COLOR = ta.getColor(R.styleable.MyView_textcolor, 0xff9A32CD);
        TEXT_SIXE = ta.getDimensionPixelOffset(R.styleable.MyView_textsize, 30);
        PROGRESS_WIDTH = ta.getDimensionPixelOffset(R.styleable.MyView_progresswidth, 30);
        ta.recycle();
        initView();
    }

    private void initView() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        // mPaint.setDither(true);// 设置抖动,颜色过渡更均匀
        mPaint.setStrokeCap(Cap.ROUND);
    }

    public int defaultWidth = 300;

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecModel = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecModel = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpcSzie = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecModel == MeasureSpec.AT_MOST & heightSpecModel == MeasureSpec.AT_MOST) {
            widthSpecSize = defaultWidth;
            heightSpcSzie = defaultWidth;
            circleRadius = defaultWidth;
        } else if (widthSpecModel == MeasureSpec.AT_MOST) {
            widthSpecSize = defaultWidth;
            circleRadius = defaultWidth;
        } else if (heightSpecModel == MeasureSpec.AT_MOST) {
            heightSpcSzie = defaultWidth;
            circleRadius = defaultWidth;
        }
        circleRadius = widthSpecSize;
        setMeasuredDimension(widthSpecSize, heightSpcSzie);
    }

    public void setProgress(int progress) {
        this.progress = progress;
        if (running) {
            System.out.println("====打断");
            running = false;
        }
        running = true;
        invalidate();
    }

    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        /** 绘制背景 */
        drawbg(canvas);
        drawText(canvas);
        drawProgress(canvas);
        if (sweepAngle != progress) {
            sweepAngle++;
            invalidate();
        }
    }

    private void drawProgress(final Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        mPaint.setColor(PROGRESS_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(PROGRESS_WIDTH);

        int left = width / 2 - circleRadius / 2 + PROGRESS_WIDTH / 2;
        int top = height / 2 - circleRadius / 2 + PROGRESS_WIDTH / 2;
        int right = width / 2 + circleRadius / 2 - PROGRESS_WIDTH / 2;
        int bottom = height / 2 + circleRadius / 2 - PROGRESS_WIDTH / 2;

        RectF oval1 = new RectF(left, top, right, bottom);
        int arc = sweepAngle * 360 / 100;
        canvas.drawArc(oval1, 0, arc, false, mPaint);// 小弧形
    }

    private void drawText(final Canvas canvas) {
        textDesc = sweepAngle + "%";
        final int width = getWidth();
        final int height = getHeight();

        final Rect bounds = new Rect();
        mPaint.setColor(TEXT_COLOR);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(TEXT_SIXE);
        mPaint.getTextBounds(textDesc, 0, textDesc.length(), bounds);
        canvas.drawText(textDesc, (width / 2) - (bounds.width() / 2), (height / 2) + (bounds.height() / 2), mPaint);
    }

    private void drawbg(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        mPaint.setColor(PROGRESS_BG);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(PROGRESS_WIDTH);
        canvas.drawCircle(width / 2, height / 2, circleRadius / 2 - PROGRESS_WIDTH / 2, mPaint);
    }
}