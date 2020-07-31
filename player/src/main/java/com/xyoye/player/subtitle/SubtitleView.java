package com.xyoye.player.subtitle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Created by XYJ on 2020/2/10.
 */

public class SubtitleView extends View {
    //默认视频宽度
    private static final int DEFAULT_VIDEO_WIDTH = 1920;
    //默认视频高度
    private static final int DEFAULT_VIDEO_HEIGHT = 1080;
    //默认字幕文字大小
    private static final float DEFAULT_TEXT_SIZE = 50f;
    //默认字幕文字描边宽度
    private static final float DEFAULT_STOKE_SIZE = 2f;
    //默认字幕字体颜色
    private static final int DEFAULT_TEXT_COLOR = Color.WHITE;
    //默认字幕字体描边颜色
    private static final int DEFAULT_STOKE_COLOR = Color.BLACK;
    //默认字幕文字间隔大小
    private static final float DEFAULT_LETTER_SPACE = 0f;

    //底部字幕
    private SubtitleText[] mBottomSubtitles;
    //顶部字幕
    private SubtitleText[] mTopSubtitles;
    //特殊位置字幕
    private SubtitleText[] mSpecialSubtitles;

    private TextPaint mTextPaint;
    private TextPaint mStokePaint;
    private Rect mTextBounds;

    private int mVideoWidth = DEFAULT_VIDEO_WIDTH;
    private int mVideoHeight = DEFAULT_VIDEO_HEIGHT;
    private float mTextSize = DEFAULT_TEXT_SIZE;
    private float mStokeSize = DEFAULT_STOKE_SIZE;
    private int mTextColor = DEFAULT_TEXT_COLOR;
    private int mStokeColor = DEFAULT_STOKE_COLOR;
    private float mLetterSpace = DEFAULT_LETTER_SPACE;

    public SubtitleView(Context context) {
        this(context, null);
    }

    public SubtitleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SubtitleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTextBounds = new Rect();

        mBottomSubtitles = new SubtitleText[0];
        mTopSubtitles = new SubtitleText[0];
        mSpecialSubtitles = new SubtitleText[0];

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mStokePaint = new TextPaint();
        mStokePaint.setAntiAlias(true);
        mStokePaint.setStyle(Paint.Style.STROKE);
        mStokePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mStokePaint.setTextAlign(Paint.Align.CENTER);

        setBackgroundColor(Color.TRANSPARENT);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        resetTextPaint();

