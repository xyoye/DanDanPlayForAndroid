package com.xyoye.dandanplay.ui.fragment;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseFragment;
import com.xyoye.dandanplay.mvp.impl.PersonalFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PersonalFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PersonalFragmentView;
import com.xyoye.dandanplay.ui.activities.DownloadMangerActivity;
import com.xyoye.dandanplay.ui.activities.LoginActivity;
import com.xyoye.dandanplay.ui.activities.PersonalFavoriteActivity;
import com.xyoye.dandanplay.ui.activities.PersonalHistoryActivity;
import com.xyoye.dandanplay.ui.activities.PersonalInfoActivity;
import com.xyoye.dandanplay.ui.activities.PlayerSettingActivity;
import com.xyoye.dandanplay.ui.activities.SettingActivity;
import com.xyoye.dandanplay.ui.activities.VideoScanActivity;
import com.xyoye.dandanplay.utils.AppConfig;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2018/6/29.
 */

public class PersonalFragment extends BaseFragment<PersonalFragmentPresenter> implements PersonalFragmentView{
    @BindView(R.id.user_image_iv)
    ImageView userImageIv;
    @BindView(R.id.user_name_tv)
    TextView userNameTv;
    @BindView(R.id.button_login)
    TextView loginButton;

    public static PersonalFragment newInstance() {
        return new PersonalFragment();
    }

    @NonNull
    @Override
    protected PersonalFragmentPresenter initPresenter() {
        return new PersonalFragmentPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_personal;
    }

    @Override
    public void initView() {

    }

    public void changeView() {
        if (AppConfig.getInstance().isLogin()) {
            loginButton.setVisibility(View.GONE);
            RequestOptions options = new RequestOptions()
                    .error(R.mipmap.default_image)
                    .placeholder(R.mipmap.default_image);

            Glide.with(this)
                    .load(AppConfig.getInstance().getUserImage())
                    .apply(options)
                    .into(userImageIv);
            userNameTv.setText(AppConfig.getInstance().getUserScreenName());
        } else {
            loginButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void initListener() {

    }

    @Override
    public void onSupportVisible() {
        if (IApplication.isUpdateUserInfo) {
            changeView();
        }
    }

    @OnClick({R.id.user_image_iv, R.id.user_info_rl, R.id.button_login, R.id.player_setting_ll, R.id.app_setting_ll, R.id.scan_setting_ll, R.id.download_setting_ll, R.id.favorite_ll, R.id.history_ll})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.player_setting_ll:
                launchActivity(PlayerSettingActivity.class);
                break;
            case R.id.app_setting_ll:
                launchActivity(SettingActivity.class);
                break;
            case R.id.scan_setting_ll:
                launchActivity(VideoScanActivity.class);
                break;
            case R.id.download_setting_ll:
                launchActivity(DownloadMangerActivity.class);
                break;
            case R.id.user_info_rl:
                launchActivity(PersonalInfoActivity.class);
                break;
            case R.id.button_login:
                launchActivity(LoginActivity.class);
                break;
            case R.id.user_image_iv:
                ToastUtils.showShort("头像功能暂未开放");
                break;
            case R.id.favorite_ll:
                if (AppConfig.getInstance().isLogin()) {
                    launchActivity(PersonalFavoriteActivity.class);
                } else {
                    launchActivity(LoginActivity.class);
                }
                break;
            case R.id.history_ll:
                if (AppConfig.getInstance().isLogin()) {
                    launchActivity(PersonalHistoryActivity.class);
                } else {
                    launchActivity(LoginActivity.class);
                }
                break;
        }
    }
}
