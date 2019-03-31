package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.CheckBox;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.LanDeviceBean;
import com.xyoye.dandanplay.bean.SmbBean;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyy on 2018/11/20.
 */

public class SmbDialog extends Dialog {

    @BindView(R.id.lan_account_et)
    TextInputEditText lanAccountEt;
    @BindView(R.id.lan_password_et)
    TextInputEditText lanPasswordEt;
    @BindView(R.id.lan_domain_et)
    TextInputEditText lanDomainEt;
    @BindView(R.id.anonymous_cb)
    CheckBox anonymousCb;
    @BindView(R.id.lan_ip_et)
    TextInputEditText lanIpEt;
    @BindView(R.id.lan_ip_layout)
    TextInputLayout lanIpLayout;

    private int mPosition;
    private boolean isAddDevice;
    private SmbBean mSmbBean;
    private OnSmbAuthListener authListener;

    public SmbDialog(@NonNull Context context, SmbBean smbBean, int position, OnSmbAuthListener authListener) {
        super(context, R.style.Dialog);
        this.mPosition = position;
        this.mSmbBean = smbBean;
        this.isAddDevice = position == -1;
        this.authListener = authListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_auth_lan);
        ButterKnife.bind(this);

        if (mSmbBean != null){
            lanAccountEt.setText(mSmbBean.getAccount());
            lanPasswordEt.setText(mSmbBean.getPassword());
            lanDomainEt.setText(mSmbBean.getDomain());
            anonymousCb.setChecked(mSmbBean.isAnonymous());
        }
        lanIpLayout.setVisibility(isAddDevice ? View.VISIBLE : View.GONE);
    }

    @OnClick({R.id.cancel_tv, R.id.confirm_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel_tv:
                SmbDialog.this.dismiss();
                break;
            case R.id.confirm_tv:
                boolean anonymous = anonymousCb.isChecked();
                String account = lanAccountEt.getText().toString();
                String password = lanPasswordEt.getText().toString();
                String domain = lanDomainEt.getText().toString();
                String ip = lanIpEt.getText().toString();
                if (isAddDevice && StringUtils.isEmpty(ip)){
                    ToastUtils.showShort("请输入ip地址");
                    return;
                }
                if (!anonymous && StringUtils.isEmpty(account)) {
                    ToastUtils.showShort("请输入账号密码或选择匿名登陆");
                    return;
                }
                SmbBean smbBean = new SmbBean();
                smbBean.setAccount(account);
                smbBean.setPassword(password);
                smbBean.setDomain(domain);
                smbBean.setAnonymous(anonymous);
                if (isAddDevice){
                    smbBean.setUrl(ip);
                    if (authListener != null){
                        SmbDialog.this.dismiss();
                        authListener.onSubmit(smbBean, mPosition);
                    }
                }else {
                    smbBean.setUrl(ip);
                    if (authListener != null){
                        SmbDialog.this.dismiss();
                        authListener.onSubmit(smbBean, mPosition);
                    }
                }
                break;
        }
    }

    public interface OnSmbAuthListener{
        void onSubmit(SmbBean smbBean, int position);
    }
}
