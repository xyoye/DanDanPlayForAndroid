package com.xyoye.dandanplay.ui.mainMod;

import android.support.annotation.NonNull;

import com.xyoye.core.base.BaseFragment;
import com.xyoye.core.interf.presenter.BasePresenter;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.mvp.view.HomeFragmentView;
import com.xyoye.dandanplay.mvp.impl.HomeFragmentPresenterImpl;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class HomeFragment extends BaseFragment implements HomeFragmentView{

    public static HomeFragment newInstance(){
        return new HomeFragment();
    }

    @NonNull
    @Override
    protected BasePresenter initPresenter() {
        return new HomeFragmentPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public void initView() {

    }

    @Override
    public void initListener() {

    }
}
