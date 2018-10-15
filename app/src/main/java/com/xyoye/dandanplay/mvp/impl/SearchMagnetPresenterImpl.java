package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.bean.MagnetBean;
import com.xyoye.dandanplay.mvp.presenter.SearchMagnetPresenter;
import com.xyoye.dandanplay.mvp.view.SearchMagnetView;
import com.xyoye.dandanplay.net.CommOtherDataObserver;
import com.xyoye.dandanplay.net.NetworkConsumer;

/**
 * Created by YE on 2018/10/13.
 */


public class SearchMagnetPresenterImpl extends BaseMvpPresenter<SearchMagnetView> implements SearchMagnetPresenter {

    public SearchMagnetPresenterImpl(SearchMagnetView view, Lifeful lifeful) {
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
    public void searchManget(String anime, int typeId, int subGroundId) {
        MagnetBean.searchManget(anime, typeId, subGroundId, new CommOtherDataObserver<MagnetBean>(getLifeful()) {
            @Override
            public void onSuccess(MagnetBean magnetBean) {

            }

            @Override
            public void onError(int errorCode, String message) {
                TLog.e(message);
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }
}
