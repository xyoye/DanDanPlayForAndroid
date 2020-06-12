package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.smb.info.SmbType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2020/6/12.
 */

public class SmbToolsDialog extends Dialog {

    @BindView(R.id.dialog_title_tv)
    TextView dialogTitleTv;
    @BindView(R.id.smb_rg)
    RadioGroup smbRg;

    private SmbToolsCallback callback;

    public SmbToolsDialog(@NonNull Context context, @NonNull SmbToolsCallback callback) {
        super(context, R.style.Dialog);
        this.callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_smb_tools);
        ButterKnife.bind(this);

        dialogTitleTv.setText("选择SMB连接工具");

        switch (AppConfig.getInstance().getSmbTools()) {
            case SMBJ_RPC:
                smbRg.check(R.id.smbj_rpc_rb);
                break;
            case SMBJ:
                smbRg.check(R.id.smbj_rb);
                break;
            case JCIFS:
                smbRg.check(R.id.jcifs_ng_rb);
                break;
            case JCIFS_NG:
                smbRg.check(R.id.jcifs_rb);
                break;
        }
    }

    @OnClick({R.id.cancel_tv, R.id.confirm_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel_tv:
                SmbToolsDialog.this.dismiss();
                break;
            case R.id.confirm_tv:
                SmbToolsDialog.this.dismiss();
                SmbType smbType = getSmbType(smbRg.getCheckedRadioButtonId());
                AppConfig.getInstance().setSmbTools(smbType);
                callback.onSelected(smbType);
                break;
        }
    }

    private SmbType getSmbType(@IdRes int radioButtonId) {
        switch (radioButtonId) {
            case R.id.smbj_rpc_rb:
                return SmbType.SMBJ_RPC;
            case R.id.smbj_rb:
                return SmbType.SMBJ;
            case R.id.jcifs_ng_rb:
                return SmbType.JCIFS_NG;
            case R.id.jcifs_rb:
                return SmbType.JCIFS;
        }
        return SmbType.SMBJ_RPC;
    }

    public interface SmbToolsCallback{
        void onSelected(SmbType smbType);
    }
}
