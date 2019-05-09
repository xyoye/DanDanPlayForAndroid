package com.player.ijkplayer.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.player.danmaku.danmaku.model.BaseDanmaku;
import com.player.ijkplayer.R;
import com.player.ijkplayer.utils.AnimHelper;
import com.player.ijkplayer.utils.SoftInputUtils;

/**
 * Created by xyoye on 2019/5/6.
 */

public class DanmuPostView extends LinearLayout implements View.OnClickListener{
    private static final int INVALID_VALUE = -1;
    //弹幕输入框
    private EditText danmuPostEt;
    // 弹幕基础设置布局
    private View mDanmakuOptionsBasic;
    // 弹幕字体大小选项卡
    private RadioGroup mDanmakuTextSizeOptions;
    // 弹幕类型选项卡
    private RadioGroup mDanmakuTypeOptions;
    // 弹幕当前颜色
    private RadioButton mDanmakuCurColor;
    // 开关弹幕颜色选项卡
    private ImageView mDanmakuMoreColorIcon;
    // 弹幕颜色选项卡
    private RadioGroup mDanmakuColorOptions;

    // 弹幕基础设置布局的宽度
    private int mBasicOptionsWidth = INVALID_VALUE;
    // 弹幕更多颜色设置布局宽度
    private int mMoreOptionsWidth = INVALID_VALUE;
    // 弹幕颜色
    private int mDanmakuTextColor = Color.WHITE;
    // 弹幕字体大小
    private float mDanmakuTextSize = INVALID_VALUE;
    // 弹幕类型
    private int mDanmakuType = BaseDanmaku.TYPE_SCROLL_RL;

    private float parserDensity = INVALID_VALUE;
    private DanmuPostListener listener;

    public DanmuPostView(Context context) {
        this(context, null);
    }

    @SuppressLint("ClickableViewAccessibility")
    public DanmuPostView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.layout_danmu_post, this);

        // 这些为弹幕配置处理
        int oneBtnWidth = getResources().getDimensionPixelOffset(R.dimen.danmaku_input_options_color_radio_btn_size);
        // 布局宽度为每个选项卡宽度 * 12 个，有12种可选颜色
        mMoreOptionsWidth = oneBtnWidth * 12;

        danmuPostEt = findViewById(R.id.danmu_post_et);
        mDanmakuOptionsBasic = findViewById(R.id.input_options_basic);
        mDanmakuCurColor = findViewById(R.id.input_options_color_current);
        mDanmakuMoreColorIcon = findViewById(R.id.input_options_color_more_icon);
        mDanmakuTextSizeOptions = findViewById(R.id.input_options_group_textsize);
        mDanmakuTypeOptions = findViewById(R.id.input_options_group_type);
        mDanmakuColorOptions = findViewById(R.id.input_options_color_group);

        findViewById(R.id.input_options_more).setOnClickListener(this);
        findViewById(R.id.danmu_post_close_iv).setOnClickListener(this);
        findViewById(R.id.danmu_post_send_iv).setOnClickListener(this);

        mDanmakuTextSizeOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.input_options_small_textsize) {
                    mDanmakuTextSize = 25f * (parserDensity - 0.6f) * 0.7f;
                } else if (checkedId == R.id.input_options_medium_textsize) {
                    mDanmakuTextSize = 25f * (parserDensity - 0.6f);
                }
            }
        });
        mDanmakuTypeOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.input_options_rl_type) {
                    mDanmakuType = BaseDanmaku.TYPE_SCROLL_RL;
                } else if (checkedId == R.id.input_options_top_type) {
                    mDanmakuType = BaseDanmaku.TYPE_FIX_TOP;
                } else if (checkedId == R.id.input_options_bottom_type) {
                    mDanmakuType = BaseDanmaku.TYPE_FIX_BOTTOM;
                }
            }
        });
        mDanmakuColorOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 取的是 tag 字符串值，需转换为颜色
                String color = (String) findViewById(checkedId).getTag();
                mDanmakuTextColor = Color.parseColor(color);
                mDanmakuCurColor.setBackgroundColor(mDanmakuTextColor);
            }
        });



        this.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.VISIBLE){
                    danmuPostEt.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SoftInputUtils.setEditFocusable(getContext(), danmuPostEt);
                        }
                    }, 200);
                }
            }
        });


        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.danmu_post_close_iv){
            resetView();
            this.clearFocus();
            this.setVisibility(GONE);
            SoftInputUtils.closeSoftInput(getContext());
            if (listener != null)
                listener.onClose();
        }else if (id == R.id.danmu_post_send_iv){
            String text = danmuPostEt.getText().toString();
            if (StringUtils.isEmpty(text)){
                ToastUtils.showShort("弹幕内容不能为空");
            }else {
                if (listener != null){
                    if (mDanmakuTextSize == INVALID_VALUE) {
                        mDanmakuTextSize = 25f * (parserDensity - 0.6f);
                    }
                    listener.postDanmu(text, mDanmakuTextSize, mDanmakuType, mDanmakuTextColor);
                }
                danmuPostEt.setText("");
            }
        }else if (id == R.id.input_options_more){
            if (mBasicOptionsWidth == INVALID_VALUE) {
                mBasicOptionsWidth = mDanmakuOptionsBasic.getWidth();
            }
            if (mDanmakuColorOptions.getWidth() == 0) {
                AnimHelper.doClipViewWidth(mDanmakuOptionsBasic, mBasicOptionsWidth, 0, 300);
                AnimHelper.doClipViewWidth(mDanmakuColorOptions, 0, mMoreOptionsWidth, 300);
                ViewCompat.animate(mDanmakuMoreColorIcon).rotation(180).setDuration(150).setStartDelay(250).start();
            } else {
                AnimHelper.doClipViewWidth(mDanmakuOptionsBasic, 0, mBasicOptionsWidth, 300);
                AnimHelper.doClipViewWidth(mDanmakuColorOptions, mMoreOptionsWidth, 0, 300);
                ViewCompat.animate(mDanmakuMoreColorIcon).rotation(0).setDuration(150).setStartDelay(250).start();
            }
        }
    }

    public void resetView(){
        if (mDanmakuColorOptions.getWidth() != 0) {
            AnimHelper.doClipViewWidth(mDanmakuOptionsBasic, 0, mBasicOptionsWidth, 300);
            AnimHelper.doClipViewWidth(mDanmakuColorOptions, mMoreOptionsWidth, 0, 300);
            ViewCompat.animate(mDanmakuMoreColorIcon).rotation(0).setDuration(150).setStartDelay(250).start();
        }
    }

    public void setParserDensity(float density){
        parserDensity = density;
    }

    public void setCallBack(DanmuPostListener listener){
        this.listener = listener;
    }

    public interface DanmuPostListener{
        void postDanmu(String text, float size, int type, int color);

        void onClose();
    }
}
