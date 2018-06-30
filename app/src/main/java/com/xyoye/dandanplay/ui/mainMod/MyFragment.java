package com.xyoye.dandanplay.ui.mainMod;

import android.support.annotation.NonNull;

import com.xyoye.core.base.BaseFragment;
import com.xyoye.core.interf.presenter.BasePresenter;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.mvp.view.MyFragmentView;
import com.xyoye.dandanplay.mvp.impl.MyFragmentPresenterImpl;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class MyFragment extends BaseFragment implements MyFragmentView{

    public static MyFragment newInstance(){
        return new MyFragment();
    }

    @NonNull
    @Override
    protected BasePresenter initPresenter() {
        return new MyFragmentPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_my;
    }

    @Override
    public void initView() {

    }

    @Override
    public void initListener() {

    }
}
