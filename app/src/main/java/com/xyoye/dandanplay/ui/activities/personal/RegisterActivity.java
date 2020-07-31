package com.xyoye.dandanplay.ui.activities.personal;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.bean.params.RegisterParam;
import com.xyoye.dandanplay.mvp.impl.RegisterPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.RegisterPresenter;
import com.xyoye.dandanplay.mvp.view.RegisterView;
import com.xyoye.dandanplay.ui.weight.dialog.ToLoginDialog;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2018/8/5.
 */

public class RegisterActivity extends BaseMvpActivity<RegisterPresenter> implements RegisterView {

    @BindView(R.id.account_et)
    EditText accountEt;
    @BindView(R.id.email_et)
    EditText emailEt;
    @BindView(R.id.password_et)
    EditText passwordEt;
    @BindView(R.id.eye_iv)
    ImageView eyeIv;

    private boolean isPasswordShow = false;

    @Override
    public void initView() {
        setTitle("注册");
    }

    @Override
    public void initListener() {

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

    private void register() {
        String userName = accountEt.getText().toString();
        String password = passwordEt.getText().toString();
        String email = emailEt.getText().toString();

        /*
         * 用户名               只能包含英文或数字，长度为5-20位，首位不能为数字。
         * 密码                长度为5到20位之间。
         * 备用邮箱（找回密码用）  长度不能超过50个字符。
         */
        if (StringUtils.isEmpty(userName)) {
            ToastUtils.showShort("用户名不能为空");
            return;
        }
        if (userName.length() < 5 || userName.length() > 20) {
            ToastUtils.showShort("用户名字长度为5-20个字符");
            return;
        }
        if (!userName.matches("[0-9a-zA-Z]*")) {
            ToastUtils.showShort("只能包含英文或数字");
            return;
        }
        if (userName.substring(0, 1).matches("[0-9]*")) {
            ToastUtils.showShort("用户名不能以数字开头");
            return;
        }

        if (StringUtils.isEmpty(password)) {
            ToastUtils.showShort("密码不能为空");
            return;
        }
        if (password.length() < 5 || password.length() > 20) {
            ToastUtils.showShort("密码长度为5-20个字符");
            return;
        }

        if (StringUtils.isEmpty(email)) {
            ToastUtils.showShort("邮箱不能为空");
            return;
        }
        if (email.length() > 50) {
            ToastUtils.showShort("邮箱长度为50个字符以内");
            return;
        }
        presenter.register(new RegisterParam(userName, password, email));
    }

    @Override
    public Context getRegisterContext() {
        return this;
    }

    @Override
    public void registerSuccess() {
        ToLoginDialog dialog = new ToLoginDialog(this, R.style.Dialog, 0, RegisterActivity.this::finish);
        dialog.show();
    }

    @Override
    public void showLoading() {
        showLoadingDialog();
    }

    @Override
    public void hideLoading() {
        dismissLoadingDialog();
    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
    }

    @OnClick({R.id.eye_iv, R.id.register_bt})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.eye_iv:
                isPasswordShow = !isPasswordShow;
                passwordEt.setInputType(isPasswordShow
                        ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
                passwordEt.setSelection(passwordEt.length());
                eyeIv.setImageResource(isPasswordShow
                        ? R.mipmap.ic_eye_open
                        : R.mipmap.ic_eye_close);
                break;
            case R.id.register_bt:
                register();
                break;
        }
    }
}
