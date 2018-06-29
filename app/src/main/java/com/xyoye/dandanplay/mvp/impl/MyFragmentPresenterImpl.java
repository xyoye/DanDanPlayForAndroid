package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.dandanplay.mvp.HomeFragmentView.MyFragmentView;
import com.xyoye.dandanplay.mvp.presenter.MyFragmentPresenter;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class MyFragmentPresenterImpl extends BaseMvpPresenter<MyFragmentView> implements MyFragmentPresenter {

    public MyFragmentPresenterImpl(MyFragmentView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {

    }

    @Override
    public void process(Bundle savedInstanceState) {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }
}
