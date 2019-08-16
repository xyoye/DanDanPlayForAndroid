package com.xyoye.dandanplay.ui.activities.personal;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.bean.params.LoginParam;
import com.xyoye.dandanplay.mvp.impl.LoginPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.LoginPresenter;
import com.xyoye.dandanplay.mvp.view.LoginView;
import com.xyoye.dandanplay.ui.activities.MainActivity;
import com.xyoye.dandanplay.utils.KeyUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2018/7/22.
 */

public class LoginActivity extends BaseMvpActivity<LoginPresenter> implements LoginView {
    @BindView(R.id.account_et)
    EditText accountEt;
    @BindView(R.id.password_et)
    EditText passwordEt;
    @BindView(R.id.eye_iv)
    ImageView eyeIv;

    private boolean isPasswordShow = false;

    @Override
    public void initView() {
        setTitle("登录");
    }

    @Override
    public void initListener() {

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

    private void login() {
        String userName = accountEt.getText().toString();
        String password = passwordEt.getText().toString();
        if (StringUtils.isEmpty(userName)) {
            ToastUtils.showShort("用户名不能为空");
        } else if (StringUtils.isEmpty(password)) {
            ToastUtils.showShort("密码不能为空");
        } else {
            LoginParam param = new LoginParam();
            param.setUserName(userName);
            param.setPassword(password);
            param.setAppId(KeyUtil.getDanDanAppId(this));
            param.setUnixTimestamp(System.currentTimeMillis() / 1000);
            param.buildHash(this);
            presenter.login(param);
        }
    }

    @Override
    protected void onBeforeFinish() {
        launchMain();
    }

    @Override
    public void onBackPressed() {
        launchMain();
        super.onBackPressed();
    }

    @Override
    public void launchMain() {
        if (getIntent().getBooleanExtra("isOpen", false))
            launchActivity(MainActivity.class);
        else
            this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

    @OnClick({R.id.eye_iv, R.id.login_bt, R.id.to_register_tv, R.id.to_reset_password_tv})
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
            case R.id.login_bt:
                login();
                break;
            case R.id.to_register_tv:
                launchActivity(RegisterActivity.class);
                break;
            case R.id.to_reset_password_tv:
                launchActivity(ResetPasswordActivity.class);
                break;
        }
    }
}
