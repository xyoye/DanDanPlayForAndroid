package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.bean.AnimeDetailBean;
import com.xyoye.dandanplay.mvp.presenter.AnimeDetailPresenter;
import com.xyoye.dandanplay.mvp.view.AnimeDetailView;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

/**
 * Created by YE on 2018/7/20.
 */


public class AnimeDetailPresenterImpl extends BaseMvpPresenter<AnimeDetailView> implements AnimeDetailPresenter {

    public AnimeDetailPresenterImpl(AnimeDetailView view, Lifeful lifeful) {
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
        getView().showLoading();
        AnimeDetailBean.getAnimaDetail(animaId, new CommJsonObserver<AnimeDetailBean>(getLifeful()) {
            @Override
            public void onSuccess(AnimeDetailBean animeDetailBean) {
                getView().hideLoading();
                getView().showAnimeDetail(animeDetailBean);
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                TLog.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }
}
