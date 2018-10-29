package com.xyoye.dandanplay.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseFragment;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
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
import com.xyoye.dandanplay.ui.activities.SettingActivity;
import com.xyoye.dandanplay.utils.UserInfoShare;
import com.xyoye.dandanplay.ui.weight.item.PersonalFavoriteAnimaItem;
import com.xyoye.dandanplay.ui.weight.item.PersonalPlayHistoryItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class PersonalFragment extends BaseFragment<PersonalFragmentPresenter> implements PersonalFragmentView,View.OnClickListener {
    @BindView(R.id.user_info_rl)
    RelativeLayout userInfoRl;
    @BindView(R.id.user_image_iv)
    ImageView userImageIv;
    @BindView(R.id.user_name_tv)
    TextView userNameTv;
    @BindView(R.id.button_login)
    Button loginButton;
    @BindView(R.id.download_manager_rl)
    RelativeLayout downloadManagerRl;
    @BindView(R.id.favorite_rl)
    RelativeLayout favoriteRl;
    @BindView(R.id.history_rl)
    RelativeLayout historyRl;
    @BindView(R.id.favorite_recycler_view)
    RecyclerView favoriteRecyclerView;
    @BindView(R.id.history_recycler_view)
    RecyclerView historyRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refresh;

    private BaseRvAdapter<AnimeFavoriteBean.FavoritesBean> favoriteAdapter;
    private BaseRvAdapter<PlayHistoryBean.PlayHistoryAnimesBean> historyAdapter;

    public static PersonalFragment newInstance(){
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
        setHasOptionsMenu(true);
        getBaseActivity().setSupportActionBar(toolbar);
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


        if (UserInfoShare.getInstance().isLogin()){
            presenter.getFragmentData();
        }else {
            refreshUI(null, null);
        }
        changeView();
    }

    public void changeView(){
        if (UserInfoShare.getInstance().isLogin()){
            userInfoRl.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            Glide.with(this)
                    .load(UserInfoShare.getInstance().getUserImage())
                    .into(userImageIv);
            userNameTv.setText(UserInfoShare.getInstance().getUserScreenName());
        }else {
            loginButton.setVisibility(View.VISIBLE);
            userInfoRl.setVisibility(View.GONE);
        }
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Activity activity;
        activity = getActivity();
        if (activity != null)
            activity.getMenuInflater().inflate(R.menu.menu_settings, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                launchActivity(SettingActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initListener() {
        userInfoRl.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        userImageIv.setOnClickListener(this);
        downloadManagerRl.setOnClickListener(this);
        favoriteRl.setOnClickListener(this);
        historyRl.setOnClickListener(this);

        refresh.setOnRefreshListener(() ->
                presenter.getFragmentData());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.user_info_rl:
                launchActivity(PersonalInfoActivity.class);
                break;
            case R.id.button_login:
                launchActivity(LoginActivity.class);
                break;
            case R.id.user_image_iv:
                ToastUtils.showShort("头像功能暂未开放");
                break;
            case R.id.download_manager_rl:
                launchActivity(DownloadMangerActivity.class);
                break;
            case R.id.favorite_rl:
                if (UserInfoShare.getInstance().isLogin()){
                    Intent intent = new Intent(getContext(), PersonalFavoriteActivity.class);
                    startActivity(intent);
                }else {
                    ToastUtils.showShort("请登录后再进行此操作");
                }
                break;
            case R.id.history_rl:
                if (UserInfoShare.getInstance().isLogin()){
                    Intent intent = new Intent(getContext(), PersonalHistoryActivity.class);
                    startActivity(intent);
                }else {
                    ToastUtils.showShort("请登录后再进行此操作");
                }
                break;
        }
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
}