        resetStokePaint();
    }

    private void resetTextPaint() {
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextPaint.setLetterSpacing(mLetterSpace);
        }
    }

    private void resetStokePaint() {
        mStokePaint.setTextSize(mTextSize);
        mStokePaint.setStrokeWidth(mStokeSize);
        mStokePaint.setColor(mStokeColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextPaint.setLetterSpacing(mLetterSpace);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制前，清除上一次的字幕
        clearSubtitle(canvas);

        //底部字幕倒序绘制，从下往上绘制
        int mSubtitleY = getMeasuredHeight();
        for (int i = mBottomSubtitles.length - 1; i >= 0; i--) {
            SubtitleText subtitle = mBottomSubtitles[i];
            if (TextUtils.isEmpty(subtitle.text)) {
                continue;
            }

            //底部字幕除第一行，都略微缩小
            mTextSize = subtitle.size == 0 ? DEFAULT_TEXT_SIZE : subtitle.size;
            if (i != 0) {
                mTextSize -= dp2px(2);
            }

            mTextColor = subtitle.color == 0 ? DEFAULT_TEXT_COLOR : subtitle.color;
            mStokeColor = subtitle.stokeColor == 0 ? DEFAULT_STOKE_COLOR : subtitle.stokeColor;
            mStokeSize = subtitle.stokeSize == 0 ? DEFAULT_STOKE_SIZE : subtitle.stokeSize * DEFAULT_STOKE_SIZE;
            mLetterSpace = subtitle.letterSpace == 0 ? DEFAULT_LETTER_SPACE : subtitle.letterSpace;

            resetTextPaint();
            resetStokePaint();

            mTextPaint.getTextBounds(subtitle.text, 0, subtitle.text.length(), mTextBounds);

            int x = getMeasuredWidth() / 2;
            int y = mSubtitleY - mTextBounds.height() / 2;
            drawSubtitle(canvas, subtitle.text, x, y, subtitle.angle);

            mSubtitleY -= mTextBounds.height() + dp2px(3);
        }

        //顶部字幕绘制，从上往下绘制
        mSubtitleY = dp2px(10);
        for (SubtitleText topSubtitle : mTopSubtitles) {
            if (TextUtils.isEmpty(topSubtitle.text)) {
                continue;
            }

            mTextColor = topSubtitle.color == 0 ? DEFAULT_TEXT_COLOR : topSubtitle.color;
            mTextSize = topSubtitle.size == 0 ? DEFAULT_TEXT_SIZE : topSubtitle.size;
            mStokeColor = topSubtitle.stokeColor == 0 ? DEFAULT_STOKE_COLOR : topSubtitle.stokeColor;
            mStokeSize = topSubtitle.stokeSize == 0 ? DEFAULT_STOKE_SIZE : topSubtitle.stokeSize * DEFAULT_STOKE_SIZE;
            mLetterSpace = topSubtitle.letterSpace == 0 ? DEFAULT_LETTER_SPACE : topSubtitle.letterSpace;

            resetTextPaint();
            resetStokePaint();

            mTextPaint.getTextBounds(topSubtitle.text, 0, topSubtitle.text.length(), mTextBounds);

            int x = getMeasuredWidth() / 2;
            int y = mSubtitleY + mTextBounds.height() / 2;
            drawSubtitle(canvas, topSubtitle.text, x, y, topSubtitle.angle);

            mSubtitleY += mTextBounds.height() + dp2px(3);
        }

        //特殊字幕根据特定位置绘制
        for (SubtitleText specialSubtitle : mSpecialSubtitles) {
            if (specialSubtitle.point != null) {
                float x = (float) specialSubtitle.point.x / (float) mVideoWidth * (float) getMeasuredWidth();
                float y = (float) specialSubtitle.point.y / (float) mVideoHeight * (float) getMeasuredHeight();

                drawSubtitle(canvas, specialSubtitle.text, x, y, specialSubtitle.angle);
                return;
            }
        }
    }

    private void drawSubtitle(Canvas canvas, String text, float centerX, float centerY, int angle) {
        if (angle > 0) {
            canvas.rotate(angle, centerX, centerY);
        }
        if (mTextColor == DEFAULT_TEXT_COLOR) {
            canvas.drawText(text, centerX, centerY, mStokePaint);
        }
        canvas.drawText(text, centerX, centerY, mTextPaint);
        if (angle > 0) {
            canvas.rotate(-angle, centerX, centerY);
        }
    }

    private void clearSubtitle(Canvas canvas) {
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    public void setBottomTexts(SubtitleText... subtitleArray) {
        this.mBottomSubtitles = subtitleArray;
        invalidate();
    }

    public void setTopTexts(SubtitleText... subtitleArray) {
        this.mTopSubtitles = subtitleArray;
        invalidate();
    }

    /**
     * 特殊字幕尚未完善
     */
    @Deprecated
    public void setSpecialTexts(SubtitleText... subtitleArray) {
        this.mSpecialSubtitles = subtitleArray;
        invalidate();
    }

    public void setVideoSize(int width, int height) {
        this.mVideoWidth = width;
        this.mVideoHeight = height;
    }

    private int dp2px(final float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static class SubtitleText {
        //字幕问题
        private String text;
        //文字大小
        private int size;
        //字体颜色
        private int color;
        //字体描边颜色
        private int stokeColor;
        //字体描边宽度
        private int stokeSize;
        //字符间距
        private float letterSpace;
        //字幕文字角度
        private int angle;
        //字幕位置
        private Point point;

        public SubtitleText() {
            this("");
        }

        public SubtitleText(String text) {
            this.text = text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public void setStokeColor(int stokeColor) {
            this.stokeColor = stokeColor;
        }

        public void setStokeSize(int stokeSize) {
            this.stokeSize = stokeSize;
        }

        /**
         * 由于字幕显示位置无法确定，此方法暂不使用
         */
        @Deprecated
        public void setLetterSpace(float letterSpace) {
            this.letterSpace = letterSpace;
        }

        /**
         * 由于字幕显示位置无法确定，此方法暂不使用
         */
        @Deprecated
        public void setAngle(int angle) {
            this.angle = angle;
        }

        /**
         * 由于字幕显示位置无法确定，此方法暂不使用
         */
        @Deprecated
        public void setPoint(int x, int y) {
            this.point = new Point();
            this.point.x = x;
            this.point.y = y;
        }
    }
}
