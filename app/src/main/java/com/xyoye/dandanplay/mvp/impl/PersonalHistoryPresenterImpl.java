package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.mvp.presenter.PersonalHistoryPresenter;
import com.xyoye.dandanplay.mvp.view.PersonalHistoryView;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YE on 2018/7/24.
 */


public class PersonalHistoryPresenterImpl extends BaseMvpPresenter<PersonalHistoryView> implements PersonalHistoryPresenter{

    public PersonalHistoryPresenterImpl(PersonalHistoryView view, Lifeful lifeful) {
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
    public void getPlayHistory(){
        getView().showLoading();
        PlayHistoryBean.getPlayHistory(new CommJsonObserver<PlayHistoryBean>(getLifeful()) {
            @Override
            public void onSuccess(PlayHistoryBean playHistoryBean) {
                getView().refreshHistory(playHistoryBean);
                getView().hideLoading();
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().refreshHistory(null);
                getView().hideLoading();
                TLog.e(message);
            }
        }, new NetworkConsumer());
    }
}
