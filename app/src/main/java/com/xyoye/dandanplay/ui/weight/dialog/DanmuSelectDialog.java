package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.utils.AppConfig;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2018/12/24.
 */

public class DanmuSelectDialog extends Dialog {
    @BindView(R.id.dont_show_cb)
    CheckBox dontShowCb;
    @BindView(R.id.select_danmu_rb)
    RadioButton selectDanmuRb;
    @BindView(R.id.select_player_rb)
    RadioButton selectPlayerRb;
    @BindView(R.id.select_detail_rg)
    RadioGroup selectDetailRg;

    private DanmuSelectListener selectListener;

    public DanmuSelectDialog(@NonNull Context context, DanmuSelectListener selectListener) {
        super(context, R.style.Dialog);
        this.selectListener = selectListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_danmu_select);
        ButterKnife.bind(this);

        dontShowCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                selectDetailRg.setVisibility(View.VISIBLE);
            }else {
                selectDetailRg.setVisibility(View.GONE);
            }
        });
    }

    @OnClick({R.id.cancel_tv, R.id.confirm_tv})
    public void onViewClicked(View view) {
        boolean isNotShow = dontShowCb.isChecked();
        boolean isSelectDanmu = selectDanmuRb.isChecked();
        boolean isSelectPlayer = selectPlayerRb.isChecked();
        if (isNotShow && !isSelectDanmu && !isSelectPlayer){
            ToastUtils.showShort("请选择默认打方式");
            return;
        }
        if (isNotShow){
            if (isSelectDanmu){
                AppConfig.getInstance().setShowOuterChainDanmuDialog(false);
                AppConfig.getInstance().setOuterChainDanmuSelect(true);
            }else{
                AppConfig.getInstance().setShowOuterChainDanmuDialog(false);
                AppConfig.getInstance().setOuterChainDanmuSelect(false);
            }
        }
        switch (view.getId()) {
            case R.id.confirm_tv:
                selectListener.onSelect(true);
                break;
            case R.id.cancel_tv:
                selectListener.onSelect(false);
                break;
        }
        DanmuSelectDialog.this.dismiss();
    }

    public interface DanmuSelectListener {
        void onSelect(boolean isSelectDanmu);
    }
}
