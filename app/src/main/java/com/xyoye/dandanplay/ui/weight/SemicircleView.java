package com.xyoye.dandanplay.ui.weight;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;

import com.xyoye.dandanplay.R;

/**
 * Created by xyy on 2019/4/4.
 */

public class SemicircleView extends android.support.v7.widget.AppCompatImageView {
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

    private Bitmap mForeground;
    private Bitmap mBackground;
    private Canvas mForeCanvas;
    private Paint mPaint;

    //曲率
    private float mCurvature;
    //距离顶部
    private float mDistanceTop;

    public SemicircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SemicircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SemicircleView, defStyle, 0);
        mCurvature = a.getFloat(R.styleable.SemicircleView_curvature, 1.0f);
        mDistanceTop = a.getDimension(R.styleable.SemicircleView_distance_top, getHeight()/2);
        a.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        float mRadius = getRadius(getMeasuredWidth(), mDistanceTop) * mCurvature;
        float mStartX = getMeasuredWidth() / 2;
        float mStartY = mDistanceTop - mRadius;
        mForeCanvas.drawCircle(mStartX, mStartY, mRadius, mPaint);
        //在新的图层上面绘制
        int layer = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        canvas.drawBitmap(mBackground, 0, 0, null);
        canvas.drawBitmap(mForeground, 0, 0, null);
        canvas.restoreToCount(layer);
    }


    //过三点取圆半径
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

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mForeground = bm.copy(Bitmap.Config.ARGB_8888, true);
        initSource();
        invalidate();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mForeground = getBitmapFromDrawable(drawable).copy(Bitmap.Config.ARGB_8888, true);
        initSource();
        invalidate();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mForeground = getBitmapFromDrawable(getDrawable()).copy(Bitmap.Config.ARGB_8888, true);
        initSource();
        invalidate();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        mForeground = getBitmapFromDrawable(getDrawable()).copy(Bitmap.Config.ARGB_8888, true);
        initSource();
        invalidate();
    }

    private void initSource(){
        mForeCanvas = new Canvas(mForeground);
        mBackground = Bitmap.createBitmap(mForeground.getWidth(), mForeground.getWidth(), Bitmap.Config.RGB_565);
        mBackground.eraseColor(Color.parseColor("#88000000"));
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            Bitmap bitmap;

            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(2, 2, BITMAP_CONFIG);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }
}
