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
import com.xyoye.dandanplay.mvp.impl.AnimaPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.AnimaPresenter;
import com.xyoye.dandanplay.mvp.view.AnimaView;
import com.xyoye.dandanplay.weight.ScrollableHelper;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/15.
 */


public class AnimaFragment extends BaseFragment<AnimaPresenter> implements ScrollableHelper.ScrollableContainer, AnimaView {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private BaseRvAdapter<AnimeBeans.BangumiListBean> adapter;

    public static AnimaFragment newInstance(AnimeBeans animeBeans){
        AnimaFragment animaFragment = new AnimaFragment();
        Bundle args = new Bundle();
        args.putSerializable("anima", animeBeans);
        animaFragment.setArguments(args);
        return animaFragment;
    }

    @NonNull
    @Override
    protected AnimaPresenter initPresenter() {
        return new AnimaPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_anima;
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
                return new AnimaItem();
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
