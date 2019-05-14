package com.player.commom.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
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
    private TextView numberNoLimitTv, numberAutoLimitTv;
    private EditText numberInputLimitEt;
    //是否开启云屏蔽
    private boolean isOpenCloudFilter = false;
    //弹幕文字大小
    private float mDanmuTextSize;
    //弹幕文字大小
    private float mDanmuTextAlpha;
    //弹幕速度大小
    private float mDanmuSpeed;
    //弹幕同屏数量
    private int mDanmuNumberLimit;
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
        numberNoLimitTv = findViewById(R.id.number_no_limit_tv);
        numberAutoLimitTv = findViewById(R.id.number_auto_limit_tv);
        numberInputLimitEt = findViewById(R.id.number_input_limit_et);

        mDanmuMobileIv.setOnClickListener(this);
        mDanmuTopIv.setOnClickListener(this);
        mDanmuBottomIv.setOnClickListener(this);
        mMoreBlockRl.setOnClickListener(this);
        addDanmuExtraTimeTv.setOnClickListener(this);
        reduceDanmuExtraTimeTv.setOnClickListener(this);
        numberNoLimitTv.setOnClickListener(this);
        numberAutoLimitTv.setOnClickListener(this);

        danmuExtraTimeEt.setImeOptions(EditorInfo.IME_ACTION_DONE);
        danmuExtraTimeEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        danmuExtraTimeEt.setSingleLine(true);
        numberInputLimitEt.setImeOptions(EditorInfo.IME_ACTION_DONE);
        numberInputLimitEt.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        numberInputLimitEt.setSingleLine(true);
    }

    public SettingDanmuView setListener(SettingDanmuListener settingListener){
        this.settingListener = settingListener;
        return this;
    }

    //初始弹幕文字大小
    @SuppressLint("SetTextI18n")
    public SettingDanmuView setDanmuSize(int progressSize){
        mDanmuSizeSb.setMax(100);
        float calcProgressSize = (float) progressSize;
        mDanmuTextSize = calcProgressSize/50;
        mDanmuSizeTv.setText(progressSize + "%");
        mDanmuSizeSb.setProgress(progressSize);
        return this;
    }

    //初始弹幕速度
    @SuppressLint("SetTextI18n")
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
    @SuppressLint("SetTextI18n")
    public SettingDanmuView setDanmuAlpha(int progressAlpha){
        mDanmuAlphaSb.setMax(100);
        float calcProgressAlpha = (float) progressAlpha;
        mDanmuTextAlpha = calcProgressAlpha/100;
        mDanmuAlphaTv.setText(progressAlpha + "%");
        mDanmuAlphaSb.setProgress(progressAlpha);
        return this;
    }

    //初始同屏数量
    public SettingDanmuView setDanmuNumberLimit(int num){
        setLimitSize(num);
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

    //初始云屏蔽状态
    public void setCloudFilterStatus(boolean isOpenCloudFilter){
        this.isOpenCloudFilter = isOpenCloudFilter;
        mDanmuCloudFilter.setChecked(isOpenCloudFilter);
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

        danmuExtraTimeEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE){
                try {
                    String extraTime = danmuExtraTimeEt.getText().toString().trim();
                    float extraTimeLong = Float.valueOf(extraTime);
                    settingListener.setExtraTime((int)(extraTimeLong * 1000));
                    danmuExtraTime = extraTimeLong;
                }catch (Exception e){
                    ToastUtils.showShort("请输入正确的时间");
                    return true;
                }
                danmuExtraTimeEt.clearFocus();
                return false;
            }
            return false;
        });

        numberInputLimitEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE){
                try {
                    String numText = numberInputLimitEt.getText().toString().trim();
                    int num = Integer.valueOf(numText);
                    if (num < 1){
                        ToastUtils.showShort("同屏数量不能小于1");
                        return true;
                    }
                    setLimitSize(num);
                    settingListener.setNumberLimit(num);
                }catch (Exception e){
                    ToastUtils.showShort("请输入正确的数量");
                    return true;
                }
                numberInputLimitEt.clearFocus();
                return false;
            }
            return false;
        });

        mDanmuCloudFilter.setOnCheckedChangeListener((buttonView, isChecked) ->
                settingListener.setCloudFilter(isChecked));

        this.setOnTouchListener((v, event) -> true);

        return this;
    }

    /**
     * 设置语言类型
     */
    @SuppressLint("SetTextI18n")
    private void setLimitSize(int num){
        mDanmuNumberLimit = num;
        switch (num){
            case 0:
                numberNoLimitTv.setBackgroundColor(Color.parseColor("#33ffffff"));
                numberAutoLimitTv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                numberInputLimitEt.setText("");
                break;
            case -1:
                numberNoLimitTv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                numberAutoLimitTv.setBackgroundColor(Color.parseColor("#33ffffff"));
                numberInputLimitEt.setText("");
                break;
            default:
                numberNoLimitTv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                numberAutoLimitTv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                numberInputLimitEt.setText(num+"");
                break;
        }
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
        }else if (id == R.id.number_auto_limit_tv){
            setLimitSize(-1);
            settingListener.setNumberLimit(-1);
        }else if (id == R.id.number_no_limit_tv){
            setLimitSize(0);
            settingListener.setNumberLimit(0);
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

    public int getDanmuNumberLimit() {
        return mDanmuNumberLimit;
    }


    public interface SettingDanmuListener{
        void setDanmuSize(int progress);
        void setDanmuSpeed(int progress);
        void setDanmuAlpha(int progress);
        void setExtraTime(int timeMs);
        void setCloudFilter(boolean isChecked);
        void setDanmuMobEnable(boolean enable);
        void setDanmuTopEnable(boolean enable);
        void setDanmuBotEnable(boolean enable);
        void setBlockViewShow();
        void setExtraTimeAdd(int time);
        void setExtraTimeReduce(int time);
        void setNumberLimit(int num);
    }
}
