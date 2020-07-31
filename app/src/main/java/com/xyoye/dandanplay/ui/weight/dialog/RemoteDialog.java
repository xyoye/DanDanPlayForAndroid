package com.xyoye.dandanplay.ui.weight.dialog;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.RemoteScanBean;
import com.xyoye.dandanplay.ui.activities.play.RemoteActivity;
import com.xyoye.dandanplay.ui.activities.play.RemoteScanActivity;
import com.xyoye.dandanplay.utils.AppConfig;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;

/**
 * Created by xyoye on 2019/7/11.
 */

public class RemoteDialog extends Dialog {

    @BindView(R.id.ip_edit_et)
    TextInputEditText ipEditEt;
    @BindView(R.id.ip_edit_layout)
    TextInputLayout ipEditLayout;
    @BindView(R.id.port_edit_et)
    TextInputEditText portEditEt;
    @BindView(R.id.port_edit_layout)
    TextInputLayout portEditLayout;
    @BindView(R.id.auth_edit_et)
    TextInputEditText authEditEt;

    private Disposable permissionDis;
    private FragmentActivity activity;

    public RemoteDialog(@NonNull Context context) {
        super(context, R.style.Dialog);
        this.activity = (FragmentActivity) context;
        setContentView(R.layout.dialog_remote);
        ButterKnife.bind(this);

        ipEditEt.setHint("IP地址");
        portEditEt.setHint("端口");
        authEditEt.setHint("API密钥（可为空）");

        String remoteLoginData = AppConfig.getInstance().getRemoteLoginData();
        if (!StringUtils.isEmpty(remoteLoginData)){

            String[] loginData = remoteLoginData.split(";");
            if (loginData.length != 3) return;

            ipEditEt.setText(loginData[0]);
            portEditEt.setText(loginData[1]);
            authEditEt.setText(loginData[2].trim());
        }
    }

    @OnClick({R.id.remote_scan_ll, R.id.cancel_tv, R.id.confirm_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.remote_scan_ll:
                permissionDis = new RxPermissions(activity).
                        request(Manifest.permission.CAMERA)
                        .subscribe(granted -> {
                            if (granted) {
                                activity.startActivity(new Intent(activity, RemoteScanActivity.class));
                                RemoteDialog.this.dismiss();
                            }else {
                                ToastUtils.showLong("未授予照相机权限，无法打开扫描页面");
                            }
                        });
                break;
            case R.id.cancel_tv:
                RemoteDialog.this.dismiss();
                break;
            case R.id.confirm_tv:
                String ipData = ipEditEt.getText().toString().trim();
                String portData = portEditEt.getText().toString();
                String auth = authEditEt.getText().toString();
                if (StringUtils.isEmpty(ipData)) {
                    ipEditLayout.setErrorEnabled(true);
                    ipEditLayout.setError("IP地址不能为空");
                    return;
                }
                if (StringUtils.isEmpty(portData)) {
                    portEditLayout.setErrorEnabled(true);
                    portEditLayout.setError("端口不能为空");
                    return;
                }

                List<String> ipList = new ArrayList<>();
                ipList.add(ipData);
                RemoteScanBean scanBean = new RemoteScanBean();
                scanBean.setIp(ipList);
                scanBean.setPort(Integer.valueOf(portData));
                if (!StringUtils.isEmpty(auth)){
                    scanBean.setAuthorization(auth);
                    scanBean.setTokenRequired(true);
                }

                Intent intent = new Intent(activity, RemoteActivity.class);
                intent.putExtra("remote_data", scanBean);
                activity.startActivity(intent);
                RemoteDialog.this.dismiss();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (permissionDis != null)
            permissionDis.dispose();
    }
}
