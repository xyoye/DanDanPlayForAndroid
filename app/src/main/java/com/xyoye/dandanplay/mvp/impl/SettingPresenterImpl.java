package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import androidx.lifecycle.LifecycleOwner;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.SettingPresenter;
import com.xyoye.dandanplay.mvp.view.SettingView;

/**
 * Created by xyoye on 2018/7/24.
 */

public class SettingPresenterImpl extends BaseMvpPresenterImpl<SettingView> implements SettingPresenter {

    public SettingPresenterImpl(SettingView view, LifecycleOwner lifecycleOwner) {
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
