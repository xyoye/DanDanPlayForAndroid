package com.xyoye.dandanplay.ui.weight;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 有颜色分割线
 *
 * Created by xyoye on 2019/3/26.
 */

public class ItemDecorationDivider extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    private int mCount;
    private int mLineWidth;

    public ItemDecorationDivider(int dividerWidthPx, @ColorInt int dividerColor, int spanCount) {
        GradientDrawable shapeDrawable = new GradientDrawable();
        shapeDrawable.setColor(dividerColor);
        shapeDrawable.setShape(GradientDrawable.RECTANGLE);
        shapeDrawable.setSize(dividerWidthPx, dividerWidthPx);

        this.mCount = spanCount;
        this.mLineWidth = dividerWidthPx;
        this.mDivider = shapeDrawable;
    }
    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        drawHorizontal(c, parent);
        drawVertical(c, parent);

    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getLeft() - params.leftMargin - mDivider.getIntrinsicWidth();
            final int right = child.getRight() + params.rightMargin + mDivider.getIntrinsicWidth();
            int top;
            int bottom;

            if ((i / mCount) == 0) {
                //画item最上面的分割线
                top = 0;
                bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
                //画item下面的分割线
                top = child.getBottom() + params.bottomMargin;
                bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            } else {
                //画item下面的分割线
                top = child.getBottom() + params.bottomMargin;
                bottom = top + mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }

        }
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);

            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getTop() - params.topMargin;
            final int bottom = child.getBottom() + params.bottomMargin;
            int left;
            int right;

            if ((i % mCount) == 0) {
                //item左边分割线
                left = 0;
                right = left + mDivider.getIntrinsicWidth();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
                //item右边分割线
                left = child.getRight() + params.rightMargin;
                right = left + mDivider.getIntrinsicWidth();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            } else {
                left = child.getRight() + params.rightMargin;
                right = left + mDivider.getIntrinsicWidth();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        //当前position
        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();

        //position从0计算，所以最后一列为 mCount - 1
        int endCol = mCount - 1;

        int eachWidth = endCol * mLineWidth / mCount;

        //默认顶部不画分割线
        int top = 0;

        //设mCount = 4, 设lineWidth = 1
        // l 第一列 r     第二列     第三列     第四列
        //     -   3   1  _  2   2  _  1   3  _
        // 0  | |  -   - | | -   - | | -   - | | 0
        //     -   4   4  -  4   4  -  4   4  -
        int left = itemPosition % mCount * (mLineWidth - eachWidth);
        int right = eachWidth - left;

        //每一项底部画分割线
        int bottom = mLineWidth;

        //第一行顶部画分割线
        if (itemPosition <= endCol){
            top = mLineWidth;
        }

        //第一列左边画分割线
        if (itemPosition % mCount == 0){
            left = mLineWidth;
        }

        //最后一列右边画分割线
        if (itemPosition % mCount == endCol){
            right = mLineWidth;
        }

        outRect.set(left, top, right, bottom);
    }
}
