package com.xyoye.dandanplay.ui.weight;

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
import com.xyoye.dandanplay.utils.scan.camera.CameraManager;
import com.xyoye.dandanplay.utils.scan.view.QRCodeReaderView;

public final class ScanWindowView extends View {
    private CameraManager cameraManager;
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
    //背景颜色
    private final int maskColor;
    //是否显示边框
    private final boolean showFrame;

    //大小
    private final float previewWidth, previewHeight;

    //位置属性
    private float previewX, previewY;
    private final boolean centerX, centerY;

    public float scannerStart = 0;
    public float scannerEnd = 0;

    @SuppressLint("CustomViewStyleable")
    public ScanWindowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanWindowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化自定义属性信息
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ScanWindowView);
        frameColor = array.getColor(R.styleable.ScanWindowView_frame_color, getResources().getColor(R.color.scan_window_frame));
        rectColor = array.getColor(R.styleable.ScanWindowView_rect_color,  getResources().getColor(R.color.scan_window_corner));
        lineColor = array.getColor(R.styleable.ScanWindowView_line_color,  getResources().getColor(R.color.scan_window_laser));
        maskColor = array.getColor(R.styleable.ScanWindowView_mask_color,  getResources().getColor(R.color.scan_window_mask));
        previewX = array.getDimension(R.styleable.ScanWindowView_preview_x, 0f);
        previewY = array.getDimension(R.styleable.ScanWindowView_preview_y, 0f);
        centerX = array.getBoolean(R.styleable.ScanWindowView_preview_centerHorizontal, false);
        centerY = array.getBoolean(R.styleable.ScanWindowView_preview_centerVertical, false);
        previewWidth = array.getDimension(R.styleable.ScanWindowView_preview_width, 0);
        previewHeight = array.getDimension(R.styleable.ScanWindowView_preview_height, 0);
        showFrame = array.getBoolean(R.styleable.ScanWindowView_show_frame, false);
        array.recycle();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {

        if (scannerStart == 0 || scannerEnd == 0) {
            scannerStart = previewY;
            scannerEnd = previewY + previewHeight - SCANNER_LINE_HEIGHT;
        }
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        if (centerX) previewX = (width - previewWidth) / 2;
        if (centerY) previewY = (height - previewHeight) / 2;

        if(cameraManager != null)
            cameraManager.setFramingRectF(new RectF(previewX, previewY, previewX + previewWidth, previewY + previewHeight));

        // Draw the exterior (i.e. outside the framing rect) darkened
        drawExterior(canvas, width, height);

        // 绘制方框
        if (showFrame)
            drawFrame(canvas);
        // 绘制边角
        drawCorner(canvas);
        // 绘制扫描线
        drawLaserScanner(canvas);

        postInvalidateDelayed(ANIMATION_DELAY,
                (int) (previewX - POINT_SIZE),
                (int) (previewY - POINT_SIZE),
                (int) (previewX + previewWidth + POINT_SIZE),
                (int) (previewY + previewHeight + POINT_SIZE));
    }

    //绘制边角
    private void drawCorner(Canvas canvas) {
        paint.setColor(rectColor);
        //左上
        canvas.drawRect(previewX, previewY, previewX + CORNER_RECT_WIDTH, previewY + CORNER_RECT_HEIGHT, paint);
        canvas.drawRect(previewX, previewY, previewX + CORNER_RECT_HEIGHT, previewY + CORNER_RECT_WIDTH, paint);
        //右上
        canvas.drawRect(previewX + previewWidth - CORNER_RECT_WIDTH, previewY, previewX + previewWidth, previewY + CORNER_RECT_HEIGHT, paint);
        canvas.drawRect(previewX + previewWidth - CORNER_RECT_HEIGHT, previewY, previewX + previewWidth, previewY + CORNER_RECT_WIDTH, paint);
        //左下
        canvas.drawRect(previewX, previewY + previewHeight - CORNER_RECT_WIDTH, previewX + CORNER_RECT_HEIGHT, previewY + previewHeight, paint);
        canvas.drawRect(previewX, previewY + previewHeight - CORNER_RECT_HEIGHT, previewX + CORNER_RECT_WIDTH, previewY + previewHeight, paint);
        //右下
        canvas.drawRect(previewX + previewWidth - CORNER_RECT_WIDTH, previewY + previewHeight - CORNER_RECT_HEIGHT, previewX + previewWidth, previewY + previewHeight, paint);
        canvas.drawRect(previewX + previewWidth - CORNER_RECT_HEIGHT, previewY + previewHeight - CORNER_RECT_WIDTH, previewX + previewWidth, previewY + previewHeight, paint);
    }

    //绘制扫描线
    private void drawLaserScanner(Canvas canvas) {
        paint.setColor(lineColor);
        //线性渐变
        LinearGradient linearGradient = new LinearGradient(
                previewX, scannerStart,
                previewX, scannerStart + SCANNER_LINE_HEIGHT,
                shadeColor(lineColor),
                lineColor,
                Shader.TileMode.MIRROR);

        paint.setShader(linearGradient);
        if (scannerStart <= scannerEnd) {
            //椭圆
            RectF rectF = new RectF(previewX + 2 * SCANNER_LINE_HEIGHT, scannerStart, previewX + previewWidth - 2 * SCANNER_LINE_HEIGHT, scannerStart + SCANNER_LINE_HEIGHT);
            canvas.drawOval(rectF, paint);
            scannerStart += SCANNER_LINE_MOVE_DISTANCE;
        } else {
            scannerStart = previewY;
        }
        paint.setShader(null);
    }

    //处理颜色模糊
    public int shadeColor(int color) {
        String hax = Integer.toHexString(color);
        String result = "20" + hax.substring(2);
        return Integer.valueOf(result, 16);
    }

    // 绘制扫描区边框 Draw a two pixel solid black border inside the framing rect
    private void drawFrame(Canvas canvas) {
        paint.setColor(frameColor);
        canvas.drawRect(previewX, previewY, previewX + previewWidth + 1, previewY + 2, paint);
        canvas.drawRect(previewX, previewY + 2, previewX + 2, previewY + previewHeight - 1, paint);
        canvas.drawRect(previewX + previewWidth - 1, previewY, previewX + previewWidth + 1, previewY + previewHeight - 1, paint);
        canvas.drawRect(previewX, previewY + previewHeight - 1, previewX + previewWidth + 1, previewY + previewHeight + 1, paint);
    }

    // 绘制模糊区域 Draw the exterior (i.e. outside the framing rect) darkened
    private void drawExterior(Canvas canvas, int width, int height) {
        paint.setColor(maskColor);
        canvas.drawRect(0, 0, width, previewY, paint);
        canvas.drawRect(0, previewY, previewX, previewY + previewHeight + 1, paint);
        canvas.drawRect(previewX + previewWidth + 1, previewY, width, previewY + previewHeight + 1, paint);
        canvas.drawRect(0, previewY + previewHeight + 1, width, height, paint);
    }

    public void drawWindow() {
        invalidate();
    }

    public void bindQRCodeView(QRCodeReaderView qrCodeReaderView) {
        this.cameraManager = qrCodeReaderView.getCameraManager();
    }
}