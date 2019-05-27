package com.player.commom.widgets;

import android.content.Context;
import android.util.AttributeSet;

/**
 * 跑马灯TextView
 *
 * Created by xyoye on 2019/5/9
 */
public class MarqueeTextView extends android.support.v7.widget.AppCompatTextView {

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
