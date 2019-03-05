package com.xyoye.dandanplay.ui.activities;

import android.support.annotation.NonNull;
import android.widget.EditText;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.bean.params.ChangePasswordParam;
import com.xyoye.dandanplay.mvp.impl.ChangePasswordPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.ChangePasswordPresenter;
import com.xyoye.dandanplay.mvp.view.ChangePasswordView;
import com.xyoye.dandanplay.ui.weight.dialog.ToLoginDialog;
import com.xyoye.dandanplay.utils.AppConfig;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by YE on 2018/8/11.
 */

public class ChangePasswordActivity extends BaseMvpActivity<ChangePasswordPresenter> implements ChangePasswordView {

    @BindView(R.id.old_password_et)
    EditText oldPasswordEt;
    @BindView(R.id.new_password_et)
    EditText newPasswordEt;

    @Override
    public void initView() {
        setTitle("修改密码");
    }

    @Override
    public void initListener() {

    }

    @NonNull
    @Override
    protected ChangePasswordPresenter initPresenter() {
        return new ChangePasswordPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_change_password;
    }

    private void change() {

        String oldPassword = oldPasswordEt.getText().toString();
        String newPassword = newPasswordEt.getText().toString();

        if (StringUtils.isEmpty(oldPassword)) {
            ToastUtils.showShort("旧密码不能为空");
            return;
        }
        if (StringUtils.isEmpty(newPassword)) {
            ToastUtils.showShort("新密码不能为空");
            return;
        }
        if (oldPassword.length() < 5 || oldPassword.length() > 20) {
            ToastUtils.showShort("旧密码长度为5-20个字符");
            return;
        }
        if (newPassword.length() < 5 || newPassword.length() > 20) {
            ToastUtils.showShort("新密码长度为5-20个字符");
            return;
        }
        presenter.change(new ChangePasswordParam(oldPassword, newPassword));
    }

    @Override
    public void changeSuccess() {
        ToLoginDialog dialog = new ToLoginDialog(this, R.style.Dialog, 2, () -> {
            AppConfig.getInstance().setLogin(false);
            AppConfig.getInstance().saveUserName("");
            AppConfig.getInstance().saveUserScreenName("");
            AppConfig.getInstance().saveUserImage("");
            AppConfig.getInstance().saveToken("");
            if (ActivityUtils.isActivityExistsInStack(PersonalInfoActivity.class))
                ActivityUtils.finishActivity(PersonalInfoActivity.class);
            IApplication.isUpdateUserInfo = true;
            launchActivity(LoginActivity.class);
            ChangePasswordActivity.this.finish();
        });
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
        change();
    }
}
