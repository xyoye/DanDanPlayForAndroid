package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.mvp.presenter.PersonalFavoritePresenter;
import com.xyoye.dandanplay.mvp.view.PeronalFavoriteView;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

/**
 * Created by xyoye on 2018/7/24.
 */

public class PersonalPresenterImpl extends BaseMvpPresenterImpl<PeronalFavoriteView> implements PersonalFavoritePresenter {

    public PersonalPresenterImpl(PeronalFavoriteView view, Lifeful lifeful) {
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
    public void getFavorite(){
        getView().showLoading();
        AnimeFavoriteBean.getFavorite(new CommJsonObserver<AnimeFavoriteBean>(getLifeful()) {
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
}
