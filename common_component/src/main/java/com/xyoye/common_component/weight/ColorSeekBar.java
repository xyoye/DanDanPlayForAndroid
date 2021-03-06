package com.xyoye.common_component.weight;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ArrayRes;

import com.xyoye.common_component.R;

import java.util.ArrayList;
import java.util.List;

public class ColorSeekBar extends View {
    private int[] mColorSeeds = new int[]{
            0xFF000000, 0xFF9900FF, 0xFF0000FF,
            0xFF00FF00, 0xFF00FFFF, 0xFFFF0000,
            0xFFFF00FF, 0xFFFF6600, 0xFFFFFF00,
            0xFFFFFFFF, 0xFF000000};
    private Context mContext;

    private Paint mColorRectPaint;
    private Bitmap mTransparentBitmap;
    private RectF mColorRect;

    private int mThumbHeight;
    private float mThumbRadius;
    private int mBarHeight = 2;
    private int mBarWidth;
    private int mBarRadius;
    private int mThumbColor;

    private int mMaxPosition;
    private int mPosition;

    private int realLeft;
    private int mColorsToInvoke = -1;
    private List<Integer> mCachedColors = new ArrayList<>();

    private Paint colorPaint = new Paint();
    private Paint thumbPaint = new Paint();

    private boolean mInit = false;
    private boolean mFirstDraw = true;
    private boolean mMovingColorBar;

    private OnColorChangeListener mOnColorChangeLister;

    public ColorSeekBar(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public ColorSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public ColorSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ColorSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        applyStyle(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightSpeMode = MeasureSpec.getMode(heightMeasureSpec);

        if (heightSpeMode == MeasureSpec.AT_MOST || heightSpeMode == MeasureSpec.UNSPECIFIED) {
            int mViewHeight = mThumbHeight + mBarHeight;
            setMeasuredDimension(widthMeasureSpec, mViewHeight);
        }
    }

    protected void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;
        //get attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorSeekBar, defStyleAttr, defStyleRes);
        mMaxPosition = a.getInteger(R.styleable.ColorSeekBar_maxPosition, 100);
        mPosition = a.getInteger(R.styleable.ColorSeekBar_defaultPosition, 0);
        mBarHeight = (int) a.getDimension(R.styleable.ColorSeekBar_barHeight, (float) dp2px(5));
        mBarRadius = (int) a.getDimension(R.styleable.ColorSeekBar_barRadius, (float) dp2px(5));
        mThumbHeight = (int) a.getDimension(R.styleable.ColorSeekBar_thumbHeight, (float) dp2px(15));
        mThumbColor = a.getColor(R.styleable.ColorSeekBar_thumbColor, -1);
        int colorsId = a.getResourceId(R.styleable.ColorSeekBar_colorSeeds, 0);
        a.recycle();

        if (colorsId != 0) {
            mColorSeeds = getColorsById(colorsId);
        }

        setBackgroundColor(Color.TRANSPARENT);
    }

