package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.AnimeDetailBean;
import com.xyoye.dandanplay.mvp.presenter.AnimeDetailPresenter;
import com.xyoye.dandanplay.mvp.view.AnimeDetailView;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

/**
 * Created by xyoye on 2018/7/20.
 */

public class AnimeDetailPresenterImpl extends BaseMvpPresenterImpl<AnimeDetailView> implements AnimeDetailPresenter {

    public AnimeDetailPresenterImpl(AnimeDetailView view, Lifeful lifeful) {
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
    public void getAnimeDetail(String animeId) {
        getView().showLoading();
        AnimeDetailBean.getAnimaDetail(animeId, new CommJsonObserver<AnimeDetailBean>(getLifeful()) {
            @Override
            public void onSuccess(AnimeDetailBean animeDetailBean) {
                getView().hideLoading();
                getView().showAnimeDetail(animeDetailBean);
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
    public void followConfirm(String animeId) {
        AnimeDetailBean.addFavorite(animeId, new CommJsonObserver<CommJsonEntity>(getLifeful()) {
            @Override
            public void onSuccess(CommJsonEntity commJsonEntity) {
                getView().afterFollow(true);
            }

            @Override
            public void onError(int errorCode, String message) {
                ToastUtils.showShort(message);
                LogUtils.e(message);
            }
        }, new NetworkConsumer());
    }

    @Override
    public void followCancel(String animeId) {
        AnimeDetailBean.reduceFavorite(animeId, new CommJsonObserver<CommJsonEntity>(getLifeful()) {
            @Override
            public void onSuccess(CommJsonEntity commJsonEntity) {
                getView().afterFollow(false);
            }

            @Override
            public void onError(int errorCode, String message) {
                ToastUtils.showShort(message);
                LogUtils.e(message);
            }
        }, new NetworkConsumer());
    }
}
