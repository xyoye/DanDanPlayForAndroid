package com.xyoye.dandanplay.ui.personalMod;

import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xyoye.core.base.BaseActivity;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.mvp.impl.PersonalInfoPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PersonalInfoPresenter;
import com.xyoye.dandanplay.mvp.view.PersonalInfoView;
import com.xyoye.dandanplay.utils.TokenShare;
import com.xyoye.dandanplay.utils.UserInfoShare;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/23.
 */


public class PersonalInfoActivity extends BaseActivity<PersonalInfoPresenter> implements PersonalInfoView {
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.login_out_bt)
    Button loginOutBt;


    @Override
    public void initView() {
        setTitle("");
        toolbarTitle.setText("个人信息");
    }

    @Override
    public void initListener() {
        loginOutBt.setOnClickListener(v -> {
            UserInfoShare.getInstance().setLogin(false);
            UserInfoShare.getInstance().saveUserName("");
            UserInfoShare.getInstance().saveUserScreenName("");
            UserInfoShare.getInstance().saveUserImage("");
            TokenShare.getInstance().saveToken("");

            launchActivity(LoginActivity.class);
            PersonalInfoActivity.this.finish();
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
}
