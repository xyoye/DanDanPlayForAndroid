package com.player.ijkplayer.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.StringUtils;
import com.player.ijkplayer.R;
import com.player.ijkplayer.utils.Constants;
import com.player.subtitle.SubtitleView;

/**
 * Created by xyy on 2019/2/22.
 */

public class SettingSubtitleView extends LinearLayout implements View.OnClickListener{
    private Switch subtitleSwitch;
    private SeekBar subtitleCnSB;
    private SeekBar subtitleUSSB;
    private TextView subtitleChangeSourceTv;
    private TextView subtitleLoadStatusTv;
    private TextView subtitleCnSizeTv;
    private TextView subtitleUSSizeTv;
    private TextView onlyCnShowTv, onlyUsShowTv, bothLanguageTv;
    private TextView encodingUtf8, encodingUtf16, encodingGbk, encodingOther;
    private TextView addEncodingTv;
    private LinearLayout encodingInputLL;
    private EditText encodingEt;
    private TextView addExtraTimeTv, reduceExtraTimeTv;
    private EditText subExtraTimeEt;

    //时间偏移量
    private float timeOffset;

    //控制回调
    private SettingSubtitleListener settingListener = null;

    public SettingSubtitleView(Context context) {
        this(context, null);
    }

    public SettingSubtitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.view_setting_subtitle, this);

        subtitleSwitch = findViewById(R.id.subtitle_sw);
        subtitleLoadStatusTv = findViewById(R.id.subtitle_load_status_tv);
        subtitleChangeSourceTv = findViewById(R.id.subtitle_change_source_tv);
        subtitleCnSizeTv = findViewById(R.id.subtitle_chinese_size_tv);
        subtitleCnSB = findViewById(R.id.subtitle_chinese_size_sb);
        subtitleUSSizeTv = findViewById(R.id.subtitle_english_size_tv);
        subtitleUSSB = findViewById(R.id.subtitle_english_size_sb);
        onlyCnShowTv = findViewById(R.id.only_chinese_tv);
        onlyUsShowTv = findViewById(R.id.only_english_tv);
        bothLanguageTv = findViewById(R.id.both_language_tv);
        encodingUtf8 = findViewById(R.id.encoding_utf_8);
        encodingUtf16 = findViewById(R.id.encoding_utf_16);
        encodingGbk = findViewById(R.id.encoding_gbk);
        encodingOther = findViewById(R.id.encoding_other);
        addEncodingTv = findViewById(R.id.add_encoding_tv);
        encodingInputLL = findViewById(R.id.input_encoding_ll);
        encodingEt = findViewById(R.id.input_encoding_et);
        addExtraTimeTv = findViewById(R.id.subtitle_extra_time_add);
        reduceExtraTimeTv = findViewById(R.id.subtitle_extra_time_reduce);
        subExtraTimeEt = findViewById(R.id.subtitle_extra_time_et);

        subtitleChangeSourceTv.setOnClickListener(this);
        onlyCnShowTv.setOnClickListener(this);
        onlyUsShowTv.setOnClickListener(this);
        bothLanguageTv.setOnClickListener(this);
        encodingUtf8.setOnClickListener(this);
        encodingUtf16.setOnClickListener(this);
        encodingGbk.setOnClickListener(this);
        encodingOther.setOnClickListener(this);
        addEncodingTv.setOnClickListener(this);
        addExtraTimeTv.setOnClickListener(this);
        reduceExtraTimeTv.setOnClickListener(this);

        subExtraTimeEt.setImeOptions(EditorInfo.IME_ACTION_DONE);
        subExtraTimeEt.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        subExtraTimeEt.setSingleLine(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void init(){
        subtitleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settingListener.setSubtitleSwitch(subtitleSwitch, isChecked);
            }
        });

        subExtraTimeEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    try {
                        String offset = subExtraTimeEt.getText().toString().trim();
                        timeOffset = Float.valueOf(offset);
                    }catch (Exception e){
                        Toast.makeText(getContext(), "请输入正确的时间", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                return false;
            }
        });

        subtitleCnSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0 ) progress = 1;
                subtitleCnSizeTv.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress == 0 ) progress = 1;
                settingListener.setSubtitleCnSize(progress);
            }
        });

        subtitleUSSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0 ) progress = 1;
                subtitleUSSizeTv.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                if (progress == 0 ) progress = 1;
                settingListener.setSubtitleEnSize(progress);
            }
        });
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    public SettingSubtitleView initListener(SettingSubtitleListener settingListener){
        this.settingListener = settingListener;
        return this;
    }

    public SettingSubtitleView initSubtitleCnSize(int progress){
        subtitleCnSB.setMax(100);
        subtitleCnSizeTv.setText(progress + "%");
        subtitleCnSB.setProgress(progress);
        return this;
    }

    public SettingSubtitleView initSubtitleEnSize(int progress){
        subtitleUSSB.setMax(100);
        subtitleUSSizeTv.setText(progress + "%");
        subtitleUSSB.setProgress(progress);
        return this;
    }

    public SettingSubtitleView setSubtitleLanguageType(int type){
        switch (type){
            case SubtitleView.LANGUAGE_TYPE_ENGLISH:
                onlyCnShowTv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                onlyUsShowTv.setBackgroundColor(Color.parseColor("#33ffffff"));
                bothLanguageTv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                break;
            case SubtitleView.LANGUAGE_TYPE_BOTH:
                onlyCnShowTv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                onlyUsShowTv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                bothLanguageTv.setBackgroundColor(Color.parseColor("#33ffffff"));
                break;
            default:
                onlyCnShowTv.setBackgroundColor(Color.parseColor("#33ffffff"));
                onlyUsShowTv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                bothLanguageTv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
                break;
        }
        return this;
    }

    public void setSubtitleEncoding(String encoding){
        encodingUtf8.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
        encodingUtf16.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
        encodingGbk.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
        encodingOther.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.sel_item_background));
        switch (encoding.toUpperCase()){
            case "UTF-8":
            case "":
                encodingUtf8.setBackgroundColor(Color.parseColor("#33ffffff"));
                encodingInputLL.setVisibility(GONE);
                break;
            case "UTF-16":
                encodingUtf16.setBackgroundColor(Color.parseColor("#33ffffff"));
                encodingInputLL.setVisibility(GONE);
                break;
            case "GBK":
                encodingGbk.setBackgroundColor(Color.parseColor("#33ffffff"));
                encodingInputLL.setVisibility(GONE);
                break;
            default:
                encodingOther.setBackgroundColor(Color.parseColor("#33ffffff"));
                encodingInputLL.setVisibility(VISIBLE);
                break;
        }
    }

    public void setSubtitleLoadStatus(boolean isLoad){
        if (isLoad){
            subtitleSwitch.setChecked(true);
            subtitleLoadStatusTv.setText("（已加载）");
            subtitleLoadStatusTv.setTextColor(getResources().getColor(R.color.theme_color));
        }else {
            subtitleSwitch.setChecked(false);
            subtitleLoadStatusTv.setText("（未加载）");
            subtitleLoadStatusTv.setTextColor(Color.parseColor("#ff0000"));
        }
    }

    public float getTimeOffset(){
        return timeOffset;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.subtitle_change_source_tv){
            settingListener.setOpenSubtitleSelector();
        }else if (id == R.id.only_chinese_tv){
            settingListener.setSubtitleLanguageType(SubtitleView.LANGUAGE_TYPE_CHINA);
            setSubtitleLanguageType(SubtitleView.LANGUAGE_TYPE_CHINA);
        }else if (id == R.id.only_english_tv){
            settingListener.setSubtitleLanguageType(SubtitleView.LANGUAGE_TYPE_ENGLISH);
            setSubtitleLanguageType(SubtitleView.LANGUAGE_TYPE_ENGLISH);
        }else if (id == R.id.both_language_tv){
            settingListener.setSubtitleLanguageType(SubtitleView.LANGUAGE_TYPE_BOTH);
            setSubtitleLanguageType(SubtitleView.LANGUAGE_TYPE_BOTH);
        }else if (id == R.id.encoding_utf_8){
            setSubtitleEncoding("utf-8");
            settingListener.setSubtitleEncoding("utf-8");
        }else if (id == R.id.encoding_utf_16){
            setSubtitleEncoding("utf-16");
            settingListener.setSubtitleEncoding("utf-16");
        }else if (id == R.id.encoding_gbk){
            setSubtitleEncoding("gbk");
            settingListener.setSubtitleEncoding("gbk");
        }else if (id == R.id.encoding_other){
            encodingInputLL.setVisibility(VISIBLE);
        }else if (id == R.id.add_encoding_tv){
            String encoding = encodingEt.getText().toString().trim();
            if (!StringUtils.isEmpty(encoding)){
                setSubtitleEncoding(encoding);
                settingListener.setSubtitleEncoding(encoding);
            }else{
                Toast.makeText(getContext(), "编码格式不能为空", Toast.LENGTH_LONG).show();
            }
        }else if (id == R.id.subtitle_extra_time_reduce){
            timeOffset -= 0.5f;
            subExtraTimeEt.setText(String.valueOf(timeOffset));
        }else if (id == R.id.subtitle_extra_time_add){
            timeOffset += 0.5f;
            subExtraTimeEt.setText(String.valueOf(timeOffset));
        }
    }

    public interface SettingSubtitleListener{
        void setSubtitleSwitch(Switch switchView, boolean isChecked);
        void setSubtitleCnSize(int progress);
        void setSubtitleEnSize(int progress);

        void setOpenSubtitleSelector();
        void setSubtitleLanguageType(int type);
        void setSubtitleEncoding(String encoding);
    }
}
