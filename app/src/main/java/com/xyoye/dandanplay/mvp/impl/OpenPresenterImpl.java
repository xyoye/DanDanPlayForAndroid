package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.PersonalBean;
import com.xyoye.dandanplay.mvp.presenter.OpenPresenter;
import com.xyoye.dandanplay.mvp.view.OpenView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

/**
 * Created by xyoye on 2018/7/15.
 */

public class OpenPresenterImpl extends BaseMvpPresenterImpl<OpenView> implements OpenPresenter {

    public OpenPresenterImpl(OpenView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {
        //判断用户上次是否登录
        if (AppConfig.getInstance().isLogin()){
            reToken();
        }else {
            getView().launch(false);
        }
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

    private void reToken(){
        PersonalBean.reToken(new CommJsonObserver<PersonalBean>(getLifeful()) {
            @Override
            public void onSuccess(PersonalBean personalBean) {
                AppConfig.getInstance().setLogin(true);
                AppConfig.getInstance().saveUserScreenName(personalBean.getScreenName());
                AppConfig.getInstance().saveUserImage(personalBean.getProfileImage());
                AppConfig.getInstance().saveToken(personalBean.getToken());
                getView().launch(false);
            }

            @Override
            public void onError(int errorCode, String message) {
                AppConfig.getInstance().setLogin(false);
                AppConfig.getInstance().saveUserName("");
                AppConfig.getInstance().saveUserImage("");
                AppConfig.getInstance().saveUserScreenName("");
                AppConfig.getInstance().saveToken("");
                LogUtils.e(message);
                getView().launch(true);
            }
        }, new NetworkConsumer());
    }
}
