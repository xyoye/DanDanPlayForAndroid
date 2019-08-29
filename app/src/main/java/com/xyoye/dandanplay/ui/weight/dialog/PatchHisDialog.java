package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.event.PatchFixEvent;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2018/12/12.
 */

public class PatchHisDialog extends Dialog {

    @BindView(R.id.his_tv)
    TextView hisTv;
    @BindView(R.id.confirm_tv)
    TextView confirmTv;
    @BindView(R.id.clear_his)
    TextView clearHis;
    @BindView(R.id.fix_mode_tv)
    TextView fixModeTv;

    private List<PatchFixEvent> eventList;
    private Context mContext;

    public PatchHisDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        eventList = JsonUtil.getObjectList(SPUtils.getInstance().getString("patch_his"), PatchFixEvent.class);
        if (eventList == null) eventList = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_patch_his);
        ButterKnife.bind(this);

        StringBuilder his = new StringBuilder();
        if (eventList != null && eventList.size() > 0) {
            for (int i = eventList.size() - 1; i >= 0; i--) {
                PatchFixEvent event = eventList.get(i);
                his.append(event.getTime());
                if (event.getCode() > 0)
                    his.append("    c：").append(event.getCode());
                his.append("    v：").append(event.getVersion());
                if (event.getCode() == -2)
                    his.append("    ").append("Newest").append("\n\n");
                else
                    his.append("\n").append(event.getMsg()).append("\n\n");
            }
        }

        if (AppConfig.getInstance().isAutoQueryPatch()) {
            fixModeTv.setText("自动");
            fixModeTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
        } else {
            fixModeTv.setText("手动");
            fixModeTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_pink));
        }

        hisTv.setText(his.toString());
        hisTv.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @OnClick({R.id.confirm_tv, R.id.clear_his, R.id.fix_mode_tv, R.id.about_sophix_iv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fix_mode_tv:
                if (AppConfig.getInstance().isAutoQueryPatch()) {
                    AppConfig.getInstance().setAutoQueryPatch(false);
                    fixModeTv.setText("手动");
                    fixModeTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_pink));
                } else {
                    AppConfig.getInstance().setAutoQueryPatch(true);
                    fixModeTv.setText("自动");
                    fixModeTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_theme));
                }
                break;
            case R.id.about_sophix_iv:
                new CommonDialog.Builder(mContext)
                        .setAutoDismiss()
                        .hideCancel()
                        .build()
                        .show(mContext.getResources().getString(R.string.what_is_sophix), "什么是热修复", "确定", "");
                break;
            case R.id.confirm_tv:
                PatchHisDialog.this.dismiss();
                break;
            case R.id.clear_his:
                SPUtils.getInstance().remove("patch_his");
                hisTv.setText("");
                break;
        }
    }
}
