package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.PersonalBean;
import com.xyoye.dandanplay.bean.params.ResetPasswordParam;
import com.xyoye.dandanplay.mvp.presenter.ResetPasswordPresenter;
import com.xyoye.dandanplay.mvp.view.ResetPasswordView;
import com.xyoye.dandanplay.ui.weight.dialog.ToLoginDialog;
import com.xyoye.dandanplay.utils.KeyUtil;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

/**
 * Created by YE on 2018/8/11.
 */


public class ResetPasswordPresenterImpl extends BaseMvpPresenterImpl<ResetPasswordView> implements ResetPasswordPresenter {

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
                LogUtils.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }
}
