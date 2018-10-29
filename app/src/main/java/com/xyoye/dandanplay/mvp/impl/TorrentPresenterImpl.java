package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.dandanplay.mvp.presenter.TorrentPresenter;
import com.xyoye.dandanplay.mvp.view.TorrentView;

/**
 * Created by xyy on 2018/10/23.
 */

public class TorrentPresenterImpl extends BaseMvpPresenter<TorrentView> implements TorrentPresenter {

    public TorrentPresenterImpl(TorrentView view, Lifeful lifeful) {
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
