package com.xyoye.dandanplay.ui.mainMod;

import android.support.annotation.NonNull;

import com.xyoye.core.base.BaseFragment;
import com.xyoye.core.interf.presenter.BasePresenter;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.mvp.HomeFragmentView.PlayFragmentView;
import com.xyoye.dandanplay.mvp.impl.PlayFragmentPresenterImpl;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class PlayFragment extends BaseFragment implements PlayFragmentView{

    public static PlayFragment newInstance(){
        return new PlayFragment();
    }

    @NonNull
    @Override
    protected BasePresenter initPresenter() {
        return new PlayFragmentPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutId() {
        return R.layout.fragment_play;
    }

    @Override
    public void initView() {

    }

    @Override
    public void initListener() {

    }
}