    private void init() {
        //init size
        mThumbRadius = mThumbHeight / 2f;
        int mPaddingSize = (int) mThumbRadius;
        int realRight = getWidth() - getPaddingRight() - mPaddingSize;
        //init left right top bottom
        realLeft = getPaddingLeft() + mPaddingSize;
        int realTop = getPaddingTop() + mPaddingSize;

        mBarWidth = realRight - realLeft;

        //init rect
        mColorRect = new RectF(realLeft, realTop, realRight, realTop + mBarHeight);

        //init paint
        LinearGradient mColorGradient = new LinearGradient(0, 0, mColorRect.width(), 0, mColorSeeds, null, Shader.TileMode.CLAMP);
        mColorRectPaint = new Paint();
        mColorRectPaint.setShader(mColorGradient);
        mColorRectPaint.setAntiAlias(true);
        cacheColors();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTransparentBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        mTransparentBitmap.eraseColor(Color.TRANSPARENT);
        init();
        mInit = true;
        if (mColorsToInvoke != -1) {
            seekToColor(mColorsToInvoke);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int color = getCurrentColor();

        float colorPosition = (float) mPosition / mMaxPosition * mBarWidth;
        colorPaint.setAntiAlias(true);
        colorPaint.setColor(color);
        //clear
        canvas.drawBitmap(mTransparentBitmap, 0, 0, null);

        //draw color bar
        canvas.drawRoundRect(mColorRect, mBarRadius, mBarRadius, mColorRectPaint);
        //draw color bar thumb
        float thumbX = colorPosition + realLeft;
        float thumbY = mColorRect.top + mColorRect.height() / 2;
        canvas.drawCircle(thumbX, thumbY, mBarHeight / 2f + 5, colorPaint);

        //draw color bar thumb radial gradient shader
        //RadialGradient thumbShader = new RadialGradient(thumbX, thumbY, mThumbRadius, toAlpha, null, Shader.TileMode.CLAMP);
        int thumbColor = mThumbColor == -1 ? color : mThumbColor;
        thumbPaint.setAntiAlias(true);

        //thumbGradientPaint.setShader(thumbShader);
        thumbPaint.setColor(Color.WHITE);
        canvas.drawCircle(thumbX, thumbY, (mThumbHeight / 2f) + 2f, thumbPaint);
        thumbPaint.setColor(thumbColor);
        canvas.drawCircle(thumbX, thumbY, mThumbHeight / 2f, thumbPaint);

        if (mFirstDraw) {
            if (mOnColorChangeLister != null) {
                mOnColorChangeLister.onColorChange(mPosition, getCurrentColor());
            }
            mFirstDraw = false;
        }
        super.onDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return true;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isOnBar(mColorRect, x, y)) {
                    mMovingColorBar = true;
                    float value = (x - realLeft) / mBarWidth * mMaxPosition;
                    seekTo((int) value);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                if (mMovingColorBar) {
                    float value = (x - realLeft) / mBarWidth * mMaxPosition;
                    seekTo((int) value);
                }
                if (mOnColorChangeLister != null && mMovingColorBar) {
                    mOnColorChangeLister.onColorChange(mPosition, getCurrentColor());
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mMovingColorBar = false;
                break;
            default:
        }
        return true;
    }

    public int getCurrentColor() {
        //pick mode
        if (mPosition >= mCachedColors.size()) {
            int color = pickColor(mPosition);
            return Color.rgb(Color.red(color), Color.green(color), Color.blue(color));
        }

        //cache mode
        return mCachedColors.get(mPosition);
    }

    public void setOnColorChangeListener(OnColorChangeListener onColorChangeListener) {
        this.mOnColorChangeLister = onColorChangeListener;
    }

    public void setColorSeeds(@ArrayRes int resId) {
        setColorSeeds(getColorsById(resId));
    }

    public void setColorSeeds(int[] colors) {
        mColorSeeds = colors;
        init();
        invalidate();
        if (mOnColorChangeLister != null) {
            mOnColorChangeLister.onColorChange(mPosition, getCurrentColor());
        }
    }

    public int getPositionFromColor(int color) {
        return mCachedColors.indexOf(Color.rgb(Color.red(color), Color.green(color), Color.blue(color)));
    }

    public void setMaxPosition(int value) {
        this.mMaxPosition = value;
        invalidate();
        cacheColors();
    }

    public void seekTo(int position) {
        this.mPosition = position;
        mPosition = Math.min(mPosition, mMaxPosition);
        mPosition = Math.max(mPosition, 0);
        invalidate();
        if (mOnColorChangeLister != null) {
            mOnColorChangeLister.onColorChange(mPosition, getCurrentColor());
        }
    }

    public void seekToColor(int color) {
        int rgbColor = Color.rgb(Color.red(color), Color.green(color), Color.blue(color));

        if (mInit) {
            int value = mCachedColors.indexOf(rgbColor);
            seekTo(value);
        } else {
            mColorsToInvoke = color;
        }
    }

    public int getPosition() {
        return mPosition;
    }

    /**
     * @return whether MotionEvent is performing on bar or not
     */
    private boolean isOnBar(RectF r, float x, float y) {
        return r.left - mThumbRadius < x && x < r.right + mThumbRadius && r.top - mThumbRadius < y && y < r.bottom + mThumbRadius;
    }

    public boolean isFirstDraw() {
        return mFirstDraw;
    }

    private void cacheColors() {
        //if the view's size hasn't been initialized. do not cache.
        if (mBarWidth < 1) {
            return;
        }
        mCachedColors.clear();
        for (int i = 0; i <= mMaxPosition; i++) {
            mCachedColors.add(pickColor(i));
        }
    }

    private int pickColor(int value) {
        return pickColor((float) value / mMaxPosition * mBarWidth);
    }

    private int pickColor(float position) {
        float unit = position / mBarWidth;
        if (unit <= 0.0) {
            return mColorSeeds[0];
        }


        if (unit >= 1) {
            return mColorSeeds[mColorSeeds.length - 1];
        }

        float colorPosition = unit * (mColorSeeds.length - 1);
        int i = (int) colorPosition;
        colorPosition -= i;
        int c0 = mColorSeeds[i];
        int c1 = mColorSeeds[i + 1];
        int mRed = mix(Color.red(c0), Color.red(c1), colorPosition);
        int mGreen = mix(Color.green(c0), Color.green(c1), colorPosition);
        int mBlue = mix(Color.blue(c0), Color.blue(c1), colorPosition);
        return Color.rgb(mRed, mGreen, mBlue);
    }

    private int mix(int start, int end, float position) {
        return start + Math.round(position * (end - start));
    }

    private int[] getColorsById(@ArrayRes int id) {
        if (isInEditMode()) {
            String[] s = mContext.getResources().getStringArray(id);
            int[] colors = new int[s.length];
            for (int j = 0; j < s.length; j++) {
                colors[j] = Color.parseColor(s[j]);
            }
            return colors;
        } else {
            TypedArray typedArray = mContext.getResources().obtainTypedArray(id);
            int[] colors = new int[typedArray.length()];
            for (int j = 0; j < typedArray.length(); j++) {
                colors[j] = typedArray.getColor(j, Color.BLACK);
            }
            typedArray.recycle();
            return colors;
        }
    }

    private int dp2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public interface OnColorChangeListener {

        void onColorChange(int position, int color);
    }
}
