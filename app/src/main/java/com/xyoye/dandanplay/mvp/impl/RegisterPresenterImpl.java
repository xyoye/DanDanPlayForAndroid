package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.RegisterBean;
import com.xyoye.dandanplay.bean.params.RegisterParam;
import com.xyoye.dandanplay.mvp.presenter.RegisterPresenter;
import com.xyoye.dandanplay.mvp.view.RegisterView;
import com.xyoye.dandanplay.utils.SoUtils;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

/**
 * Created by xyoye on 2018/8/5.
 */

public class RegisterPresenterImpl extends BaseMvpPresenterImpl<RegisterView> implements RegisterPresenter {

    public RegisterPresenterImpl(RegisterView view, Lifeful lifeful) {
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
    public void register(RegisterParam param) {
        getView().showLoading();
        param.setScreenName(param.getUserName());
        param.setAppId(SoUtils.getInstance().getDanDanAppId());
        param.setUnixTimestamp(System.currentTimeMillis()/1000);
        param.buildHash(getView().getRegisterContext());
        RegisterBean.register(param, new CommJsonObserver<RegisterBean>(getLifeful()) {
            @Override
            public void onSuccess(RegisterBean registerBean) {
                getView().hideLoading();
                getView().registerSuccess();
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
