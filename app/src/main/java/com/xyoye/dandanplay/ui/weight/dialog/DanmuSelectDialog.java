package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyy on 2018/12/24.
 */

public class DanmuSelectDialog extends Dialog {
    @BindView(R.id.dont_show_rb)
    RadioButton dontShowRb;
    @BindView(R.id.select_danmu_rb)
    RadioButton selectDanmuRb;
    @BindView(R.id.select_player_rb)
    RadioButton selectPlayerRb;
    @BindView(R.id.select_detail_ll)
    LinearLayout selectDetailLl;

    private DanmuSelectListener selectListener;

    public DanmuSelectDialog(@NonNull Context context, DanmuSelectListener selectListener) {
        super(context, R.style.Dialog);
        this.selectListener = selectListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_danmu_select);
    }

    @OnClick({R.id.cancel_tv, R.id.confirm_tv})
    public void onViewClicked(View view) {
        boolean isNotShow = dontShowRb.isChecked();
        boolean isSelectDanmu = selectDanmuRb.isChecked();
        boolean isSelectPlayer = selectPlayerRb.isChecked();
        switch (view.getId()) {
            case R.id.confirm_tv:
                if (isNotShow && !isSelectDanmu && !isSelectPlayer){
                    ToastUtils.showShort("请选择默认打方式");
                }else if (isNotShow && isSelectDanmu){

                }else if (!isNotShow){

                }else {

                }
                break;
            case R.id.cancel_tv:
                if (isNotShow && !isSelectDanmu && !isSelectPlayer){
                    ToastUtils.showShort("请选择默认打方式");
                }else if (isNotShow && isSelectDanmu){

                }else if (isNotShow && isSelectPlayer){

                }else {

                }
                break;
        }
    }

    public interface DanmuSelectListener {
        void onSelect(String type);
    }
}
