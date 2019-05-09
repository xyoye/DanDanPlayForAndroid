package com.player.ijkplayer.widgets;

import android.content.Context;
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

    private SkipTipListener listener;

    public SkipTipView(Context context) {
        super(context, null);
    }

    public SkipTipView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.layout_skip_tip_v2, this);

        skipTimeTv = findViewById(R.id.tv_skip_time);

        findViewById(R.id.iv_cancel_skip).setOnClickListener(this);
        findViewById(R.id.tv_do_skip).setOnClickListener(this);
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
