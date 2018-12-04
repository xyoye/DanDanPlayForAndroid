package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.LanVideoPresenter;
import com.xyoye.dandanplay.mvp.view.LanVideoView;
import com.xyoye.dandanplay.utils.Lifeful;

/**
 * Created by xyy on 2018/11/22.
 */

public class LanVideoPresenterImpl extends BaseMvpPresenterImpl<LanVideoView> implements LanVideoPresenter {

    public LanVideoPresenterImpl(LanVideoView view, Lifeful lifeful) {
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
