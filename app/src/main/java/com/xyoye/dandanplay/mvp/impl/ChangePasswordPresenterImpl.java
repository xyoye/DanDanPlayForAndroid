package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.PersonalBean;
import com.xyoye.dandanplay.bean.params.ChangePasswordParam;
import com.xyoye.dandanplay.mvp.presenter.ChangePasswordPresenter;
import com.xyoye.dandanplay.mvp.view.ChangePasswordView;
import com.xyoye.dandanplay.net.CommJsonEntity;
import com.xyoye.dandanplay.net.CommJsonObserver;
import com.xyoye.dandanplay.net.NetworkConsumer;
import com.xyoye.dandanplay.ui.authMod.ToLoginDialog;

/**
 * Created by YE on 2018/8/11.
 */


public class ChangePasswordPresenterImpl extends BaseMvpPresenter<ChangePasswordView> implements ChangePasswordPresenter {

    public ChangePasswordPresenterImpl(ChangePasswordView view, Lifeful lifeful) {
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
    public void change(ChangePasswordParam param) {
        PersonalBean.changePassword(param, new CommJsonObserver<CommJsonEntity>() {
            @Override
            public void onSuccess(CommJsonEntity commJsonEntity) {
                ToLoginDialog dialog = new ToLoginDialog(getView().getChangeContext(), R.style.Dialog,2);
                dialog.show();
            }

            @Override
            public void onError(int errorCode, String message) {
                TLog.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }
}
