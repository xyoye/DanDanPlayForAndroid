package com.xyoye.dandanplay.ui.weight.shadow;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.ColorInt;

import com.xyoye.dandanplay.R;

import java.lang.ref.WeakReference;


/**
 * @author limuyang
 * @date 2018/8/14
 * @class describe
 */
public class LayoutHelper implements ILayout{


    // divider
    private int mTopDividerHeight = 0;
    private int mTopDividerInsetLeft = 0;
    private int mTopDividerInsetRight = 0;
    private int mTopDividerColor;
    private int mTopDividerAlpha = 255;

    private int mBottomDividerHeight = 0;
    private int mBottomDividerInsetLeft = 0;
    private int mBottomDividerInsetRight = 0;
    private int mBottomDividerColor;
    private int mBottomDividerAlpha = 255;

    private int mLeftDividerWidth = 0;
    private int mLeftDividerInsetTop = 0;
    private int mLeftDividerInsetBottom = 0;
    private int mLeftDividerColor;
    private int mLeftDividerAlpha = 255;

    private int mRightDividerWidth = 0;
    private int mRightDividerInsetTop = 0;
    private int mRightDividerInsetBottom = 0;
    private int mRightDividerColor;
    private int mRightDividerAlpha = 255;
    private Paint mDividerPaint;

    // round
    private Paint              mClipPaint;
    private PorterDuffXfermode mMode;
    private int                mRadius;
    private @ILayout.HideRadiusSide int mHideRadiusSide = HIDE_RADIUS_SIDE_NONE;
    private float[] mRadiusArray;
    private RectF   mBorderRect;
    private int mBorderColor = 0;
    private int mBorderWidth = 1;
    private int mOuterNormalColor = 0;
    private WeakReference<View> mOwner;
    private boolean mIsOutlineExcludePadding = false;
    private Path    mPath                    = new Path();

    // shadow
    private boolean mIsShowBorderOnlyBeforeL = true;
    private int mShadowElevation = 0;
    private float mShadowAlpha = 0f;

    // outline inset
    private int mOutlineInsetLeft = 0;
    private int mOutlineInsetRight = 0;
    private int mOutlineInsetTop = 0;
    private int mOutlineInsetBottom = 0;

