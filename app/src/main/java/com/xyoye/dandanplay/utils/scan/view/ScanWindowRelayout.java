package com.xyoye.dandanplay.utils.scan.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by xyoye on 2019/10/23.
 * 扫描窗口背景Relayout
 */

public class ScanWindowRelayout extends RelativeLayout {

    private Rect windowRect;

    @SuppressLint("CustomViewStyleable")
    public ScanWindowRelayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanWindowRelayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        //绑定子view中找到的第一个ScanWindowView
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof ScanWindowView){
                bindWindow(view);
                break;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //清空扫描窗口位置背景颜色
        if (windowRect != null) {
            canvas.save();
            canvas.clipRect(windowRect);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            canvas.restore();
        }
    }

    /**
     * 绑定扫描窗口
     * 获取扫描窗口的左边距，上边距，宽，高
     */
    private void bindWindow(@NonNull View view){
        int[] location = new int[2];
        view.getLocationInWindow(location);

        windowRect = new Rect();
        windowRect.set(
                location[0],
                location[1],
                location[0] + view.getMeasuredWidth(),
                location[1] + view.getMeasuredHeight()
        );
    }
}
