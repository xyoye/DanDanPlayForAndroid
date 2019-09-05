package com.xyoye.player.commom.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.xyoye.player.R;

/**
 * Created by xyoye on 2019/5/6.
 */

public class BottomBarView extends LinearLayout implements View.OnClickListener{
    // 播放键
    private ImageView mIvPlay;
    // 当前时间
    private TextView mTvCurTime;
    // 结束时间
    private TextView mTvEndTime;
    // 弹幕显示/隐藏按钮
    private ImageView mIvDanmakuControl;
    // 播放进度条
    private SeekBar mDanmakuPlayerSeek;

    private BottomBarListener listener;

    public BottomBarView(Context context) {
        this(context, null);
    }

    public BottomBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.layout_bottom_bar_v2, this);

        mIvPlay = findViewById(R.id.iv_play);
        mTvCurTime = findViewById(R.id.tv_cur_time);
        mTvEndTime = findViewById(R.id.tv_end_time);
        mIvDanmakuControl = findViewById(R.id.iv_danmaku_control);
        mDanmakuPlayerSeek = findViewById(R.id.danmaku_player_seek);

        mIvPlay.setOnClickListener(this);
        mIvDanmakuControl.setOnClickListener(this);
        findViewById(R.id.tv_open_edit_danmaku).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (listener != null){
            int id = v.getId();
            if (id == R.id.iv_play){
                listener.onPlayClick();
            }else if (id == R.id.iv_danmaku_control){
                listener.onDanmuCtrlClick();
            }else if (id == R.id.tv_open_edit_danmaku){
                listener.onDanmuSendClick();
            }
        }
    }

    public void setPlayIvStatus(boolean isPlaying){
        mIvPlay.setSelected(isPlaying);
    }

    public void setDanmuIvStatus(boolean isShow){
        mIvDanmakuControl.setSelected(isShow);
    }

    public boolean getDanmuIvStatus(){
        return mIvDanmakuControl.isSelected();
    }

    public void setCurTime(String curTime){
        mTvCurTime.setText(curTime);
    }

    public void setEndTime(String endTime){
        mTvEndTime.setText(endTime);
    }

    public void setSeekMax(int max){
        mDanmakuPlayerSeek.setMax(max);
    }

    public void setSeekProgress(int progress){
        mDanmakuPlayerSeek.setProgress(progress);
    }

    public void setSeekSecondaryProgress(int progress){
        mDanmakuPlayerSeek.setSecondaryProgress(progress);
    }

    public void setSeekCallBack(SeekBar.OnSeekBarChangeListener seekCallBack){
        mDanmakuPlayerSeek.setOnSeekBarChangeListener(seekCallBack);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setSeekBarTouchCallBack(OnTouchListener touchListener){
        mDanmakuPlayerSeek.setOnTouchListener(touchListener);
    }

    public void setCallBack(BottomBarListener listener){
        this.listener = listener;
    }

    public interface BottomBarListener{
        void onPlayClick();

        void onDanmuCtrlClick();

        void onDanmuSendClick();
    }
}
