package com.xyoye.dandanplay.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseFragment;
import com.xyoye.dandanplay.base.BaseRvAdapter;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
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
import com.xyoye.dandanplay.ui.weight.item.PersonalFavoriteAnimaItem;
import com.xyoye.dandanplay.ui.weight.item.PersonalPlayHistoryItem;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.interf.AdapterItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by xyoye on 2018/6/29.
 */

public class PersonalFragment extends BaseFragment<PersonalFragmentPresenter> implements PersonalFragmentView{
    @BindView(R.id.user_info_rl)
    RelativeLayout userInfoRl;
    @BindView(R.id.user_image_iv)
    ImageView userImageIv;
    @BindView(R.id.user_name_tv)
    TextView userNameTv;
    @BindView(R.id.button_login)
    TextView loginButton;
    @BindView(R.id.favorite_rl)
    RelativeLayout favoriteRl;
    @BindView(R.id.history_rl)
    RelativeLayout historyRl;
    @BindView(R.id.favorite_recycler_view)
    RecyclerView favoriteRecyclerView;
    @BindView(R.id.history_recycler_view)
    RecyclerView historyRecyclerView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refresh;

    private BaseRvAdapter<AnimeFavoriteBean.FavoritesBean> favoriteAdapter;
    private BaseRvAdapter<PlayHistoryBean.PlayHistoryAnimesBean> historyAdapter;

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
        refresh.setColorSchemeResources(R.color.theme_color);

        favoriteRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        historyRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        favoriteAdapter = new BaseRvAdapter<AnimeFavoriteBean.FavoritesBean>(new ArrayList<>()) {
            @NonNull
            @Override
            public AdapterItem<AnimeFavoriteBean.FavoritesBean> onCreateItem(int viewType) {
                return new PersonalFavoriteAnimaItem();
            }
        };
        favoriteRecyclerView.setAdapter(favoriteAdapter);

        historyAdapter = new BaseRvAdapter<PlayHistoryBean.PlayHistoryAnimesBean>(new ArrayList<>()) {
            @NonNull
            @Override
            public AdapterItem<PlayHistoryBean.PlayHistoryAnimesBean> onCreateItem(int viewType) {
                return new PersonalPlayHistoryItem();
            }
        };
        historyRecyclerView.setAdapter(historyAdapter);
    }

    public void changeView() {
        if (AppConfig.getInstance().isLogin()) {
            userInfoRl.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            Glide.with(this)
                    .load(AppConfig.getInstance().getUserImage())
                    .into(userImageIv);
            userNameTv.setText(AppConfig.getInstance().getUserScreenName());
        } else {
            loginButton.setVisibility(View.VISIBLE);
            userInfoRl.setVisibility(View.GONE);
        }
    }

    @Override
    public void initListener() {
        refresh.setOnRefreshListener(() -> {
            if (AppConfig.getInstance().isLogin()) {
                presenter.getFragmentData();
            } else {
                refresh.setRefreshing(false);
            }
        });
    }

    @Override
    public void refreshFavorite(AnimeFavoriteBean favoriteBean) {
        favoriteAdapter.setData(favoriteBean == null
                ? new ArrayList<>()
                : favoriteBean.getFavorites());
        favoriteAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshHistory(PlayHistoryBean historyBean) {
        historyAdapter.setData(historyBean == null
                ? new ArrayList<>()
                : historyBean.getPlayHistoryAnimes());
        historyAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshUI(AnimeFavoriteBean favoriteBean, PlayHistoryBean historyBean) {
        favoriteAdapter.setData(favoriteBean == null
                ? new ArrayList<>()
                : favoriteBean.getFavorites());
        favoriteAdapter.notifyDataSetChanged();

        historyAdapter.setData(historyBean == null
                ? new ArrayList<>()
                : historyBean.getPlayHistoryAnimes());
        historyAdapter.notifyDataSetChanged();
        if (refresh != null)
            refresh.setRefreshing(false);
    }

    @Override
    public void onSupportVisible() {
        if (IApplication.isUpdateUserInfo) {
            IApplication.isUpdateUserInfo = false;
            if (AppConfig.getInstance().isLogin()) {
                refresh.setRefreshing(true);
                presenter.getFragmentData();
            } else {
                refreshUI(null, null);
            }
            changeView();
        }
    }

    @OnClick({R.id.user_image_iv, R.id.user_info_rl, R.id.button_login, R.id.player_setting_ll, R.id.app_setting_ll, R.id.scan_setting_ll, R.id.download_setting_ll, R.id.favorite_rl, R.id.history_rl})
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
            case R.id.favorite_rl:
                if (AppConfig.getInstance().isLogin()) {
                    launchActivity(PersonalFavoriteActivity.class);
                } else {
                    launchActivity(LoginActivity.class);
                }
                break;
            case R.id.history_rl:
                if (AppConfig.getInstance().isLogin()) {
                    launchActivity(PersonalHistoryActivity.class);
                } else {
                    launchActivity(LoginActivity.class);
                }
                break;
        }
    }
}
