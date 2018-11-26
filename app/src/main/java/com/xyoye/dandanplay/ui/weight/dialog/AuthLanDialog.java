package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.utils.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.LanDeviceBean;
import com.xyoye.dandanplay.bean.event.AddLanDeviceEvent;
import com.xyoye.dandanplay.bean.event.AuthLanEvent;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyy on 2018/11/20.
 */

public class AuthLanDialog extends Dialog {

    @BindView(R.id.lan_account_et)
    TextInputEditText lanAccountEt;
    @BindView(R.id.lan_password_et)
    TextInputEditText lanPasswordEt;
    @BindView(R.id.anonymous_cb)
    CheckBox anonymousCb;
    @BindView(R.id.lan_cancel_bt)
    Button lanCancelBt;
    @BindView(R.id.lan_login_bt)
    Button lanLoginBt;
    @BindView(R.id.lan_ip_et)
    TextInputEditText lanIpEt;
    @BindView(R.id.lan_ip_layout)
    TextInputLayout lanIpLayout;

    private int position;
    private LanDeviceBean mDeviceBean;
    //用于区分是否为添加新设备
    private boolean isShowIp;

    public AuthLanDialog(@NonNull Context context, int themeResId, LanDeviceBean deviceBean, int position, boolean showIp) {
        super(context, themeResId);
        this.position = position;
        mDeviceBean = deviceBean;
        isShowIp = showIp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_auth_lan);
        ButterKnife.bind(this);

        if (mDeviceBean != null){
            lanAccountEt.setText(mDeviceBean.getAccount());
            lanPasswordEt.setText(mDeviceBean.getPassword());
            anonymousCb.setChecked(mDeviceBean.isAnonymous());
        }
        lanIpLayout.setVisibility(isShowIp ? View.VISIBLE : View.GONE);
    }

    @OnClick({R.id.lan_cancel_bt, R.id.lan_login_bt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.lan_cancel_bt:
                AuthLanDialog.this.dismiss();
                break;
            case R.id.lan_login_bt:
                boolean anonymous = anonymousCb.isChecked();
                String account = lanAccountEt.getText().toString();
                String password = lanPasswordEt.getText().toString();
                String ip = lanIpEt.getText().toString();
                if (isShowIp && StringUtils.isEmpty(ip)){
                    ToastUtils.showShort("请输入ip地址");
                    return;
                }
                if (!anonymous && StringUtils.isEmpty(account)) {
                    ToastUtils.showShort("请输入账号密码或选择匿名登陆");
                    return;
                }
                if (!isShowIp)
                    EventBus.getDefault().post(new AuthLanEvent(account, password, anonymous, position));
                else {
                    LanDeviceBean lanDeviceBean = new LanDeviceBean();
                    lanDeviceBean.setIp(ip);
                    lanDeviceBean.setAccount(account);
                    lanDeviceBean.setPassword(password);
                    lanDeviceBean.setAnonymous(anonymous);
                    EventBus.getDefault().post(new AddLanDeviceEvent(lanDeviceBean));
                }
                break;
        }
    }
}
