package com.xyoye.dandanplay.ui.activities.personal;

import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.mvp.impl.PersonalInfoPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PersonalInfoPresenter;
import com.xyoye.dandanplay.mvp.view.PersonalInfoView;
import com.xyoye.dandanplay.ui.weight.dialog.CommonEditTextDialog;
import com.xyoye.dandanplay.utils.AppConfig;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/7/23.
 */

public class PersonalInfoActivity extends BaseMvpActivity<PersonalInfoPresenter> implements PersonalInfoView,View.OnClickListener {
    @BindView(R.id.login_out_bt)
    Button loginOutBt;
    @BindView(R.id.screen_name_rl)
    RelativeLayout screenNameRl;
    @BindView(R.id.password_rl)
    RelativeLayout passwordRl;
    @BindView(R.id.user_name_tv)
    TextView userNameTv;
    @BindView(R.id.screen_name_tv)
    TextView screenNameTv;


    @Override
    public void initView() {
        setTitle("个人信息");
        if (AppConfig.getInstance().isLogin()){
            String screenName = AppConfig.getInstance().getUserScreenName();
            String userName = AppConfig.getInstance().getUserName();

            screenNameTv.setText(screenName);
            userNameTv.setText(userName);
        }else {
            ToastUtils.showShort("请先登录再进行此操作");
        }
    }

    @Override
    public void initListener() {
        screenNameRl.setOnClickListener(this);
        passwordRl.setOnClickListener(this);

        loginOutBt.setOnClickListener(v -> {
            if (AppConfig.getInstance().isLogin()){
                AppConfig.getInstance().setLogin(false);
                AppConfig.getInstance().saveUserName("");
                AppConfig.getInstance().saveUserScreenName("");
                AppConfig.getInstance().saveUserImage("");
                AppConfig.getInstance().saveToken("");
                IApplication.isUpdateUserInfo = true;

                launchActivity(LoginActivity.class);
                PersonalInfoActivity.this.finish();
            }else {
                ToastUtils.showShort("请先登录再进行此操作");
            }

        });
    }

    @NonNull
    @Override
    protected PersonalInfoPresenter initPresenter() {
        return new PersonalInfoPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_personal_info;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.screen_name_rl:
                new CommonEditTextDialog(PersonalInfoActivity.this, CommonEditTextDialog.SCREEN_NAME, data -> {
                    screenNameTv.setText(data[0]);
                    AppConfig.getInstance().saveUserScreenName(data[0]);
                    IApplication.isUpdateUserInfo = true;
                    ToastUtils.showShort("修改昵称成功");
                }).show();
                break;
            case R.id.password_rl:
                launchActivity(ChangePasswordActivity.class);
                break;
        }
    }
}
