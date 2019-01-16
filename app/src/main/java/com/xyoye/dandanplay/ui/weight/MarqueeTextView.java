package com.xyoye.dandanplay.ui.weight;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 单行文本跑马灯控件
 */
@SuppressLint("AppCompatCustomView")
public class MarqueeTextView extends TextView implements OnClickListener {

    public static final int TOLEFT = 0;
    public static final int TORIGHT = 1;
    private final static String TAG = MarqueeTextView.class.getSimpleName();
    /**
     * 是否开始滚动
     */
    private boolean mIsStarting = false;
    /**
     * 是否可以点击
     */
    private boolean mIsClickable = false;
    /**
     * 文本长度
     */
    private float mTextLength;
    /**
     * view的宽度
     */
    private float mViewWidth;
    /**
     * 文字的横坐标
     */
    private float mX;
    /**
     * 文字的纵坐标
     */
    private float mY;
    /**
     * view的宽度+文字长度
     */
    private float mViewWidthPlusTextLength;
    /**
     * view的宽度+文字长度*2
     */
    private float mViewWidthPlusTwoTextLength;
    /**
     * 绘图样式
     */
    private Paint mPaint;
    /**
     * 文本内容
     */
    private String mText;
    /**
     * 文字滚动间隔，越大越快
     */
    private float mStep = 2;
    /**
     * 滚动方向
     */
    private int mOrientation = 0;
    private boolean isFirst = true;
    private boolean isFirstDraw = true;

    public MarqueeTextView(Context context) {
        this(context, null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setSingleLine();
        setEllipsize(null);
    }

    /**
     * 设置是否允许点击，点击会 停止/开始 滚动
     */
    public MarqueeTextView setOnClickable(boolean clickable) {
        this.mIsClickable = clickable;
        if (clickable) {
            setOnClickListener(this);
        }
        return this;
    }

    /**
     * 设置文本
     */
    public MarqueeTextView setText(@NonNull String strings) {
        mText = strings;
        this.mPaint = getPaint();
        return this;
    }

    /**
     * 设置滚动间隔，越大滚动的越快
     */
    public MarqueeTextView setStep(float step) {
        this.mStep = step;
        return this;
    }

    /**
     * 设置滚动方向
     */
    public MarqueeTextView setOrientation(@Orientation int orientation) {
        this.mOrientation = orientation;
        return this;
    }

    public void startScroll() {
        if (mPaint.measureText(mText) <= getWidth()){
            return;
        }
        mIsStarting = true;
        invalidate();
    }

    public void stopScroll() {
        mIsStarting = false;
        if (mOrientation == TOLEFT) {
            mX = mViewWidthPlusTextLength;
        }else {
            mX = 0;
        }
        invalidate();
    }

    public boolean isScrolling(){
        return mIsStarting;
    }

    private void switchText() {
        mTextLength = mPaint.measureText(mText);
        mViewWidth = getWidth();
        mViewWidthPlusTextLength = mViewWidth + mTextLength;
        mViewWidthPlusTwoTextLength = mViewWidth + mTextLength * 2;

        if (mOrientation == TOLEFT) {
            if (isFirstDraw){
                isFirstDraw = false;
                mX = mViewWidthPlusTextLength;
            }else
                mX = mTextLength;
        } else if (mOrientation == TORIGHT) {
            if (isFirstDraw){
                isFirstDraw = false;
                mX = 0;
            }else
                mX = -mTextLength;
        }

        mY = getTextSize() + getPaddingTop();
    }

    @Override
    public boolean onPreDraw() {
        if (isFirst) {
            isFirst = false;
            switchText();
        }
        return super.onPreDraw();
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (TextUtils.isEmpty(mText)) {
            return;
        }

        switch (mOrientation) {
            case TOLEFT:
                canvas.drawText(mText, mViewWidthPlusTextLength - mX, mY, mPaint);
                break;
            case TORIGHT:
                canvas.drawText(mText, mX, mY, mPaint);
                break;
        }

        if (!mIsStarting) {
            this.setEllipsize(TextUtils.TruncateAt.END);
            return;
        }

        switch (mOrientation) {
            case TOLEFT:
                mX += mStep;
                if (mX > mViewWidthPlusTwoTextLength) {
                    switchText();
                }
                break;
            case TORIGHT:
                mX += mStep;
                if (mX > mViewWidthPlusTwoTextLength) {
                    switchText();
                }
                break;
        }

        invalidate();
    }

    @Override
    public void onClick(View v) {
        if (!mIsClickable) {
            return;
        }

        if (mIsStarting) {
            stopScroll();
        } else {
            startScroll();
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.step = mX;
        ss.isStarting = mIsStarting;
        ss.text = mText;
        ss.temp_view_plus_text_length = mViewWidthPlusTextLength;
        ss.temp_view_plus_two_text_length = mViewWidthPlusTwoTextLength;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mX = ss.step;
        mIsStarting = ss.isStarting;
        mText = ss.text;
        mViewWidthPlusTextLength = ss.temp_view_plus_text_length;
        mViewWidthPlusTwoTextLength = ss.temp_view_plus_two_text_length;
    }

    @IntDef({TOLEFT, TORIGHT})
    @Retention(RetentionPolicy.SOURCE)
    @interface Orientation {
    }

    public static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }
        };
        boolean isStarting;
        float step;
        String text;
        float temp_view_plus_text_length;
        float temp_view_plus_two_text_length;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            isStarting = in.readByte() != 0;
            step = in.readFloat();
            text = in.readString();
            temp_view_plus_text_length = in.readFloat();
            temp_view_plus_two_text_length = in.readFloat();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeByte((byte) (isStarting ? 1 : 0));
            out.writeFloat(step);
            out.writeString(text);
            out.writeFloat(temp_view_plus_text_length);
            out.writeFloat(temp_view_plus_two_text_length);
        }
    }
}