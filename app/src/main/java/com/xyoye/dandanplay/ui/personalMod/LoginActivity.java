package com.xyoye.dandanplay.ui.personalMod;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.jaeger.library.StatusBarUtil;
import com.umeng.socialize.UMShareAPI;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.params.LoginParam;
import com.xyoye.dandanplay.mvp.impl.LoginPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.LoginPresenter;
import com.xyoye.dandanplay.mvp.view.LoginView;
import com.xyoye.dandanplay.ui.mainMod.MainActivity;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/22.
 */

public class LoginActivity extends BaseActivity<LoginPresenter> implements LoginView,View.OnClickListener {
    @BindView(R.id.login_return_iv)
    ImageView loginReturnIv;
    @BindView(R.id.user_name_et)
    EditText userNameEt;
    @BindView(R.id.user_password_et)
    EditText userPasswordEt;
    @BindView(R.id.user_password_eye_iv)
    ImageView eyeIv;
    @BindView(R.id.login_bt)
    Button loginBt;
    @BindView(R.id.forgot_password_tv)
    TextView forgotPasswordTv;
    @BindView(R.id.register_tv)
    TextView registerTv;

    private boolean isPShow = false;

    @Override
    public void initView() {

    }

    @Override
    public void initListener() {
        loginReturnIv.setOnClickListener(this);
        eyeIv.setOnClickListener(this);
        loginBt.setOnClickListener(this);
        forgotPasswordTv.setOnClickListener(this);
        registerTv.setOnClickListener(this);
    }

    @NonNull
    @Override
    protected LoginPresenter initPresenter() {
        return new LoginPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_login;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_return_iv:
                launchMain();
                break;
            case R.id.user_password_eye_iv:
                if (!isPShow){
                    userPasswordEt.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    eyeIv.setImageResource(R.mipmap.ic_password_show);
                    isPShow = true;
                }else {
                    userPasswordEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    eyeIv.setImageResource(R.mipmap.ic_password_hidden);
                    isPShow = false;
                }
                break;
            case R.id.login_bt:
                login();
                break;
            case R.id.forgot_password_tv:
                ToastUtils.showShort("该功能暂未开放");
                break;
            case R.id.register_tv:
                ToastUtils.showShort("该功能暂未开放");
                break;
        }
    }

    private void login(){
        String userName = userNameEt.getText().toString();
        String password = userPasswordEt.getText().toString();
        if (StringUtils.isEmpty(userName)){
            ToastUtils.showShort("用户名不能为空");
        }else if (StringUtils.isEmpty(password)){
            ToastUtils.showShort("密码不能为空");
        }else {
            presenter.login(new LoginParam(userName, password));
        }
    }

    @Override
    public Context getPersonalContext() {
        return this;
    }

    @Override
    public void launchMain() {
        if(getIntent().getBooleanExtra("isOpen", false))
            launchActivity(MainActivity.class);
        else
            this.finish();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    protected void setStatusBar() {
        StatusBarUtil.setTranslucentForImageView(this,null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }
}
