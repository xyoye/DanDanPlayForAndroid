package com.xyoye.dandanplay.mvp.impl;

import android.arch.lifecycle.LifecycleOwner;
import android.os.Bundle;

import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.SplashPresenter;
import com.xyoye.dandanplay.mvp.view.SplashView;
import com.xyoye.dandanplay.utils.AppConfig;

/**
 * Created by xyoye on 2018/7/15.
 */

public class SplashPresenterImpl extends BaseMvpPresenterImpl<SplashView> implements SplashPresenter {

    public SplashPresenterImpl(SplashView view, LifecycleOwner lifecycleOwner) {
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

    @Override
    public void checkToken() {
        if (AppConfig.getInstance().isLogin()) {
            long lastLoginTime = AppConfig.getInstance().getUpdateFilterTime();
            long nowTime = System.currentTimeMillis();

            //15天登录一次
            if (nowTime - lastLoginTime > 15 * 24 * 60 * 60 * 1000) {
                AppConfig.getInstance().setLogin(false);
                AppConfig.getInstance().saveUserName("");
                AppConfig.getInstance().saveUserImage("");
                AppConfig.getInstance().saveUserScreenName("");
                AppConfig.getInstance().saveToken("");
            }

        }
    }
}
