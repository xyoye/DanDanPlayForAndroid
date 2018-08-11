package com.xyoye.dandanplay.ui.personalMod;

import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.event.ChangeScreenNameEvent;
import com.xyoye.dandanplay.mvp.impl.PersonalInfoPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PersonalInfoPresenter;
import com.xyoye.dandanplay.mvp.view.PersonalInfoView;
import com.xyoye.dandanplay.ui.authMod.ChangePasswordActivity;
import com.xyoye.dandanplay.ui.authMod.ChangeScreenNameDialog;
import com.xyoye.dandanplay.ui.authMod.LoginActivity;
import com.xyoye.dandanplay.utils.TokenShare;
import com.xyoye.dandanplay.utils.UserInfoShare;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/23.
 */


public class PersonalInfoActivity extends BaseActivity<PersonalInfoPresenter> implements PersonalInfoView,View.OnClickListener {
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
        if (UserInfoShare.getInstance().isLogin()){
            String screenName = UserInfoShare.getInstance().getUserScreenName();
            String userName = UserInfoShare.getInstance().getUserName();

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
            if (UserInfoShare.getInstance().isLogin()){
                UserInfoShare.getInstance().setLogin(false);
                UserInfoShare.getInstance().saveUserName("");
                UserInfoShare.getInstance().saveUserScreenName("");
                UserInfoShare.getInstance().saveUserImage("");
                TokenShare.getInstance().saveToken("");

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
                ChangeScreenNameDialog dialog = new ChangeScreenNameDialog(PersonalInfoActivity.this, R.style.Dialog);
                dialog.show();
                break;
            case R.id.password_rl:
                launchActivity(ChangePasswordActivity.class);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChangeScreenNameEvent event){
        screenNameTv.setText(event.getScreenName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
}
