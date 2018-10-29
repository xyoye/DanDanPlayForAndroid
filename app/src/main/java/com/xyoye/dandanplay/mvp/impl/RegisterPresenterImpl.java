package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.KeyUtil;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.RegisterBean;
import com.xyoye.dandanplay.bean.params.RegisterParam;
import com.xyoye.dandanplay.mvp.presenter.RegisterPresenter;
import com.xyoye.dandanplay.mvp.view.RegisterView;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.ui.weight.dialog.ToLoginDialog;

/**
 * Created by YE on 2018/8/5.
 */


public class RegisterPresenterImpl extends BaseMvpPresenter<RegisterView> implements RegisterPresenter {

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
        param.setScreenName(param.getUserName());
        param.setAppId(KeyUtil.getAppId(getView().getRegisterContext()));
        param.setUnixTimestamp(System.currentTimeMillis()/1000);
        param.buildHash(getView().getRegisterContext());
        RegisterBean.register(param, new CommJsonObserver<RegisterBean>(getLifeful()) {
            @Override
            public void onSuccess(RegisterBean registerBean) {
                ToLoginDialog dialog = new ToLoginDialog(getView().getRegisterContext(), R.style.Dialog, 0);
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
