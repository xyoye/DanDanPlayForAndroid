package com.xyoye.dandanplay.mvp.impl;

import android.arch.lifecycle.LifecycleOwner;
import android.os.Bundle;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.AnimePresenter;
import com.xyoye.dandanplay.mvp.view.AnimeView;

/**
 * Created by xyoye on 2018/7/15.
 */

public class AnimePresenterImpl extends BaseMvpPresenterImpl<AnimeView> implements AnimePresenter {

    public AnimePresenterImpl(AnimeView view, LifecycleOwner lifecycleOwner) {
        super(view, lifecycleOwner);
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
