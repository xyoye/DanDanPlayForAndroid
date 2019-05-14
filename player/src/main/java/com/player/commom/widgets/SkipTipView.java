package com.player.commom.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.player.ijkplayer.R;

/**
 * Created by xyoye on 2019/5/6.
 */

public class SkipTipView extends LinearLayout implements View.OnClickListener{
    private TextView skipTimeTv;
    private TextView skipContentTv;
    private TextView skipConfirmTv;

    private SkipTipListener listener;
    private boolean skipSubtitle;

    public SkipTipView(Context context) {
        super(context, null);
    }

    public SkipTipView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkipTipView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        View.inflate(context, R.layout.layout_skip_tip_v2, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SkipTipView, defStyle, 0);
        skipSubtitle = typedArray.getBoolean(R.styleable.SkipTipView_skip_subtitle, false);
        typedArray.recycle();

        skipTimeTv = findViewById(R.id.tv_skip_time);
        skipContentTv = findViewById(R.id.skip_content_tv);
        skipConfirmTv = findViewById(R.id.tv_do_skip);

        skipConfirmTv.setOnClickListener(this);
        findViewById(R.id.iv_cancel_skip).setOnClickListener(this);

        if (skipSubtitle){
            skipConfirmTv.setText("前往加载");
            skipConfirmTv.setTextColor(getResources().getColor(R.color.theme_color));
        }
    }

    @SuppressLint("SetTextI18n")
    public void setSkipContent(int subtitleSize){
        if (skipSubtitle){
            skipContentTv.setText("匹配到"+subtitleSize+"条在线字幕");
        }
    }

    @Override
    public void onClick(View v) {
        if (listener != null){
            int id = v.getId();
            if (id == R.id.iv_cancel_skip){
                listener.onCancel();
            }else if (id == R.id.tv_do_skip){
                listener.onSkip();
            }
        }
    }

    public void setSkipTime(String time){
        skipTimeTv.setText(time);
    }

    public void setCallBack(SkipTipListener listener){
        this.listener = listener;
    }

    public interface SkipTipListener{
        void onCancel();

        void onSkip();
    }
}
