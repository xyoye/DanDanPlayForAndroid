package com.player.commom.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by xyoye on 2018/7/1.
 */

public final class AnimHelper {

    private AnimHelper() {
        throw new AssertionError();
    }


    /**
     * 执行滑动动画
     * @param view
     * @param startX
     * @param endX
     * @param duration
     */
    public static void doSlide(View view, int startX, int endX, int duration) {
        ObjectAnimator translationX = ObjectAnimator.ofFloat(view, "translationX", startX, endX);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(duration);
        set.playTogether(translationX, alpha);
        set.start();
    }

    /**
     * 执行展示动画
     * @param view
     */
    public static void doShowAnimator(final View view){
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
                super.onAnimationStart(animation);
            }
        });
        set.setDuration(250);
        set.playTogether(alpha);
        set.start();
    }

    /**
     * 执行隐藏动画
     * @param view
     */
    public static void doHideAnimator(final View view){
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1, 0);
        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
                super.onAnimationEnd(animation);
            }
        });
        set.setDuration(250);
        set.playTogether(alpha);
        set.start();
    }

    /**
     * 裁剪视图宽度
     * @param view
     * @param srcWidth
     * @param endWidth
     * @param duration
     */
    public static void doClipViewWidth(final View view, int srcWidth, int endWidth, int duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(srcWidth, endWidth).setDuration(duration);
        valueAnimator.addUpdateListener(valueAnimator1 -> {
            int width = (int) valueAnimator1.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = width;
            view.setLayoutParams(layoutParams);
        });
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.start();
    }

    /**
     * 裁剪视图宽度
     * @param view
     * @param srcHeight
     * @param endHeight 大概高度，用于动画展示
     * @param duration
     */
    public static void doClipViewHeight(final View view, int srcHeight, int endHeight, int duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(srcHeight, endHeight).setDuration(duration);
        valueAnimator.addUpdateListener(valueAnimator1 -> {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.setLayoutParams(layoutParams);
        });
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.start();
    }

    /**
     * 横向位移动画
     */
    public static void viewTranslationX(View view){
        viewTranslationX(view, view.getWidth());
    }

    /**
     * 横向位移动画
     * @param transX 位移距离
     */
    public static void viewTranslationX(View view, int transX){
        if (view.getVisibility() == View.GONE)
            view.setVisibility(View.VISIBLE);
        ViewCompat.animate(view).translationX(transX).setDuration(500);
    }

    /**
     * 横向位移动画
     * @param transX 位移距离
     * @param duration 位移时间
     */
    public static void viewTranslationX(View view, int transX, long duration){
        if (view.getVisibility() == View.GONE)
            view.setVisibility(View.VISIBLE);
        ViewCompat.animate(view).translationX(transX).setDuration(duration);
    }

    /**
     * 纵向位移动画
     */
    public static void viewTranslationY(View view){
        viewTranslationY(view, view.getHeight());
    }

    /**
     * 纵向位移动画
     * @param transY 位移距离
     */
    public static void viewTranslationY(View view, int transY){
        if (view.getVisibility() == View.GONE)
            view.setVisibility(View.VISIBLE);
        ViewCompat.animate(view).translationY(transY).setDuration(300);
    }
}
