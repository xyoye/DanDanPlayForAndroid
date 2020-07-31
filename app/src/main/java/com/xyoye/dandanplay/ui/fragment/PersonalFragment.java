package com.xyoye.dandanplay.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpFragment;
import com.xyoye.dandanplay.bean.event.UpdateFragmentEvent;
import com.xyoye.dandanplay.mvp.impl.PersonalFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PersonalFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PersonalFragmentView;
import com.xyoye.dandanplay.ui.activities.ShellActivity;
import com.xyoye.dandanplay.ui.activities.anime.AnimeListActivity;
import com.xyoye.dandanplay.ui.activities.personal.CommonQuestionActivity;
import com.xyoye.dandanplay.ui.activities.personal.DownloadBiliBiliActivity;
import com.xyoye.dandanplay.ui.activities.personal.DownloadManagerActivity;
import com.xyoye.dandanplay.ui.activities.personal.LocalPlayHistoryActivity;
import com.xyoye.dandanplay.ui.activities.personal.LoginActivity;
import com.xyoye.dandanplay.ui.activities.personal.PersonalInfoActivity;
import com.xyoye.dandanplay.ui.activities.personal.ShooterSubActivity;
import com.xyoye.dandanplay.ui.fragment.settings.AppSettingFragment;
import com.xyoye.dandanplay.ui.activities.setting.DanmuBlockManagerActivity;
import com.xyoye.dandanplay.ui.fragment.settings.DownloadSettingFragment;
import com.xyoye.dandanplay.ui.fragment.settings.PlaySettingFragment;
import com.xyoye.dandanplay.ui.activities.setting.ScanManagerManagerActivity;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.ui.weight.SwitchThemeAnimView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;
import skin.support.SkinCompatManager;
import skin.support.utils.SkinPreference;

/**
 * Created by xyoye on 2018/6/29.
 */

public class PersonalFragment extends BaseMvpFragment<PersonalFragmentPresenter> implements PersonalFragmentView {
    @BindView(R.id.user_image_iv)
    ImageView userImageIv;
    @BindView(R.id.user_name_tv)
    TextView userNameTv;
    @BindView(R.id.button_login)
    TextView loginButton;
    @BindView(R.id.skin_iv)
    ImageView skinIv;
    @BindView(R.id.skin_tv)
    TextView skinTv;

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
        if (AppConfig.getInstance().isLogin()) {
            loginButton.setVisibility(View.GONE);
            RequestOptions options = new RequestOptions()
                    .error(R.mipmap.default_image)
                    .placeholder(R.mipmap.default_image);

            Glide.with(this)
                    .load(AppConfig.getInstance().getUserImage())
                    .apply(options)
                    .transition((DrawableTransitionOptions.withCrossFade()))
                    .into(userImageIv);
            userNameTv.setText(AppConfig.getInstance().getUserScreenName());
        } else {
            loginButton.setVisibility(View.VISIBLE);
        }

        if (isLoadedSkin()) {
            skinIv.setImageResource(R.mipmap.ic_skin_light);
            skinTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_orange));
            skinTv.setText("日间模式");
        } else {
            skinIv.setImageResource(R.mipmap.ic_skin_dark);
            skinTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_black));
            skinTv.setText("夜间模式");
        }
    }

    @Override
    public void initListener() {

    }

    @OnClick({R.id.user_image_iv, R.id.user_info_rl, R.id.button_login,
            R.id.player_setting_ll, R.id.download_setting_ll, R.id.system_setting_ll,
            R.id.follow_ll, R.id.network_history_ll, R.id.local_history_ll, R.id.download_manager_ll,
            R.id.video_scan_manager_ll, R.id.danmu_block_manager_ll, R.id.common_question_ll, R.id.skin_ll,
            R.id.bilibili_danmu_download_ll, R.id.subtitle_download_ll})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.user_image_iv:
                ToastUtils.showShort("头像功能暂未开放");
                break;
            case R.id.user_info_rl:
                launchActivity(PersonalInfoActivity.class);
                break;
            case R.id.button_login:
                launchActivity(LoginActivity.class);
                break;
            case R.id.player_setting_ll:
                Bundle player = new Bundle();
                player.putString("fragment", PlaySettingFragment.class.getName());
                launchActivity(ShellActivity.class,player);
                break;
            case R.id.download_setting_ll:
                Bundle download = new Bundle();
                download.putString("fragment", DownloadSettingFragment.class.getName());
                launchActivity(ShellActivity.class,download);
                break;
            case R.id.system_setting_ll:
                Bundle app = new Bundle();
                app.putString("fragment",AppSettingFragment.class.getName());
                launchActivity(ShellActivity.class,app);
                break;
            case R.id.follow_ll:
                if (AppConfig.getInstance().isLogin()) {
                    AnimeListActivity.launchAnimeList(getContext(), AnimeListActivity.PERSONAL_FAVORITE);
                } else {
                    launchActivity(LoginActivity.class);
                }
                break;
            case R.id.network_history_ll:
                if (AppConfig.getInstance().isLogin()) {
                    AnimeListActivity.launchAnimeList(getContext(), AnimeListActivity.PERSONAL_HISTORY);
                } else {
                    launchActivity(LoginActivity.class);
                }
                break;
            case R.id.local_history_ll:
                launchActivity(LocalPlayHistoryActivity.class);
                break;
            case R.id.download_manager_ll:
                Bundle bundle = new Bundle();
                bundle.putInt("fragment_position", 0);
                launchActivity(DownloadManagerActivity.class, bundle);
                break;
            case R.id.video_scan_manager_ll:
                launchActivity(ScanManagerManagerActivity.class);
                break;
            case R.id.danmu_block_manager_ll:
                launchActivity(DanmuBlockManagerActivity.class);
                break;
            case R.id.common_question_ll:
                launchActivity(CommonQuestionActivity.class);
                break;
            case R.id.skin_ll:
                switchSkin();
                break;
            case R.id.bilibili_danmu_download_ll:
                launchActivity(DownloadBiliBiliActivity.class);
                break;
            case R.id.subtitle_download_ll:
                launchActivity(ShooterSubActivity.class);
                break;
        }
    }

    /**
     * 切换皮肤
     */
    private void switchSkin() {
        EventBus.getDefault().post(UpdateFragmentEvent.updatePlay(PlayFragment.UPDATE_DATABASE_DATA));
        SwitchThemeAnimView.create((Activity) mContext, skinIv).setDuration(800).start();
        if (isLoadedSkin()) {
            SkinCompatManager.getInstance()
                    .restoreDefaultTheme();
            skinIv.setImageResource(R.mipmap.ic_skin_dark);
            skinTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_black));
            skinTv.setText("夜间模式");
        } else {
            SkinCompatManager.getInstance()
                    .loadSkin("night", SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN);
            skinIv.setImageResource(R.mipmap.ic_skin_light);
            skinTv.setTextColor(CommonUtils.getResColor(R.color.immutable_text_orange));
            skinTv.setText("日间模式");
        }
    }

    /**
     * 是否已换肤
     */
    private boolean isLoadedSkin() {
        switch (SkinPreference.getInstance().getSkinStrategy()) {
            case SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS:
            case SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN:
            case SkinCompatManager.SKIN_LOADER_STRATEGY_PREFIX_BUILD_IN:
                return true;
            case SkinCompatManager.SKIN_LOADER_STRATEGY_NONE:
            default:
                return false;
        }
    }
}
