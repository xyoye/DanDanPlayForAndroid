package com.player.commom.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.player.ijkplayer.R;

/**
 * Created by xyy on 2019/2/21.
 */

public class SettingDanmuView extends LinearLayout implements View.OnClickListener{
    //弹幕设置相关组件
    private SeekBar mDanmuSizeSb;
    private TextView mDanmuSizeTv;
    private SeekBar mDanmuSpeedSb;
    private TextView mDanmuSpeedTv;
    private SeekBar mDanmuAlphaSb;
    private TextView mDanmuAlphaTv;
    private ImageView mDanmuMobileIv, mDanmuTopIv, mDanmuBottomIv;
    private RelativeLayout mMoreBlockRl;
    private TextView addDanmuExtraTimeTv, reduceDanmuExtraTimeTv;
    private EditText danmuExtraTimeEt;
    private Switch mDanmuCloudFilter;
    //是否开启云屏蔽
    private boolean isOpenCloudFilter = false;
    //弹幕文字大小
    private float mDanmuTextSize;
    //弹幕文字大小
    private float mDanmuTextAlpha;
    //弹幕速度大小
    private float mDanmuSpeed;
    //弹幕屏蔽获取
    private boolean isShowTop = true;
    private boolean isShowMobile = true;
    private boolean isShowBottom = true;
    //弹幕时间偏移
    private float danmuExtraTime;
    //偏移时间量（单次调节偏移量）
    private float offsetTime = 0.5f;

    //控制回调
    private SettingDanmuListener settingListener = null;

    public SettingDanmuView(Context context) {
        this(context, null);
    }

