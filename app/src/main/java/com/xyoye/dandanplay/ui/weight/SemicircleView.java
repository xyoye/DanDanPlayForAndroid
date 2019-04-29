package com.xyoye.dandanplay.ui.weight;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;

import com.xyoye.dandanplay.R;

/**
 * Created by xyy on 2019/4/4.
 * 弧形图片
 */

public class SemicircleView extends android.support.v7.widget.AppCompatImageView {

    private Bitmap mForeground;

    //曲率
    private float mCurvature;
    //距离顶部
    private float mDistanceTop;
    //遮罩外层颜色
    private int maskOutColor;
    //遮罩内层颜色
    private int maskInColor;

    public SemicircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SemicircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SemicircleView, defStyle, 0);
        mCurvature = typedArray.getFloat(R.styleable.SemicircleView_curvature, 1);
        mDistanceTop = typedArray.getDimension(R.styleable.SemicircleView_distance_top, 0);
        maskOutColor = typedArray.getColor(R.styleable.SemicircleView_mask_out_color, maskOutColor);
        maskInColor = typedArray.getColor(R.styleable.SemicircleView_mask_in_color, maskInColor);
        typedArray.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initSource(getWidth(), getHeight());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mForeground, 0, 0, null);
    }

    private void initSource(int width, int height){
        //获取半径 * 曲率，曲率越大，半径越大
        float mRadius = getRadius(width, mDistanceTop) * mCurvature;
        //计算圆点
        float mStartX = (float) (width / 2);
        float mStartY = mDistanceTop - mRadius;

        Paint mPaint = new Paint();
        //遮罩圆形区域内颜色
        mPaint.setColor(maskInColor);
        mPaint.setAntiAlias(true);
        //显示相交部分
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        //遮罩圆形区域外部
        mForeground = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //遮罩圆形区域外部颜色
        mForeground.eraseColor(maskOutColor);

        Canvas mForeCanvas = new Canvas(mForeground);
        mForeCanvas.drawCircle(mStartX, mStartY, mRadius, mPaint);
    }


    //过三点求圆半径
    private float getRadius(float width , float top){
        double x1,y1, x2,y2, x3,y3;
        double a, b, c, g, e, f;

        x1 = 0;
        y1 = 0;

        x2 = width;
        y2 = 0;

        x3 = width/2;
        y3 = top;

        e = 2 * (x2 - x1);
        f = 2 * (y2 - y1);

        g = x2*x2 - x1*x1 + y2*y2 - y1*y1;

        a = 2 * (x3 - x2);
        b = 2 * (y3 - y2);

        c = x3*x3 - x2*x2 + y3*y3 - y2*y2;

        double X = (g*b - c*f) / (e*b - a*f);
        double Y = (a*g - c*e) / (a*f - b*e);

        double R = Math.sqrt((X-x1)*(X-x1)+(Y-y1)*(Y-y1));
        return (float)R;
    }
}
