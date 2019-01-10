package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.AnimeBeans;
import com.xyoye.dandanplay.bean.SeasonAnimeBean;
import com.xyoye.dandanplay.mvp.presenter.AnimaSeasonPresenter;
import com.xyoye.dandanplay.mvp.view.AnimaSeasonView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by xyy on 2019/1/9.
 */

public class AnimaSeasonPresenterImpl extends BaseMvpPresenterImpl<AnimaSeasonView> implements AnimaSeasonPresenter {

    public AnimaSeasonPresenterImpl(AnimaSeasonView view, Lifeful lifeful) {
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
    public void getSeasonAnima(int year, int month) {
        getView().showLoading();
        SeasonAnimeBean.getSeasonAnimas(year+"", month+"", new CommJsonObserver<AnimeBeans>() {
            @Override
            public void onSuccess(AnimeBeans animeBeans) {
                getView().hideLoading();
                if (animeBeans != null){
                    if (AppConfig.getInstance().isLogin()){
                        Collections.sort(animeBeans.getBangumiList(), (o1, o2) -> {
                            // 返回值为int类型，大于0表示正序，小于0表示逆序
                            if (o1.isIsFavorited()) return -1;
                            if (o2.isIsFavorited()) return 1;
                            return 0;
                        });
                    }
                    getView().refreshAnimas(animeBeans.getBangumiList());
                }
                else
                    getView().refreshAnimas(new ArrayList<>());
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                getView().showError(message);
                LogUtils.e(message);
            }
        }, new NetworkConsumer());
    }
}
