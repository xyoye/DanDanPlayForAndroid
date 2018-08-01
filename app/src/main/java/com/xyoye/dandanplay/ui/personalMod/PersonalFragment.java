package com.xyoye.dandanplay.ui.personalMod;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseFragment;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.event.OpenAnimaDetailEvent;
import com.xyoye.dandanplay.mvp.impl.PersonalFragmentPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.PersonalFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PersonalFragmentView;
import com.xyoye.dandanplay.ui.animeMod.AnimeDetailActivity;
import com.xyoye.dandanplay.ui.settingMod.SettingActivity;
import com.xyoye.dandanplay.utils.UserInfoShare;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class PersonalFragment extends BaseFragment<PersonalFragmentPresenter> implements PersonalFragmentView,View.OnClickListener {
    @BindView(R.id.user_info_rl)
    ConstraintLayout userInfoRl;
    @BindView(R.id.login_rl)
    ConstraintLayout loginRl;
    @BindView(R.id.user_image_iv)
    ImageView userImageIv;
    @BindView(R.id.user_name_tv)
    TextView userNameTv;
    @BindView(R.id.button_login)
    Button loginButton;
    @BindView(R.id.more_favorite_tv)
    TextView moreFavoriteTv;
    @BindView(R.id.more_history_tv)
    TextView moreHistoryTv;
    @BindView(R.id.favorite_recycler_view)
    RecyclerView favoriteRecyclerView;
    @BindView(R.id.history_recycler_view)
    RecyclerView historyRecyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
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
        favoriteRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        historyRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
    }

    @Override
    public void changeView(){
        if (UserInfoShare.getInstance().isLogin()){
            userInfoRl.setVisibility(View.VISIBLE);
            loginRl.setVisibility(View.GONE);
            Glide.with(this)
                    .load(UserInfoShare.getInstance().getUserImage())
                    .into(userImageIv);
            userNameTv.setText(UserInfoShare.getInstance().getUserScreenName());
        }else {
            loginRl.setVisibility(View.VISIBLE);
            userInfoRl.setVisibility(View.GONE);
        }
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_settings, menu);
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
    public void refreshFavorite(AnimeFavoriteBean favoriteBean) {
        if (favoriteBean != null){
            favoriteAdapter = new BaseRvAdapter<AnimeFavoriteBean.FavoritesBean>(favoriteBean.getFavorites()) {
                @NonNull
                @Override
                public AdapterItem<AnimeFavoriteBean.FavoritesBean> onCreateItem(int viewType) {
                    return new PersonalFavoriteAnimaItem();
                }
            };
            if (favoriteRecyclerView != null)
                favoriteRecyclerView.setAdapter(favoriteAdapter);
        }else {
            if (favoriteRecyclerView != null)
                favoriteRecyclerView.removeAllViews();
            if (favoriteAdapter != null){
                favoriteAdapter.getData().clear();
                favoriteAdapter.notifyDataSetChanged();
            }
        }

    }

    @Override
    public void refreshHistory(PlayHistoryBean historyBean) {
        if (historyBean != null){
            historyAdapter = new BaseRvAdapter<PlayHistoryBean.PlayHistoryAnimesBean>(historyBean.getPlayHistoryAnimes()) {
                @NonNull
                @Override
                public AdapterItem<PlayHistoryBean.PlayHistoryAnimesBean> onCreateItem(int viewType) {
                    return new PersonalPlayHistoryItem();
                }
            };
            if (historyRecyclerView != null)
                historyRecyclerView.setAdapter(historyAdapter);
        }else {
            if (historyRecyclerView != null)
                historyRecyclerView.removeAllViews();
            if (historyAdapter != null){
                historyAdapter.getData().clear();
                favoriteAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void initListener() {
        userInfoRl.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        userImageIv.setOnClickListener(this);
        moreFavoriteTv.setOnClickListener(this);
        moreHistoryTv.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void openAnimaDeatil(OpenAnimaDetailEvent event){
        Intent intent = new Intent(getContext(), AnimeDetailActivity.class);
        intent.putExtra("animaId", event.getAnimaId());
        startActivity(intent);
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
                ToastUtils.showShort("此功能暂未开放");
                break;
            case R.id.more_favorite_tv:
                if (UserInfoShare.getInstance().isLogin()){
                    Intent intent = new Intent(getContext(), PersonalFavoriteActivity.class);
                    intent.putExtra("bean", presenter.getFavoriteBean());
                    startActivity(intent);
                }else {
                    ToastUtils.showShort("请登录后再进行此操作");
                }
                break;
            case R.id.more_history_tv:
                if (UserInfoShare.getInstance().isLogin()){
                    Intent intent = new Intent(getContext(), PersonalHistoryActivity.class);
                    intent.putExtra("bean", presenter.getPlayHistoryBean());
                    startActivity(intent);
                }else {
                    ToastUtils.showShort("请登录后再进行此操作");
                }
                break;
        }
    }

}
