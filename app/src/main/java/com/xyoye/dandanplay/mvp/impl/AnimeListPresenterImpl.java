package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.LogUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.bean.AnimeTagBean;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.mvp.presenter.AnimeListPresenter;
import com.xyoye.dandanplay.mvp.view.AnimeListView;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

/**
 * Created by xyoye on 2018/7/24.
 */

public class AnimeListPresenterImpl extends BaseMvpPresenterImpl<AnimeListView> implements AnimeListPresenter {

    public AnimeListPresenterImpl(AnimeListView view, LifecycleOwner lifecycleOwner) {
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
    public void getPlayHistory(){
        getView().showLoading();
        PlayHistoryBean.getPlayHistory(new CommJsonObserver<PlayHistoryBean>(getLifecycle()) {
            @Override
            public void onSuccess(PlayHistoryBean playHistoryBean) {
                getView().refreshHistory(playHistoryBean);
                getView().hideLoading();
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().refreshHistory(null);
                getView().hideLoading();
                LogUtils.e(message);
            }
        }, new NetworkConsumer());
    }

    @Override
    public void getFavorite(){
        getView().showLoading();
        AnimeFavoriteBean.getFavorite(new CommJsonObserver<AnimeFavoriteBean>(getLifecycle()) {
            @Override
            public void onSuccess(AnimeFavoriteBean animeFavoriteBean) {
                getView().hideLoading();
                getView().refreshFavorite(animeFavoriteBean);
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                getView().refreshFavorite(null);
                LogUtils.e(message);
            }
        }, new NetworkConsumer());
    }

    @Override
    public void getByTag(int tagId) {
        getView().showLoading();
        AnimeTagBean.getTagAnimeList(tagId+"", new CommJsonObserver<AnimeTagBean>(getLifecycle()) {
            @Override
            public void onSuccess(AnimeTagBean animeTagBean) {
                getView().hideLoading();
                getView().refreshTagAnime(animeTagBean);
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                getView().refreshTagAnime(null);
                LogUtils.e(message);
            }
        }, new NetworkConsumer());
    }
}
