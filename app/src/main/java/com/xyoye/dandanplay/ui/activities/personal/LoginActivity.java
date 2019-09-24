package com.xyoye.dandanplay.ui.activities.personal;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.bean.QQLoginBean;
import com.xyoye.dandanplay.bean.params.LoginParam;
import com.xyoye.dandanplay.bean.params.ThreePartLoginParam;
import com.xyoye.dandanplay.mvp.impl.LoginPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.LoginPresenter;
import com.xyoye.dandanplay.mvp.view.LoginView;
import com.xyoye.dandanplay.ui.activities.MainActivity;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.JsonUtil;

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
    private IUiListener mLoginListener;

    @Override
    public void initView() {
        String userName = AppConfig.getInstance().getUserName();
        accountEt.setText(userName);
        setTitle("登录");
    }

    @Override
    public void initListener() {
        mLoginListener = new IUiListener() {
            @Override
            public void onComplete(Object response) {
                if (null == response) {
                    ToastUtils.showShort("登录失败，QQ返回数据异常");
                    return;
                }
                QQLoginBean loginBean = JsonUtil.fromJson(String.valueOf(response), QQLoginBean.class);
                if (loginBean != null) {
                    ThreePartLoginParam loginParam = new ThreePartLoginParam();
                    loginParam.setAccessToken(loginBean.getAccess_token());
                    loginParam.setUserId(loginBean.getOpenid());
                    loginParam.buildHash();
                    presenter.loginByQQ(loginParam);
                } else {
                    ToastUtils.showShort("登录失败，QQ返回数据异常");
                }
            }

            @Override
            public void onError(UiError uiError) {
                ToastUtils.showShort("登录失败：" + uiError.errorMessage);
            }

            @Override
            public void onCancel() {
                ToastUtils.showShort("QQ登录取消");
            }
        };
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
            param.buildHash();
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

    @OnClick({R.id.eye_iv, R.id.login_bt, R.id.to_register_tv, R.id.to_reset_password_tv, R.id.login_qq_iv, R.id.login_weibo_iv})
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
            case R.id.login_qq_iv:
                IApplication.getTencent().login(this, "all", mLoginListener);
                break;
            case R.id.login_weibo_iv:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_LOGIN ||
                requestCode == Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode, resultCode, data, mLoginListener);
        }
    }
}
