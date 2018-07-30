package com.xyoye.dandanplay.ui.homeMod;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
        AnimeBeans animeBeans;
        Bundle args = getArguments();
        if (args != null){
            animeBeans = (AnimeBeans)getArguments().getSerializable("anima");
            if (animeBeans ==null) return;
        } else  return;

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        adapter = new BaseRvAdapter<AnimeBeans.BangumiListBean>(animeBeans.getBangumiList()) {
            @NonNull
            @Override
            public AdapterItem<AnimeBeans.BangumiListBean> onCreateItem(int viewType) {
                return new AnimeItem();
            }
        };
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
