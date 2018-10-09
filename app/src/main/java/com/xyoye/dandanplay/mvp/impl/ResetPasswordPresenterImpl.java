package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.KeyUtil;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.PersonalBean;
import com.xyoye.dandanplay.bean.params.ResetPasswordParam;
import com.xyoye.dandanplay.mvp.presenter.ResetPasswordPresenter;
import com.xyoye.dandanplay.mvp.view.ResetPasswordView;
import com.xyoye.dandanplay.net.CommJsonEntity;
import com.xyoye.dandanplay.net.CommJsonObserver;
import com.xyoye.dandanplay.net.NetworkConsumer;
import com.xyoye.dandanplay.ui.authMod.ToLoginDialog;

/**
 * Created by YE on 2018/8/11.
 */


public class ResetPasswordPresenterImpl extends BaseMvpPresenter<ResetPasswordView> implements ResetPasswordPresenter {

    public ResetPasswordPresenterImpl(ResetPasswordView view, Lifeful lifeful) {
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
    public void reset(ResetPasswordParam param) {
        param.setAppId(KeyUtil.getAppId(getView().getResetContext()));
        param.setUnixTimestamp(System.currentTimeMillis()/1000);
        param.buildHash(getView().getResetContext());
        PersonalBean.resetPassword(param, new CommJsonObserver<CommJsonEntity>(getLifeful()) {
            @Override
            public void onSuccess(CommJsonEntity commJsonEntity) {
                ToLoginDialog dialog = new ToLoginDialog(getView().getResetContext(), R.style.Dialog,1);
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
