package com.xyoye.dandanplay.utils.scan.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.xyoye.dandanplay.R;

/**
 * Created by xyoye on 2019/10/23.
 * 扫描窗口
 */

public final class ScanWindowView extends View {
    private static final long ANIMATION_DELAY = 20L;
    private static final int POINT_SIZE = 6;

    private static final int CORNER_RECT_WIDTH = 8;  //扫描区边角的宽
    private static final int CORNER_RECT_HEIGHT = 40; //扫描区边角的高
    private static final int SCANNER_LINE_MOVE_DISTANCE = 6;  //扫描线移动距离
    private static final int SCANNER_LINE_HEIGHT = 10;  //扫描线宽度

    private final Paint paint;

    //扫描区域边框颜色
    private final int frameColor;
    //扫描线颜色
    private final int lineColor;
    //四角颜色
    private final int rectColor;
    //是否显示边框
    private final boolean showFrame;

    private int width, height;

    public float scannerStart = 0;
    public float scannerEnd = 0;

    @SuppressLint("CustomViewStyleable")
    public ScanWindowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanWindowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ScanWindowView);
        frameColor = array.getColor(R.styleable.ScanWindowView_frame_color, getResources().getColor(R.color.scan_window_frame));
        rectColor = array.getColor(R.styleable.ScanWindowView_rect_color, getResources().getColor(R.color.scan_window_corner));
        lineColor = array.getColor(R.styleable.ScanWindowView_line_color, getResources().getColor(R.color.scan_window_laser));
        showFrame = array.getBoolean(R.styleable.ScanWindowView_show_frame, false);
        array.recycle();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        super.onLayout(changed, left, top, right, bottom);
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (scannerStart == 0 || scannerEnd == 0) {
            scannerStart = 0;
            scannerEnd = height - SCANNER_LINE_HEIGHT;
        }

        // 绘制方框
        if (showFrame) drawFrame(canvas);
        // 绘制边角
        drawCorner(canvas);
        // 绘制扫描线
        drawLaserScanner(canvas);

        //刷新扫描线位置
        postInvalidateDelayed(
                ANIMATION_DELAY,
                 POINT_SIZE,
                POINT_SIZE,
                width + POINT_SIZE,
                height + POINT_SIZE
        );
    }

    /**
     * 绘制边角
     */
    private void drawCorner(Canvas canvas) {
        paint.setColor(rectColor);
        //左上
        canvas.drawRect(0, 0, CORNER_RECT_WIDTH,  CORNER_RECT_HEIGHT, paint);
        canvas.drawRect(0, 0, CORNER_RECT_HEIGHT, CORNER_RECT_WIDTH, paint);
        //右上
        canvas.drawRect(width - CORNER_RECT_WIDTH, 0, width, CORNER_RECT_HEIGHT, paint);
        canvas.drawRect(width - CORNER_RECT_HEIGHT, 0, width, CORNER_RECT_WIDTH, paint);
        //左下
        canvas.drawRect(0, height - CORNER_RECT_WIDTH, CORNER_RECT_HEIGHT, height, paint);
        canvas.drawRect(0, height - CORNER_RECT_HEIGHT, CORNER_RECT_WIDTH, height, paint);
        //右下
        canvas.drawRect(width - CORNER_RECT_WIDTH, height - CORNER_RECT_HEIGHT, width, height, paint);
        canvas.drawRect(width - CORNER_RECT_HEIGHT, height - CORNER_RECT_WIDTH, width, height, paint);
    }

    /**
     * 绘制扫描线
     */
    private void drawLaserScanner(Canvas canvas) {
        paint.setColor(lineColor);
        //线性渐变
        LinearGradient linearGradient = new LinearGradient(
                0, scannerStart,
                0, scannerStart + SCANNER_LINE_HEIGHT,
                shadeColor(lineColor),
                lineColor,
                Shader.TileMode.MIRROR);

        paint.setShader(linearGradient);
        if (scannerStart <= scannerEnd) {
            //椭圆
            RectF rectF = new RectF(2 * SCANNER_LINE_HEIGHT, scannerStart, width - 2 * SCANNER_LINE_HEIGHT, scannerStart + SCANNER_LINE_HEIGHT);
            canvas.drawOval(rectF, paint);
            scannerStart += SCANNER_LINE_MOVE_DISTANCE;
        } else {
            scannerStart = 0;
        }
        paint.setShader(null);
    }

    /**
     * 绘制扫描区边框
     */
    private void drawFrame(Canvas canvas) {
        paint.setColor(frameColor);
        canvas.drawRect(0, 0, width + 1, 2, paint);
        canvas.drawRect(0, 2, 2, height - 1, paint);
        canvas.drawRect(width - 1, 0, width + 1, height - 1, paint);
        canvas.drawRect(0, height - 1, width + 1, height + 1, paint);
    }

    /**
     * 获取扫描线的模糊颜色
     */
    public int shadeColor(int color) {
        String hax = Integer.toHexString(color);
        String result = "20" + hax.substring(2);
        return Integer.valueOf(result, 16);
    }

    /**
     * 获取扫描区域
     */
    public RectF getRectF() {
        return new RectF(
                getLeft(),
                getTop(),
                getLeft() + getMeasuredWidth(),
                getTop() + getMeasuredHeight()
        );
    }
}