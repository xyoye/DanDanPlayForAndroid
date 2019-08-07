package com.xyoye.dandanplay.ui.weight;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

/**
 * Created by xyoye on 2019/8/7.
 */

public class ExpandableLayout extends LinearLayout {
    private View contentLayout;
    private int contentHeight;
    private int rootHeight;

    //是否已获取到内容高度
    private boolean isInit = false;
    //是否已展开
    private boolean isOpened = false;
    //是否正在执行动画
    private boolean isUpdating = false;

    public ExpandableLayout(Context context) {
        super(context);
    }

    public ExpandableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //子View只能有头布局和内容布局
        if (getChildCount() != 2)
            throw new IllegalArgumentException("child view count are "+getChildCount()+" must be 2");

        //默认高度为头布局高度
        rootHeight = getChildAt(0).getMeasuredHeight();

        contentLayout = getChildAt(1);

        if (!isInit){
            isInit = true;
            //计算初始内容布局高度
            contentHeight = contentLayout.getMeasuredHeight();
            //改变root布局高度
            ViewGroup.LayoutParams rootParams = this.getLayoutParams();
            rootParams.height = rootHeight;
            setLayoutParams(rootParams);
        }
    }

    public void show() {
        if (contentLayout == null || isUpdating || isOpened)
            return;
        doAnime(0, contentHeight);
    }

    public void hide() {
        if (contentLayout == null || isUpdating || !isOpened)
            return;
        doAnime(contentHeight, 0);
    }

    private void doAnime(int startHeight, int endHeight) {
        isOpened = endHeight > startHeight;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startHeight, endHeight).setDuration(300);
        valueAnimator.addUpdateListener(valueAnimator1 -> {
            int height = (int) valueAnimator1.getAnimatedValue();

            //改变root布局高度
            ViewGroup.LayoutParams rootParams = this.getLayoutParams();
            rootParams.height = rootHeight + height;
            setLayoutParams(rootParams);

            isUpdating = height != endHeight;
        });
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.start();
    }
}
