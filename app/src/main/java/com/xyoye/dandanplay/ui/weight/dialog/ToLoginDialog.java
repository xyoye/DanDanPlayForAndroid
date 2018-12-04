package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.ui.activities.LoginActivity;
import com.xyoye.dandanplay.utils.AppConfig;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by YE on 2018/8/5.
 */


public class ToLoginDialog extends Dialog {
    @BindView(R.id.tips_tv)
    TextView tipsTv;
    @BindView(R.id.to_login_bt)
    Button toLogin;

    private int dex;

    public ToLoginDialog(@NonNull Context context, int themeResId, int dex) {
        super(context, themeResId);
        this.dex = dex;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_to_login);
        ButterKnife.bind(this, this);

        switch (dex){
            case 0:
                tipsTv.setText("弹弹play，注册成功！");
                break;
            case 1:
                tipsTv.setText("重置密码成功，请前往邮箱查看临时密码");
                break;
            case 2:
                tipsTv.setText("修改密码成功，请重新登录");
                break;
        }

        toLogin.setOnClickListener(v -> {
            ToLoginDialog.this.cancel();
            if (dex == 2){
                AppConfig.getInstance().setLogin(false);
                AppConfig.getInstance().saveUserName("");
                AppConfig.getInstance().saveUserScreenName("");
                AppConfig.getInstance().saveUserImage("");
                AppConfig.getInstance().saveToken("");
            }
            ActivityUtils.finishToActivity(LoginActivity.class, false);
        });
    }
}
