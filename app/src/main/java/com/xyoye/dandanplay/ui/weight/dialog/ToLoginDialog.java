package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.TextView;

import com.xyoye.core.base.AppManager;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.ui.activities.ChangePasswordActivity;
import com.xyoye.dandanplay.ui.activities.LoginActivity;
import com.xyoye.dandanplay.ui.activities.PersonalInfoActivity;
import com.xyoye.dandanplay.ui.activities.RegisterActivity;
import com.xyoye.dandanplay.ui.activities.ResetPasswordActivity;
import com.xyoye.dandanplay.utils.TokenShare;
import com.xyoye.dandanplay.utils.UserInfoShare;

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
    private Class cla = null;
    private Context context;

    public ToLoginDialog(@NonNull Context context, int themeResId, int dex) {
        super(context, themeResId);
        this.context = context;
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
                cla = RegisterActivity.class;
                break;
            case 1:
                tipsTv.setText("重置密码成功，请前往邮箱查看临时密码");
                cla = ResetPasswordActivity.class;
                break;
            case 2:
                tipsTv.setText("修改密码成功，请重新登录");
                cla = ChangePasswordActivity.class;
                break;
        }

        toLogin.setOnClickListener(v -> {
            ToLoginDialog.this.cancel();
            if (cla != null){
                if (dex == 2){
                    UserInfoShare.getInstance().setLogin(false);
                    UserInfoShare.getInstance().saveUserName("");
                    UserInfoShare.getInstance().saveUserScreenName("");
                    UserInfoShare.getInstance().saveUserImage("");
                    TokenShare.getInstance().saveToken("");
                    Activity personalInfoActivity = AppManager.getActivity(PersonalInfoActivity.class);
                    Activity changePasswordActivity = AppManager.getActivity(ChangePasswordActivity.class);
                    if (personalInfoActivity != null)
                        AppManager.finishActivity(personalInfoActivity);
                    if (changePasswordActivity != null)
                        AppManager.finishActivity(changePasswordActivity);
                    context.startActivity(new Intent(context, LoginActivity.class));
                }
                Activity activity = AppManager.getActivity(cla);
                if (activity != null)
                    activity.finish();
            }
        });
    }
}
