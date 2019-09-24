package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.PersonalBean;
import com.xyoye.dandanplay.bean.event.UpdateFragmentEvent;
import com.xyoye.dandanplay.bean.params.LoginParam;
import com.xyoye.dandanplay.bean.params.ThreePartLoginParam;
import com.xyoye.dandanplay.mvp.presenter.LoginPresenter;
import com.xyoye.dandanplay.mvp.view.LoginView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by xyoye on 2018/7/22.
 */

public class LoginPresenterImpl extends BaseMvpPresenterImpl<LoginView> implements LoginPresenter {

    public LoginPresenterImpl(LoginView view, Lifeful lifeful) {
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

    @Override
    public void login(LoginParam param){
        getView().showLoading();
        PersonalBean.login(param, new CommJsonObserver<PersonalBean>(getLifeful()) {
            @Override
            public void onSuccess(PersonalBean personalBean) {
                getView().hideLoading();
                AppConfig.getInstance().setLogin(true);
                AppConfig.getInstance().saveUserScreenName(personalBean.getScreenName());
                AppConfig.getInstance().saveUserName(param.getUserName());
                AppConfig.getInstance().saveUserImage(personalBean.getProfileImage());
                AppConfig.getInstance().saveToken(personalBean.getToken());
                AppConfig.getInstance().setLastLoginTime(System.currentTimeMillis());
                ToastUtils.showShort("登录成功");
                EventBus.getDefault().post(UpdateFragmentEvent.updatePersonal());
                getView().launchMain();
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                LogUtils.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }

    @Override
    public void loginByQQ(ThreePartLoginParam param) {
        param.setSource("qq");
        loginThreePart(param);
    }

    @Override
    public void loginByWB(ThreePartLoginParam param) {
        param.setSource("weibo");
        loginThreePart(param);
    }

    private void loginThreePart(ThreePartLoginParam param){
        getView().showLoading();
        PersonalBean.loginByThreePart(param, new CommJsonObserver<PersonalBean>(getLifeful()) {
            @Override
            public void onSuccess(PersonalBean personalBean) {
                getView().hideLoading();
                AppConfig.getInstance().setLogin(true);
                AppConfig.getInstance().saveUserScreenName(personalBean.getScreenName());
                AppConfig.getInstance().saveUserName("");
                AppConfig.getInstance().saveUserImage(personalBean.getProfileImage());
                AppConfig.getInstance().saveToken(personalBean.getToken());
                AppConfig.getInstance().setLastLoginTime(System.currentTimeMillis());
                ToastUtils.showShort("登录成功");
                EventBus.getDefault().post(UpdateFragmentEvent.updatePersonal());
                getView().launchMain();
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                LogUtils.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }
}