    LayoutHelper(Context context, AttributeSet attrs, int defAttr, View owner) {
        mOwner = new WeakReference<>(owner);
//        mBottomDividerColor = mTopDividerColor = ContextCompat.getColor(context, R.color.qmui_config_color_separator);
        mBottomDividerColor = mTopDividerColor = Color.BLUE;

        mMode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
        mClipPaint = new Paint();
        mClipPaint.setAntiAlias(true);
//        mShadowAlpha = QMUIResHelper.getAttrFloatValue(context, R.attr.qmui_general_shadow_alpha);
        mBorderRect = new RectF();

        int radius = 0, shadow = 0;
        if (null != attrs || defAttr != 0) {
            TypedArray ta    = context.obtainStyledAttributes(attrs, R.styleable.ShadowLayout, defAttr, 0);
            int        count = ta.getIndexCount();
            for (int i = 0; i < count; ++i) {
                int index = ta.getIndex(i);
                if (index == R.styleable.ShadowLayout_topDividerColor) {
                    mTopDividerColor = ta.getColor(index, mTopDividerColor);
                } else if (index == R.styleable.ShadowLayout_topDividerHeight) {
                    mTopDividerHeight = ta.getDimensionPixelSize(index, mTopDividerHeight);
                } else if (index == R.styleable.ShadowLayout_topDividerInsetLeft) {
                    mTopDividerInsetLeft = ta.getDimensionPixelSize(index, mTopDividerInsetLeft);
                } else if (index == R.styleable.ShadowLayout_topDividerInsetRight) {
                    mTopDividerInsetRight = ta.getDimensionPixelSize(index, mTopDividerInsetRight);
                } else if (index == R.styleable.ShadowLayout_bottomDividerColor) {
                    mBottomDividerColor = ta.getColor(index, mBottomDividerColor);
                } else if (index == R.styleable.ShadowLayout_bottomDividerHeight) {
                    mBottomDividerHeight = ta.getDimensionPixelSize(index, mBottomDividerHeight);
                } else if (index == R.styleable.ShadowLayout_bottomDividerInsetLeft) {
                    mBottomDividerInsetLeft = ta.getDimensionPixelSize(index, mBottomDividerInsetLeft);
                } else if (index == R.styleable.ShadowLayout_bottomDividerInsetRight) {
                    mBottomDividerInsetRight = ta.getDimensionPixelSize(index, mBottomDividerInsetRight);
                } else if (index == R.styleable.ShadowLayout_leftDividerColor) {
                    mLeftDividerColor = ta.getColor(index, mLeftDividerColor);
                } else if (index == R.styleable.ShadowLayout_leftDividerWidth) {
                    mLeftDividerWidth = ta.getDimensionPixelSize(index, mBottomDividerHeight);
                } else if (index == R.styleable.ShadowLayout_leftDividerInsetTop) {
                    mLeftDividerInsetTop = ta.getDimensionPixelSize(index, mLeftDividerInsetTop);
                } else if (index == R.styleable.ShadowLayout_leftDividerInsetBottom) {
                    mLeftDividerInsetBottom = ta.getDimensionPixelSize(index, mLeftDividerInsetBottom);
                } else if (index == R.styleable.ShadowLayout_rightDividerColor) {
                    mRightDividerColor = ta.getColor(index, mRightDividerColor);
                } else if (index == R.styleable.ShadowLayout_rightDividerWidth) {
                    mRightDividerWidth = ta.getDimensionPixelSize(index, mRightDividerWidth);
                } else if (index == R.styleable.ShadowLayout_rightDividerInsetTop) {
                    mRightDividerInsetTop = ta.getDimensionPixelSize(index, mRightDividerInsetTop);
                } else if (index == R.styleable.ShadowLayout_rightDividerInsetBottom) {
                    mRightDividerInsetBottom = ta.getDimensionPixelSize(index, mRightDividerInsetBottom);
                } else if (index == R.styleable.ShadowLayout_borderColor) {
                    mBorderColor = ta.getColor(index, mBorderColor);
                } else if (index == R.styleable.ShadowLayout_borderWidth) {
                    mBorderWidth = ta.getDimensionPixelSize(index, mBorderWidth);
                } else if (index == R.styleable.ShadowLayout_shadowRadius) {
                    radius = ta.getDimensionPixelSize(index, 0);
                } else if (index == R.styleable.ShadowLayout_outerNormalColor) {
                    mOuterNormalColor = ta.getColor(index, mOuterNormalColor);
                } else if (index == R.styleable.ShadowLayout_hideRadiusSide) {
                    mHideRadiusSide = ta.getColor(index, mHideRadiusSide);
                } else if (index == R.styleable.ShadowLayout_showBorderOnlyBeforeL) {
                    mIsShowBorderOnlyBeforeL = ta.getBoolean(index, mIsShowBorderOnlyBeforeL);
                } else if (index == R.styleable.ShadowLayout_shadowElevation) {
                    shadow = ta.getDimensionPixelSize(index, shadow);
                } else if (index == R.styleable.ShadowLayout_shadowAlpha) {
                    mShadowAlpha = ta.getFloat(index, mShadowAlpha);
                } else if (index == R.styleable.ShadowLayout_outlineInsetLeft) {
                    mOutlineInsetLeft = ta.getDimensionPixelSize(index, 0);
                } else if (index == R.styleable.ShadowLayout_outlineInsetRight) {
                    mOutlineInsetRight = ta.getDimensionPixelSize(index, 0);
                } else if (index == R.styleable.ShadowLayout_outlineInsetTop) {
                    mOutlineInsetTop = ta.getDimensionPixelSize(index, 0);
                } else if (index == R.styleable.ShadowLayout_outlineInsetBottom) {
                    mOutlineInsetBottom = ta.getDimensionPixelSize(index, 0);
                } else if (index == R.styleable.ShadowLayout_outlineExcludePadding) {
                    mIsOutlineExcludePadding = ta.getBoolean(index, false);
                }
            }
            ta.recycle();
        }

        setRadiusAndShadow(radius, mHideRadiusSide, shadow, mShadowAlpha);
    }

