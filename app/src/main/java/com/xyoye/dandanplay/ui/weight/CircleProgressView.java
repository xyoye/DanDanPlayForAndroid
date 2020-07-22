package com.xyoye.dandanplay.ui.weight;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.utils.CommonUtils;

/**
 * 圆形进度条
 */
public class CircleProgressView extends View {

    private Paint mPaint;
    private TextPaint mTextPaint;
    private RectF rectF;
    private Rect textBounds;

    private int backgroundColor;
    private int progressColor;
    private int progress;
    private float progressWidth;

    private float textSize;
    private int textColor;

    public CircleProgressView(Context context) {
        super(context);
        init();
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressView);
        backgroundColor = typedArray.getColor(R.styleable.CircleProgressView_progressBackgroundColor, CommonUtils.getResColor(R.color.text_gray));
        progressColor = typedArray.getColor(R.styleable.CircleProgressView_progressColor, CommonUtils.getResColor(R.color.theme_color));
        progress = typedArray.getInt(R.styleable.CircleProgressView_progress, 0);
        progressWidth = typedArray.getDimension(R.styleable.CircleProgressView_progressWidth, dp2px(1));
        textSize = typedArray.getDimension(R.styleable.CircleProgressView_textSize, dp2px(12));
        textColor = typedArray.getColor(R.styleable.CircleProgressView_textColor, CommonUtils.getResColor(R.color.text_black));
        typedArray.recycle();
        init();
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        float offset = progressWidth / 2;

        mPaint.setShader(null);
        mPaint.setStrokeWidth(progressWidth);
        mPaint.setColor(backgroundColor);

        canvas.drawCircle(centerX, centerY, centerX - offset, mPaint);

        if (rectF == null) {
            rectF = new RectF(
                    offset,
                    offset,
                    width - offset,
                    height - offset);
        }

        mPaint.setColor(progressColor);
        canvas.drawArc(rectF, -90, 360f * (float) progress / 100f, false, mPaint);

        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(textColor);
        String progressText = progress + "%";
        mTextPaint.getTextBounds(progressText, 0, progressText.length(), textBounds);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        int baseLineY = (int) (centerY - top / 2 - bottom / 2);

        canvas.drawText(progressText, centerX, baseLineY, mTextPaint);
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        textBounds = new Rect();
    }

    public int dp2px(final float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Keep
    public void updateProgress(int progress) {
        if (progress > 100)
            progress = 100;
        this.progress = progress;
        invalidate();
    }
}
