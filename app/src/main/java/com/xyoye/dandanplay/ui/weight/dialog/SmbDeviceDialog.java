package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.SmbDeviceBean;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.helper.SmbDeviceAction;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyoye on 2020/1/3.
 */

public class SmbDeviceDialog extends Dialog {
    @BindView(R.id.dialog_title_tv)
    TextView dialogTitleTv;
    @BindView(R.id.ip_et)
    EditText ipEt;
    @BindView(R.id.account_et)
    EditText accountEt;
    @BindView(R.id.password_et)
    EditText passwordEt;
    @BindView(R.id.domain_et)
    EditText domainEt;
    @BindView(R.id.share_et)
    EditText shareEt;
    @BindView(R.id.nick_name_et)
    EditText nickNameEt;
    @BindView(R.id.anonymous_cb)
    CheckBox anonymousCb;
    @BindView(R.id.ip_ll)
    LinearLayout ipLl;
    @BindView(R.id.share_ll)
    LinearLayout shareLl;

    private SmbDeviceBean deviceBean;
    private SmbDeviceAction action;
    private SmbDeviceDialogCallback callback;

    public SmbDeviceDialog(@NonNull Context context, SmbDeviceBean deviceBean, SmbDeviceAction action, SmbDeviceDialogCallback callback) {
        super(context, R.style.Dialog);
        this.deviceBean = deviceBean;
        this.action = action;
        this.callback = callback;

        if (action == SmbDeviceAction.ACTION_DEVICE_EDIT && deviceBean == null){
            throw new NullPointerException("device info can not be null");
        }

        if (callback == null){
            throw new NullPointerException("dialog callback can not be null");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_smb_device);
        ButterKnife.bind(this);

        boolean isSmbBetaFeatureEnable = AppConfig.getInstance().isOpenSmbBetaFeature();
        shareLl.setVisibility(isSmbBetaFeatureEnable ? View.VISIBLE : View.GONE);

        switch (action){
            case ACTION_DEVICE_ADD:
                dialogTitleTv.setText("新增服务器");
                break;
            case ACTION_DEVICE_INIT:
                ipLl.setVisibility(View.GONE);
                ipEt.setText(deviceBean.getUrl());
                dialogTitleTv.setText(deviceBean.getUrl());
                break;
            case ACTION_DEVICE_EDIT:
                ipLl.setVisibility(View.GONE);
                ipEt.setText(deviceBean.getUrl());
                dialogTitleTv.setText(deviceBean.getUrl());
                accountEt.setText(deviceBean.getAccount());
                passwordEt.setText(deviceBean.getPassword());
                domainEt.setText(deviceBean.getDomain());
                shareEt.setText(isSmbBetaFeatureEnable ? deviceBean.getRootFolder() : "");
                anonymousCb.setChecked(deviceBean.isAnonymous());
                nickNameEt.setText(deviceBean.getNickName());
                break;
        }
    }

    @OnClick({R.id.cancel_tv, R.id.confirm_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel_tv:
                SmbDeviceDialog.this.dismiss();
                break;
            case R.id.confirm_tv:
                String ip = ipEt.getText().toString();
                String account = accountEt.getText().toString();
                String password = passwordEt.getText().toString();
                boolean isAnonymous = anonymousCb.isChecked();

                if (TextUtils.isEmpty(ip)){
                    ToastUtils.showShort("IP地址不能为空");
                    return;
                }

                if (!isAnonymous && (TextUtils.isEmpty(account) || TextUtils.isEmpty(password))){
                    ToastUtils.showShort("帐号信息不能为空");
                    return;
                }

                SmbDeviceBean deviceBean = new SmbDeviceBean();

                deviceBean.setUrl(ip);
                deviceBean.setAccount(account);
                deviceBean.setPassword(password);
                deviceBean.setDomain(domainEt.getText().toString());
                deviceBean.setNickName(nickNameEt.getText().toString());
                deviceBean.setRootFolder(shareEt.getText().toString());
                deviceBean.setAnonymous(isAnonymous);
                deviceBean.setSmbType(Constants.SmbType.SQL_DEVICE);
                callback.onDeviceUpdate(deviceBean);
                SmbDeviceDialog.this.dismiss();
                break;
        }
    }

    public interface SmbDeviceDialogCallback{
        void onDeviceUpdate(SmbDeviceBean deviceBean);
    }
}
