package com.xyoye.dandanplay.mvp.impl;

import android.arch.lifecycle.LifecycleOwner;
import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.PersonalBean;
import com.xyoye.dandanplay.bean.params.FindAccountParam;
import com.xyoye.dandanplay.bean.params.ResetPasswordParam;
import com.xyoye.dandanplay.mvp.presenter.ResetPasswordPresenter;
import com.xyoye.dandanplay.mvp.view.ResetPasswordView;
import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

/**
 * Created by xyoye on 2018/8/11.
 */

public class ResetPasswordPresenterImpl extends BaseMvpPresenterImpl<ResetPasswordView> implements ResetPasswordPresenter {

    public ResetPasswordPresenterImpl(ResetPasswordView view, LifecycleOwner lifecycleOwner) {
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
    public void resetPassword(ResetPasswordParam param) {
        getView().showLoading();
        PersonalBean.resetPassword(param, new CommJsonObserver<CommJsonEntity>(getLifecycle()) {
            @Override
            public void onSuccess(CommJsonEntity commJsonEntity) {
                getView().hideLoading();
                getView().resetSuccess();
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
    public void findAccount(FindAccountParam param) {
        getView().showLoading();
        PersonalBean.findAccountByEmail(param, new CommJsonObserver<CommJsonEntity>(getLifecycle()) {
            @Override
            public void onSuccess(CommJsonEntity commJsonEntity) {
                getView().hideLoading();
                getView().findAccountSuccess();
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