    @Override
    public void setUseThemeGeneralShadowElevation() {
//        mShadowElevation = QMUIResHelper.getAttrDimen(mContext, R.attr.qmui_general_shadow_elevation);
        setRadiusAndShadow(mRadius, mHideRadiusSide, mShadowElevation, mShadowAlpha);
    }

    @Override
    public void setOutlineExcludePadding(boolean outlineExcludePadding) {
        if (useFeature()) {
            View owner = mOwner.get();
            if (owner == null) {
                return;
            }
            mIsOutlineExcludePadding = outlineExcludePadding;
            owner.invalidateOutline();
        }

    }

    @Override
    public boolean setWidthLimit(int widthLimit) {
        return false;
    }

    @Override
    public boolean setHeightLimit(int heightLimit) {
        return false;
    }

    @Override
    public int getShadowElevation() {
        return mShadowElevation;
    }

    @Override
    public float getShadowAlpha() {
        return mShadowAlpha;
    }

    @Override
    public void setOutlineInset(int left, int top, int right, int bottom) {
        if (useFeature()) {
            View owner = mOwner.get();
            if (owner == null) {
                return;
            }
            mOutlineInsetLeft = left;
            mOutlineInsetRight = right;
            mOutlineInsetTop = top;
            mOutlineInsetBottom = bottom;
            owner.invalidateOutline();
        }
    }


    @Override
    public void setShowBorderOnlyBeforeL(boolean showBorderOnlyBeforeL) {
        mIsShowBorderOnlyBeforeL = showBorderOnlyBeforeL;
        invalidate();
    }

    @Override
    public void setShadowElevation(int elevation) {
        if (mShadowElevation == elevation) {
            return;
        }
        mShadowElevation = elevation;
        invalidate();
    }

    @Override
    public void setShadowAlpha(float shadowAlpha) {
        if (mShadowAlpha == shadowAlpha) {
            return;
        }
        mShadowAlpha = shadowAlpha;
        invalidate();
    }

    private void invalidate() {
        if (useFeature()) {
            View owner = mOwner.get();
            if (owner == null) {
                return;
            }
            if (mShadowElevation == 0) {
                owner.setElevation(0);
            } else {
                owner.setElevation(mShadowElevation);
            }
            owner.invalidateOutline();
        }
    }

    @Override
    public void setHideRadiusSide(@HideRadiusSide int hideRadiusSide) {
        if (mHideRadiusSide == hideRadiusSide) {
            return;
        }
        setRadiusAndShadow(mRadius, hideRadiusSide, mShadowElevation, mShadowAlpha);
    }

    @Override
    public int getHideRadiusSide() {
        return mHideRadiusSide;
    }

    @Override
    public void setRadius(int radius) {
        if (mRadius != radius) {
            setRadiusAndShadow(radius, mShadowElevation, mShadowAlpha);
        }
    }

    @Override
    public void setRadius(int radius, @ILayout.HideRadiusSide int hideRadiusSide) {
        if (mRadius == radius && hideRadiusSide == mHideRadiusSide) {
            return;
        }
        setRadiusAndShadow(radius, hideRadiusSide, mShadowElevation, mShadowAlpha);
    }

    @Override
    public int getRadius() {
        return mRadius;
    }

    @Override
    public void setRadiusAndShadow(int radius, int shadowElevation, float shadowAlpha) {
        setRadiusAndShadow(radius, mHideRadiusSide, shadowElevation, shadowAlpha);
    }

