package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.dandanplay.mvp.presenter.MainPresenter;
import com.xyoye.dandanplay.mvp.HomeFragmentView.MainView;

/**
 * Created by YE on 2018/6/28 0028.
 */


public class MainPresenterImpl extends BaseMvpPresenter<MainView> implements MainPresenter {

    public MainPresenterImpl(MainView view, Lifeful lifeful) {
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
