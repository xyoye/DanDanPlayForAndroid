package com.xyoye.dandanplay.ui.weight.anim.path;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.xyoye.dandanplay.R;

/**
 * Created by xyoye on 2019/9/15.
 */

public class TextPathAnimView extends View {
    private static final String TAG = TextPathAnimView.class.getSimpleName();

    private final int mTextBgColor;
    private final int mTextFgColor;
    private final boolean mIsLoop;
    private final int mAnimDuration;

    private Paint mPaint;
    private Path mAnimPath;
    private Path mSourcePath;
    private TextPath mSourceTextPath;
    private PathMeasure mPathMeasure;
    private ValueAnimator mAnimator;
    private AnimListener animListener;

    private int mPaddingLeft, mPaddingTop;

    public TextPathAnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextPathAnimView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TextPathAnimView, defStyle, 0);
        mAnimDuration = typedArray.getInt(R.styleable.TextPathAnimView_duration, 1500);
        mTextBgColor = typedArray.getColor(R.styleable.TextPathAnimView_text_bg_color, Color.BLACK);
        mTextFgColor = typedArray.getColor(R.styleable.TextPathAnimView_text_fg_color, Color.WHITE);
        mIsLoop = typedArray.getBoolean(R.styleable.TextPathAnimView_loop, true);

        String contentText = typedArray.getString(R.styleable.TextPathAnimView_text);
        float sizeScale = typedArray.getFloat(R.styleable.TextPathAnimView_text_size_scale, dp2px(14));
        float stokeWidth = typedArray.getFloat(R.styleable.TextPathAnimView_text_stoke_width, 3f);
        float textInterval = typedArray.getDimension(R.styleable.TextPathAnimView_text_interval, dp2px(5));
        typedArray.recycle();

        mSourceTextPath = new TextPath(contentText, sizeScale, textInterval);
        mSourcePath = mSourceTextPath.getPath();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(stokeWidth);

        mAnimPath = new Path();
        mPathMeasure = new PathMeasure();
    }

    private void initAnim() {
        mAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.addUpdateListener(animation -> {
            float stopD = mPathMeasure.getLength() * (float) animation.getAnimatedValue();
            mPathMeasure.getSegment(0, stopD, mAnimPath, true);
            invalidate();
        });

        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                mPathMeasure.getSegment(0, mPathMeasure.getLength(), mAnimPath, true);
                mPathMeasure.nextContour();
                if (mPathMeasure.getLength() == 0) {
                    if (mIsLoop) {
                        mAnimPath.reset();
                        mAnimPath.lineTo(0, 0);
                        mPathMeasure.setPath(mSourcePath, false);
                        if (animListener != null) {
                            animListener.onLoop();
                        }
                    } else {
                        animation.end();
                    }
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (animListener != null) {
                    animListener.onStart();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (animListener != null && !mIsLoop) {
                    animListener.onEnd();
                }
            }
        });
    }

    private float dp2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPaddingLeft = getPaddingLeft();
        mPaddingTop = getPaddingTop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int originWith = MeasureSpec.getSize(widthMeasureSpec);
        int originHeight = MeasureSpec.getSize(heightMeasureSpec);

        int newWidth = (int) mSourceTextPath.getWidth();
        int newHeight = (int) mSourceTextPath.getHeight();

        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT
                && getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(newWidth, newHeight);
        } else if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(newWidth, originHeight);
        } else if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(originWith, newHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(mPaddingLeft, mPaddingTop);

        mPaint.setColor(mTextBgColor);
        canvas.drawPath(mSourcePath, mPaint);

        mPaint.setColor(mTextFgColor);
        canvas.drawPath(mAnimPath, mPaint);
    }

    public void startAnim() {
        if (null == mAnimator) {
            initAnim();
        } else if (mAnimator.isRunning()) {
            return;
        }

        mAnimPath.reset();
        mAnimPath.lineTo(0, 0);
        mPathMeasure.setPath(mSourcePath, false);
        int count = 0;
        while (mPathMeasure.getLength() != 0) {
            mPathMeasure.nextContour();
            count++;
        }
        mPathMeasure.setPath(mSourcePath, false);

        mAnimator.setDuration(mAnimDuration / count);
        mAnimator.start();
    }

    public void stopAnim() {
        if (null != mAnimator && mAnimator.isRunning()) {
            mAnimator.end();
        }
    }

    public void setAnimListener(AnimListener listener) {
        this.animListener = listener;
    }

    public interface AnimListener {
        void onStart();

        void onEnd();

        void onLoop();
    }
}
