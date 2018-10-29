package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.bean.PersonalBean;
import com.xyoye.dandanplay.mvp.presenter.OpenPresenter;
import com.xyoye.dandanplay.mvp.view.OpenView;
import com.xyoye.dandanplay.utils.TokenShare;
import com.xyoye.dandanplay.utils.UserInfoShare;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

/**
 * Created by YE on 2018/7/15.
 */

public class OpenPresenterImpl extends BaseMvpPresenter<OpenView> implements OpenPresenter {

    public OpenPresenterImpl(OpenView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {
        //判断用户上次是否登录
        if (UserInfoShare.getInstance().isLogin()){
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
                UserInfoShare.getInstance().setLogin(true);
                UserInfoShare.getInstance().saveUserScreenName(personalBean.getScreenName());
                UserInfoShare.getInstance().saveUserImage(personalBean.getProfileImage());
                TokenShare.getInstance().saveToken(personalBean.getToken());
                getView().launch(false);
            }

            @Override
            public void onError(int errorCode, String message) {
                UserInfoShare.getInstance().setLogin(false);
                UserInfoShare.getInstance().saveUserName("");
                UserInfoShare.getInstance().saveUserImage("");
                UserInfoShare.getInstance().saveUserScreenName("");
                TokenShare.getInstance().saveToken("");
                TLog.e(message);
                getView().launch(true);
            }
        }, new NetworkConsumer());
    }
}
