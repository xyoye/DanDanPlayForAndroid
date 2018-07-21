package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.bean.AnimaDetailBean;
import com.xyoye.dandanplay.mvp.presenter.AnimaDetailPresenter;
import com.xyoye.dandanplay.mvp.view.AnimaDetailView;
import com.xyoye.dandanplay.net.CommJsonObserver;
import com.xyoye.dandanplay.net.NetworkConsumer;

/**
 * Created by YE on 2018/7/20.
 */


public class AnimaDetailPresenterImpl extends BaseMvpPresenter<AnimaDetailView> implements AnimaDetailPresenter {

    public AnimaDetailPresenterImpl(AnimaDetailView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {

    }

    @Override
    public void process(Bundle savedInstanceState) {
        getAnimaDetail(getView().getAnimaId());
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

    private void getAnimaDetail(String animaId){
        AnimaDetailBean.getAnimaDetail(animaId, new CommJsonObserver<AnimaDetailBean>() {
            @Override
            public void onSuccess(AnimaDetailBean animaDetailBean) {
                 getView().showAnimaDetail(animaDetailBean);
            }

            @Override
            public void onError(int errorCode, String message) {
                TLog.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }
}
