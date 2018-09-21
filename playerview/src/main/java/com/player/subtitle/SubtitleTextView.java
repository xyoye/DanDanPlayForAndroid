package com.player.subtitle;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 显示字幕的字体样式
 */

public class SubtitleTextView extends AppCompatTextView implements View.OnTouchListener {
    private Context context;

    private SubtitleClickListener listener;

    public SubtitleTextView(Context context) {
        super(context);
        init(context);
    }

    public SubtitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SubtitleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        // 默认白色字体
        setTextColor(Color.WHITE);
        setSingleLine(true);
        setShadowLayer(3, 0, 0, Color.BLUE);
        this.setOnTouchListener(this);
    }

    public void setSubtitleOnTouchListener(SubtitleClickListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (listener != null)
                    listener.ClickDown();
                break;
            case MotionEvent.ACTION_UP:
                if (listener != null)
                    listener.ClickUp();
                break;
        }
        return true;
    }
}

/**
 * 对字幕进行监听的接口
 */
interface SubtitleClickListener {
    /**
     * 按下
     */
    void ClickDown();

    /**
     * 取消
     */
    void ClickUp();
}
