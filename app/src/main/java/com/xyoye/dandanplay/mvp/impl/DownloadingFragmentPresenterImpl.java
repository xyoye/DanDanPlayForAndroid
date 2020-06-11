package com.xyoye.dandanplay.mvp.impl;

import android.arch.lifecycle.LifecycleOwner;
import android.os.Bundle;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.DownloadingFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.DownloadingFragmentView;

/**
 * Created by xyoye on 2019/8/1.
 */

public class DownloadingFragmentPresenterImpl extends BaseMvpPresenterImpl<DownloadingFragmentView> implements DownloadingFragmentPresenter {

    public DownloadingFragmentPresenterImpl(DownloadingFragmentView view, LifecycleOwner lifecycleOwner) {
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
}