    @Override
    public void setRadiusAndShadow(int radius, @ILayout.HideRadiusSide int hideRadiusSide, int shadowElevation, float shadowAlpha) {
        View owner = mOwner.get();
        if (owner == null) {
            return;
        }

        mRadius = radius;
        mHideRadiusSide = hideRadiusSide;

        if (mRadius > 0) {
            if (hideRadiusSide == HIDE_RADIUS_SIDE_TOP) {
                mRadiusArray = new float[]{0, 0, 0, 0, mRadius, mRadius, mRadius, mRadius};
            } else if (hideRadiusSide == HIDE_RADIUS_SIDE_RIGHT) {
                mRadiusArray = new float[]{mRadius, mRadius, 0, 0, 0, 0, mRadius, mRadius};
            } else if (hideRadiusSide == HIDE_RADIUS_SIDE_BOTTOM) {
                mRadiusArray = new float[]{mRadius, mRadius, mRadius, mRadius, 0, 0, 0, 0};
            } else if (hideRadiusSide == HIDE_RADIUS_SIDE_LEFT) {
                mRadiusArray = new float[]{0, 0, mRadius, mRadius, mRadius, mRadius, 0, 0};
            } else {
                mRadiusArray = null;
            }
        }

        mShadowElevation = shadowElevation;
        mShadowAlpha = shadowAlpha;
        if (useFeature()) {
            if (mShadowElevation == 0 || isRadiusWithSideHidden()) {
                owner.setElevation(0);
            } else {
                owner.setElevation(mShadowElevation);
            }

            owner.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                @TargetApi(21)
                public void getOutline(View view, Outline outline) {
                    int w = view.getWidth(), h = view.getHeight();
                    if (w == 0 || h == 0) {
                        return;
                    }
                    if (isRadiusWithSideHidden()) {
                        int left = 0, top = 0, right = w, bottom = h;
                        if (mHideRadiusSide == HIDE_RADIUS_SIDE_LEFT) {
                            left -= mRadius;
                        } else if (mHideRadiusSide == HIDE_RADIUS_SIDE_TOP) {
                            top -= mRadius;
                        } else if (mHideRadiusSide == HIDE_RADIUS_SIDE_RIGHT) {
                            right += mRadius;
                        } else if (mHideRadiusSide == HIDE_RADIUS_SIDE_BOTTOM) {
                            bottom += mRadius;
                        }
                        outline.setRoundRect(left, top,
                                right, bottom, mRadius);
                        return;
                    }

                    int top = mOutlineInsetTop, bottom = Math.max(top + 1, h - mOutlineInsetBottom),
                            left = mOutlineInsetLeft, right = w - mOutlineInsetRight;
                    if (mIsOutlineExcludePadding) {
                        left += view.getPaddingLeft();
                        top += view.getPaddingTop();
                        right = Math.max(left + 1, right - view.getPaddingRight());
                        bottom = Math.max(top + 1, bottom - view.getPaddingBottom());
                    }
                    outline.setAlpha(mShadowAlpha);
                    if (mRadius <= 0) {
                        outline.setRect(left, top,
                                right, bottom);
                    } else {
                        outline.setRoundRect(left, top,
                                right, bottom, mRadius);
                    }
                }
            });

