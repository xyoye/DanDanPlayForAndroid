package com.xyoye.dandanplay.utils.view;

import android.os.Build;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.view.View;
import android.view.ViewGroup;

public class WindowUtils {

    private final static InsetsListener topInsetsListener = (view, padding, margin, insets) -> {
        view.setPadding(view.getPaddingLeft(),padding.top + insets.getSystemWindowInsetTop(),view.getPaddingRight(),view.getPaddingBottom());
        return insets;
    };

    private final static InsetsListener bottomInsetsListener = (view, padding, margin, insets) -> {
        view.setPadding(view.getPaddingLeft(),view.getPaddingTop(),view.getPaddingRight(),padding.bottom + insets.getSystemWindowInsetBottom());
        return insets;
    };

    public static void fitWindowInsetsTop(View view) {
        doOnApplyWindowInsets(view, topInsetsListener);
    }

    public static void fitWindowInsetsBottom(View view) {
        doOnApplyWindowInsets(view, bottomInsetsListener);
    }

    public static void requestApplyInsetsWhenAttached(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewUtils.doOnAttach(view, View::requestApplyInsets);
        }
    }


    public static void doOnApplyWindowInsets(View view, InsetsListener listener) {
        Padding padding = new Padding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
        Padding margin;
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = ((ViewGroup.MarginLayoutParams) layoutParams);
            margin = new Padding(marginLayoutParams.leftMargin, marginLayoutParams.topMargin, marginLayoutParams.rightMargin, marginLayoutParams.bottomMargin);
        } else {
            margin = new Padding(0, 0, 0, 0);
        }
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsetsCompat) -> listener.onApplyWindowInsets(v, padding, margin, windowInsetsCompat));
    }

    public interface InsetsListener {
        WindowInsetsCompat onApplyWindowInsets(View view, Padding padding, Padding margin, WindowInsetsCompat insets);
    }

    public static class Padding {
        private int left;
        private int right;
        private int top;
        private int bottom;

        public Padding(int left, int top, int right, int bottom) {
            setBottom(bottom);
            setRight(right);
            setTop(top);
            setLeft(left);
        }

        public int getBottom() {
            return bottom;
        }

        public void setBottom(int bottom) {
            this.bottom = bottom;
        }

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getRight() {
            return right;
        }

        public void setRight(int right) {
            this.right = right;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }
    }

}
