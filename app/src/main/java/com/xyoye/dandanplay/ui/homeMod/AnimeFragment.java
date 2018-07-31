package com.xyoye.dandanplay.ui.homeMod;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseFragment;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeBeans;
import com.xyoye.dandanplay.mvp.impl.AnimePresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.AnimePresenter;
import com.xyoye.dandanplay.mvp.view.AnimaView;
import com.xyoye.dandanplay.weight.ScrollableHelper;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/15.
 */


public class AnimeFragment extends BaseFragment<AnimePresenter> implements ScrollableHelper.ScrollableContainer, AnimaView {
    @BindView(R.id.bangumi_list_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refresh;

    private BaseRvAdapter<AnimeBeans.BangumiListBean> adapter;

    public static AnimeFragment newInstance(AnimeBeans animeBeans){
        AnimeFragment animeFragment = new AnimeFragment();
        Bundle args = new Bundle();
        args.putSerializable("anima", animeBeans);
        animeFragment.setArguments(args);
        return animeFragment;
    }

    @NonNull
    @Override
    protected AnimePresenter initPresenter() {
        return new AnimePresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_anime;
    }

    @Override
    public void initView() {
        refresh.setColorSchemeResources(R.color.colorPrimary);
        //TODO:实现加载动画
        refresh.setEnabled(false);
        AnimeBeans animeBeans;
        Bundle args = getArguments();
        if (args != null){
            animeBeans = (AnimeBeans)getArguments().getSerializable("anima");
            if (animeBeans ==null) return;
        } else  return;
        adapter = new BaseRvAdapter<AnimeBeans.BangumiListBean>(animeBeans.getBangumiList()) {
            @NonNull
            @Override
            public AdapterItem<AnimeBeans.BangumiListBean> onCreateItem(int viewType) {
                return new AnimeItem();
            }
        };
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3){});
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void initListener() {

    }

    @Override
    public View getScrollableView() {
        return recyclerView;
    }
}
