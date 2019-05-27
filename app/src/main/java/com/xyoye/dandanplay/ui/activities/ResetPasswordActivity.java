package com.xyoye.dandanplay.ui.activities;

import android.support.annotation.NonNull;
import android.widget.EditText;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.bean.params.ResetPasswordParam;
import com.xyoye.dandanplay.mvp.impl.ResetPasswordPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.ResetPasswordPresenter;
import com.xyoye.dandanplay.mvp.view.ResetPasswordView;
import com.xyoye.dandanplay.ui.weight.dialog.ToLoginDialog;
import com.xyoye.dandanplay.utils.KeyUtil;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2018/8/11.
 */

public class ResetPasswordActivity extends BaseMvpActivity<ResetPasswordPresenter> implements ResetPasswordView {

    @BindView(R.id.account_et)
    EditText accountEt;
    @BindView(R.id.email_et)
    EditText emailEt;

    @Override
    public void initView() {
        setTitle("重置密码");
    }

    @Override
    public void initListener() {

    }

    @NonNull
    @Override
    protected ResetPasswordPresenter initPresenter() {
        return new ResetPasswordPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_reset_password;
    }

    private void reset() {
        String userName = accountEt.getText().toString();
        String email = emailEt.getText().toString();

        if (StringUtils.isEmpty(userName)) {
            ToastUtils.showShort("用户名不能为空");
        } else if (StringUtils.isEmpty(email)) {
            ToastUtils.showShort("邮箱不能为空");
        } else {
            ResetPasswordParam param = new ResetPasswordParam();
            param.setUserName(userName);
            param.setEmail(email);
            param.setAppId(KeyUtil.getDanDanAppId(this));
            param.setUnixTimestamp(System.currentTimeMillis() / 1000);
            param.buildHash(this);
            presenter.reset(param);
        }
    }

    @Override
    public void resetSuccess() {
        IApplication.isUpdateUserInfo = true;
        ToLoginDialog dialog = new ToLoginDialog(this, R.style.Dialog, 1, ResetPasswordActivity.this::finish);
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

    @OnClick(R.id.reset_bt)
    public void onViewClicked() {
        reset();
    }
}