            owner.setClipToOutline(mRadius > 0);

        }
        owner.invalidate();
    }

    /**
     * 有radius, 但是有一边不显示radius。
     */
    public boolean isRadiusWithSideHidden() {
        return mRadius > 0 && mHideRadiusSide != HIDE_RADIUS_SIDE_NONE;
    }

    @Override
    public void updateTopDivider(int topInsetLeft, int topInsetRight, int topDividerHeight, int topDividerColor) {
        mTopDividerInsetLeft = topInsetLeft;
        mTopDividerInsetRight = topInsetRight;
        mTopDividerHeight = topDividerHeight;
        mTopDividerColor = topDividerColor;
    }

    @Override
    public void updateBottomDivider(int bottomInsetLeft, int bottomInsetRight, int bottomDividerHeight, int bottomDividerColor) {
        mBottomDividerInsetLeft = bottomInsetLeft;
        mBottomDividerInsetRight = bottomInsetRight;
        mBottomDividerColor = bottomDividerColor;
        mBottomDividerHeight = bottomDividerHeight;
    }

    @Override
    public void updateLeftDivider(int leftInsetTop, int leftInsetBottom, int leftDividerWidth, int leftDividerColor) {
        mLeftDividerInsetTop = leftInsetTop;
        mLeftDividerInsetBottom = leftInsetBottom;
        mLeftDividerWidth = leftDividerWidth;
        mLeftDividerColor = leftDividerColor;
    }

    @Override
    public void updateRightDivider(int rightInsetTop, int rightInsetBottom, int rightDividerWidth, int rightDividerColor) {
        mRightDividerInsetTop = rightInsetTop;
        mRightDividerInsetBottom = rightInsetBottom;
        mRightDividerWidth = rightDividerWidth;
        mRightDividerColor = rightDividerColor;
    }

    @Override
    public void onlyShowTopDivider(int topInsetLeft, int topInsetRight,
                                   int topDividerHeight, int topDividerColor) {
        updateTopDivider(topInsetLeft, topInsetRight, topDividerHeight, topDividerColor);
        mLeftDividerWidth = 0;
        mRightDividerWidth = 0;
        mBottomDividerHeight = 0;
    }

    @Override
    public void onlyShowBottomDivider(int bottomInsetLeft, int bottomInsetRight,
                                      int bottomDividerHeight, int bottomDividerColor) {
        updateBottomDivider(bottomInsetLeft, bottomInsetRight, bottomDividerHeight, bottomDividerColor);
        mLeftDividerWidth = 0;
        mRightDividerWidth = 0;
        mTopDividerHeight = 0;
    }

    @Override
    public void onlyShowLeftDivider(int leftInsetTop, int leftInsetBottom, int leftDividerWidth, int leftDividerColor) {
        updateLeftDivider(leftInsetTop, leftInsetBottom, leftDividerWidth, leftDividerColor);
        mRightDividerWidth = 0;
        mTopDividerHeight = 0;
        mBottomDividerHeight = 0;
    }

    @Override
    public void onlyShowRightDivider(int rightInsetTop, int rightInsetBottom, int rightDividerWidth, int rightDividerColor) {
        updateRightDivider(rightInsetTop, rightInsetBottom, rightDividerWidth, rightDividerColor);
        mLeftDividerWidth = 0;
        mTopDividerHeight = 0;
        mBottomDividerHeight = 0;
    }

    @Override
    public void setTopDividerAlpha(int dividerAlpha) {
        mTopDividerAlpha = dividerAlpha;
    }

    @Override
    public void setBottomDividerAlpha(int dividerAlpha) {
        mBottomDividerAlpha = dividerAlpha;
    }

    @Override
    public void setLeftDividerAlpha(int dividerAlpha) {
        mLeftDividerAlpha = dividerAlpha;
    }

    @Override
    public void setRightDividerAlpha(int dividerAlpha) {
        mRightDividerAlpha = dividerAlpha;
    }


    public int handleMiniWidth(int widthMeasureSpec, int measuredWidth) {
        return widthMeasureSpec;
    }

    public int handleMiniHeight(int heightMeasureSpec, int measuredHeight) {
        return heightMeasureSpec;
    }

    public int getMeasuredWidthSpec(int widthMeasureSpec) {
        return widthMeasureSpec;
    }

    public int getMeasuredHeightSpec(int heightMeasureSpec) {
        return heightMeasureSpec;
    }

    @Override
    public void setBorderColor(@ColorInt int borderColor) {
        mBorderColor = borderColor;
    }

    @Override
    public void setBorderWidth(int borderWidth) {
        mBorderWidth = borderWidth;
    }

    public void drawDividers(Canvas canvas, int w, int h) {
        if (mDividerPaint == null &&
                (mTopDividerHeight > 0 || mBottomDividerHeight > 0 || mLeftDividerWidth > 0 || mRightDividerWidth > 0)) {
            mDividerPaint = new Paint();
        }
        if (mTopDividerHeight > 0) {
            mDividerPaint.setStrokeWidth(mTopDividerHeight);
            mDividerPaint.setColor(mTopDividerColor);
            if (mTopDividerAlpha < 255) {
                mDividerPaint.setAlpha(mTopDividerAlpha);
            }
            float y = mTopDividerHeight * 1f / 2;
            canvas.drawLine(mTopDividerInsetLeft, y, w - mTopDividerInsetRight, y, mDividerPaint);
        }

        if (mBottomDividerHeight > 0) {
            mDividerPaint.setStrokeWidth(mBottomDividerHeight);
            mDividerPaint.setColor(mBottomDividerColor);
            if (mTopDividerAlpha < 255) {
                mDividerPaint.setAlpha(mBottomDividerAlpha);
            }
            float y = (float) Math.floor(h - mBottomDividerHeight * 1f / 2);
            canvas.drawLine(mBottomDividerInsetLeft, y, w - mBottomDividerInsetRight, y, mDividerPaint);
        }

        if (mLeftDividerWidth > 0) {
            mDividerPaint.setStrokeWidth(mLeftDividerWidth);
            mDividerPaint.setColor(mLeftDividerColor);
            if (mLeftDividerAlpha < 255) {
                mDividerPaint.setAlpha(mLeftDividerAlpha);
            }
            canvas.drawLine(0, mLeftDividerInsetTop, 0, h - mLeftDividerInsetBottom, mDividerPaint);
        }

        if (mRightDividerWidth > 0) {
            mDividerPaint.setStrokeWidth(mRightDividerWidth);
            mDividerPaint.setColor(mRightDividerColor);
            if (mRightDividerAlpha < 255) {
                mDividerPaint.setAlpha(mRightDividerAlpha);
            }
            canvas.drawLine(w, mRightDividerInsetTop, w, h - mRightDividerInsetBottom, mDividerPaint);
        }
    }


    public void dispatchRoundBorderDraw(Canvas canvas) {
        View owner = mOwner.get();
        if (owner == null) {
            return;
        }
        if (mBorderColor == 0 && (mRadius == 0 || mOuterNormalColor == 0)) {
            return;
        }

        if (mIsShowBorderOnlyBeforeL && useFeature() && mShadowElevation != 0) {
            return;
        }

        int width = canvas.getWidth(), height = canvas.getHeight();

        // react
        if (mIsOutlineExcludePadding) {
            mBorderRect.set(1 + owner.getPaddingLeft(), 1 + owner.getPaddingTop(),
                    width - 1 - owner.getPaddingRight(), height - 1 - owner.getPaddingBottom());
        } else {
            mBorderRect.set(1, 1, width - 1, height - 1);
        }

        if (mRadius == 0 || (!useFeature() && mOuterNormalColor == 0)) {
            mClipPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(mBorderRect, mClipPaint);
            return;
        }

        // 圆角矩形
        if (!useFeature()) {
            int layerId = canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
            canvas.drawColor(mOuterNormalColor);
            mClipPaint.setColor(mOuterNormalColor);
            mClipPaint.setStyle(Paint.Style.FILL);
            mClipPaint.setXfermode(mMode);
            if (mRadiusArray == null) {
                canvas.drawRoundRect(mBorderRect, mRadius, mRadius, mClipPaint);
            } else {
                drawRoundRect(canvas, mBorderRect, mRadiusArray, mClipPaint);
            }
            mClipPaint.setXfermode(null);
            canvas.restoreToCount(layerId);
        }

        mClipPaint.setColor(mBorderColor);
        mClipPaint.setStrokeWidth(mBorderWidth);
        mClipPaint.setStyle(Paint.Style.STROKE);
        if (mRadiusArray == null) {
            canvas.drawRoundRect(mBorderRect, mRadius, mRadius, mClipPaint);
        } else {
            drawRoundRect(canvas, mBorderRect, mRadiusArray, mClipPaint);
        }
    }

    private void drawRoundRect(Canvas canvas, RectF rect, float[] radiusArray, Paint paint) {
        mPath.reset();
        mPath.addRoundRect(rect, radiusArray, Path.Direction.CW);
        canvas.drawPath(mPath, paint);

    }

    public void setOuterNormalColor(int color) {
        mOuterNormalColor = color;
        View owner = mOwner.get();
        if (owner != null) {
            owner.invalidate();
        }
    }

    public static boolean useFeature() {
        return Build.VERSION.SDK_INT >= 21;
    }
}