    public SettingDanmuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_setting_danmu, this);
        //弹幕设置相关
        mDanmuSizeTv = findViewById(R.id.danmu_size_tv);
        mDanmuSizeSb = findViewById(R.id.danmu_size_sb);
        mDanmuSpeedTv = findViewById(R.id.danmu_speed_tv);
        mDanmuSpeedSb = findViewById(R.id.danmu_speed_sb);
        mDanmuAlphaTv = findViewById(R.id.danmu_alpha_tv);
        mDanmuAlphaSb = findViewById(R.id.danmu_alpha_sb);
        mDanmuMobileIv = findViewById(R.id.mobile_danmu_iv);
        mDanmuTopIv = findViewById(R.id.top_danmu_iv);
        mDanmuBottomIv = findViewById(R.id.bottom_danmu_iv);
        mMoreBlockRl = findViewById(R.id.more_block_rl);
        addDanmuExtraTimeTv = findViewById(R.id.danmu_extra_time_add);
        reduceDanmuExtraTimeTv = findViewById(R.id.danmu_extra_time_reduce);
        danmuExtraTimeEt = findViewById(R.id.danmu_extra_time_et);
        mDanmuCloudFilter = findViewById(R.id.cloud_filter_sw);
        mDanmuCloudFilter.setChecked(isOpenCloudFilter);

        mDanmuMobileIv.setOnClickListener(this);
        mDanmuTopIv.setOnClickListener(this);
        mDanmuBottomIv.setOnClickListener(this);
        mMoreBlockRl.setOnClickListener(this);
        addDanmuExtraTimeTv.setOnClickListener(this);
        reduceDanmuExtraTimeTv.setOnClickListener(this);

        danmuExtraTimeEt.setImeOptions(EditorInfo.IME_ACTION_DONE);
        danmuExtraTimeEt.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        danmuExtraTimeEt.setSingleLine(true);
    }

    public SettingDanmuView setListener(SettingDanmuListener settingListener){
        this.settingListener = settingListener;
        return this;
    }

    //初始弹幕文字大小
    public SettingDanmuView setDanmuSize(int progressSize){
        mDanmuSizeSb.setMax(100);
        float calcProgressSize = (float) progressSize;
        mDanmuTextSize = calcProgressSize/50;
        mDanmuSizeTv.setText(progressSize + "%");
        mDanmuSizeSb.setProgress(progressSize);
        return this;
    }

    //初始弹幕速度
    public SettingDanmuView setDanmuSpeed(int progressSpeed){
        mDanmuSpeedSb.setMax(100);
        float calcProgressSpeed = (float) progressSpeed;
        mDanmuSpeed = calcProgressSpeed/40 > 2.4f
                ? 2.4f
                : calcProgressSpeed/40 ;
        mDanmuSpeedTv.setText(progressSpeed + "%");
        mDanmuSpeedSb.setProgress(progressSpeed);
        return this;
    }

    //初始弹幕文字透明度
    public SettingDanmuView setDanmuAlpha(int progressAlpha){
        mDanmuAlphaSb.setMax(100);
        float calcProgressAlpha = (float) progressAlpha;
        mDanmuTextAlpha = calcProgressAlpha/100;
        mDanmuAlphaTv.setText(progressAlpha + "%");
        mDanmuAlphaSb.setProgress(progressAlpha);
        return this;
    }

    //初始屏蔽状态
    public SettingDanmuView setBlockEnable(boolean mob, boolean top, boolean bot){
        isShowMobile = mob;
        isShowTop = top;
        isShowBottom = bot;
        if (isShowMobile) mDanmuMobileIv.setImageResource(R.mipmap.ic_mobile_unselect);
        if (isShowTop) mDanmuTopIv.setImageResource(R.mipmap.ic_top_unselect);
        if (isShowBottom) mDanmuBottomIv.setImageResource(R.mipmap.ic_bottom_unselect);
        return this;
    }

    //初始弹幕偏移量
    public SettingDanmuView setOffsetTime(float offsetTime){
        this.offsetTime = offsetTime;
        return this;
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    public SettingDanmuView init(){
        if (settingListener == null)
            throw new NullPointerException("must initialize DanmuSettingListener（必须初始化DanmuSettingListener）");
        mDanmuSizeSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0 ) progress = 1;
                mDanmuSizeTv.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress == 0 ) progress = 1;
                settingListener.setDanmuSize(progress);
            }
        });

        mDanmuSpeedSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0 ) progress = 1;
                mDanmuSpeedTv.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress == 0 ) progress = 1;
                settingListener.setDanmuSpeed(progress);
            }
        });

        mDanmuAlphaSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0 ) progress = 1;
                mDanmuAlphaTv.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress == 0 ) progress = 1;
                settingListener.setDanmuAlpha(progress);
            }
        });

        danmuExtraTimeEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    try {
                        String extraTime = danmuExtraTimeEt.getText().toString().trim();
                        int extraTimeLong = Integer.valueOf(extraTime);
                        settingListener.setExtraTime((int)((danmuExtraTime- extraTimeLong) * 1000));
                        danmuExtraTime = extraTimeLong;
                    }catch (Exception e){
                        Toast.makeText(getContext(), "请输入正确的时间", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                return false;
            }
        });

        mDanmuCloudFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingListener.setCloudFilter(isChecked);
            }
        });

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        return this;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.mobile_danmu_iv){
            isShowMobile = !isShowMobile;
            mDanmuMobileIv.setImageResource(isShowMobile
                    ? R.mipmap.ic_mobile_unselect
                    : R.mipmap.ic_mobile_select);
            settingListener.setDanmuMobEnable(isShowMobile);
        }else if (id == R.id.top_danmu_iv){
            isShowTop = !isShowTop;
            mDanmuTopIv.setImageResource(isShowTop
                    ? R.mipmap.ic_top_unselect
                    : R.mipmap.ic_top_select);
            settingListener.setDanmuTopEnable(isShowTop);
        }else if (id == R.id.bottom_danmu_iv){
            isShowBottom = !isShowBottom;
            mDanmuBottomIv.setImageResource(isShowBottom
                    ? R.mipmap.ic_bottom_unselect
                    : R.mipmap.ic_bottom_select);
            settingListener.setDanmuBotEnable(isShowBottom);
        }else if (id == R.id.more_block_rl){
            settingListener.setBlockViewShow();
        }else if (id == R.id.danmu_extra_time_add){
            settingListener.setExtraTimeAdd((int)(offsetTime * 1000));
            danmuExtraTime += offsetTime;
            danmuExtraTimeEt.setText(String.valueOf(danmuExtraTime));
        }else if (id == R.id.danmu_extra_time_reduce){
            settingListener.setExtraTimeReduce((int)(offsetTime * 1000));
            danmuExtraTime -= offsetTime;
            danmuExtraTimeEt.setText(String.valueOf(danmuExtraTime));
        }
    }

    public int getDanmuExtraTime(){
        return (int)(danmuExtraTime * 1000);
    }

    public boolean isOpenCloudFilter() {
        return isOpenCloudFilter;
    }

    public float getmDanmuTextSize() {
        return mDanmuTextSize;
    }

    public float getmDanmuTextAlpha() {
        return mDanmuTextAlpha;
    }

    public float getmDanmuSpeed() {
        return mDanmuSpeed;
    }

    public boolean isShowTop() {
        return isShowTop;
    }

    public boolean isShowMobile() {
        return isShowMobile;
    }

    public boolean isShowBottom() {
        return isShowBottom;
    }

    public float getOffsetTime() {
        return offsetTime;
    }

    public interface SettingDanmuListener{
        void setDanmuSize(int progress);
        void setDanmuSpeed(int progress);
        void setDanmuAlpha(int progress);
        void setExtraTime(int time);
        void setCloudFilter(boolean isChecked);
        void setDanmuMobEnable(boolean enable);
        void setDanmuTopEnable(boolean enable);
        void setDanmuBotEnable(boolean enable);
        void setBlockViewShow();
        void setExtraTimeAdd(int time);
        void setExtraTimeReduce(int time);
    }
}
