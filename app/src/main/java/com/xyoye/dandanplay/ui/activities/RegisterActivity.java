package com.xyoye.dandanplay.ui.activities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.bean.params.RegisterParam;
import com.xyoye.dandanplay.mvp.impl.RegisterPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.RegisterPresenter;
import com.xyoye.dandanplay.mvp.view.RegisterView;

import butterknife.BindView;

/**
 * Created by YE on 2018/8/5.
 */


public class RegisterActivity extends BaseMvpActivity<RegisterPresenter> implements RegisterView, View.OnClickListener {
    @BindView(R.id.return_iv)
    ImageView returnIv;
    @BindView(R.id.user_name_et)
    TextInputEditText userNameEt;
    @BindView(R.id.user_password_et)
    TextInputEditText userPasswordEt;
    @BindView(R.id.user_email_et)
    TextInputEditText userEmailEt;
    @BindView(R.id.user_password_layout)
    TextInputLayout userPasswordLayout;
    @BindView(R.id.user_name_layout)
    TextInputLayout userNameLayout;
    @BindView(R.id.user_email_layout)
    TextInputLayout userEmailLayout;
    @BindView(R.id.register_bt)
    Button registerBt;

    @Override
    public void initView() {
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        Window window = getWindow();
        window.setFlags(flag, flag);
    }

    @Override
    public void initListener() {
        returnIv.setOnClickListener(this);
        registerBt.setOnClickListener(this);
    }

    @NonNull
    @Override
    protected RegisterPresenter initPresenter() {
        return new RegisterPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_register;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.return_iv:
                RegisterActivity.this.finish();
                break;
            case R.id.register_bt:
                register();
                break;
        }
    }

    private void register(){
        String userName = userNameEt.getText().toString();
        String password = userPasswordEt.getText().toString();
        String email = userEmailEt.getText().toString();

        /*
         * 用户名               只能包含英文或数字，长度为5-20位，首位不能为数字。
         * 密码                长度为5到20位之间。
         * 备用邮箱（找回密码用）  长度不能超过50个字符。
         */
        if (StringUtils.isEmpty(userName)) {
            userNameLayout.setErrorEnabled(true);
            userNameLayout.setError("用户名不能为空");
            return;
        }
        if (userName.length()<5 || userName.length()>20){
            userNameLayout.setErrorEnabled(true);
            userNameLayout.setError("用户名字长度为5-20个字符");
            return;
        }
        if (!userName.matches("[0-9a-zA-Z]*")){
            userNameLayout.setErrorEnabled(true);
            userNameLayout.setError("只能包含英文或数字");
            return;
        }
        if (userName.substring(0,1).matches("[0-9]*")){
            userNameLayout.setErrorEnabled(true);
            userNameLayout.setError("用户名不能以数字开头");
            return;
        }

        if (StringUtils.isEmpty(password)) {
            userPasswordLayout.setErrorEnabled(true);
            userPasswordLayout.setError("密码不能为空");
            return;
        }
        if (password.length()<5 || password.length()>20) {
            userPasswordLayout.setErrorEnabled(true);
            userPasswordLayout.setError("密码长度为5-20个字符");
            return;
        }

        if (StringUtils.isEmpty(email)) {
            userEmailLayout.setErrorEnabled(true);
            userEmailLayout.setError("邮箱不能为空");
            return;
        }
        if (email.length() > 50) {
            userEmailLayout.setErrorEnabled(true);
            userEmailLayout.setError("邮箱长度为50个字符以内");
            return;
        }

        userNameLayout.setErrorEnabled(false);
        userPasswordLayout.setErrorEnabled(false);
        userEmailLayout.setErrorEnabled(false);
        presenter.register(new RegisterParam(userName, password, email));
    }

    @Override
    public Context getRegisterContext() {
        return this;
    }
}
