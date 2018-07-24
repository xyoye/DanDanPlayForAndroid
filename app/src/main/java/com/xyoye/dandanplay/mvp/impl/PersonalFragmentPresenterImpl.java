package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.KeyUtil;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.bean.AnimaFavoriteBean;
import com.xyoye.dandanplay.bean.PersonalBean;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.bean.params.LoginParam;
import com.xyoye.dandanplay.mvp.view.PersonalFragmentView;
import com.xyoye.dandanplay.mvp.presenter.PersonalFragmentPresenter;
import com.xyoye.dandanplay.net.CommJsonObserver;
import com.xyoye.dandanplay.net.NetworkConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class PersonalFragmentPresenterImpl extends BaseMvpPresenter<PersonalFragmentView> implements PersonalFragmentPresenter {

    private AnimaFavoriteBean favoriteBean;
    private PlayHistoryBean historyBean;

    public PersonalFragmentPresenterImpl(PersonalFragmentView view, Lifeful lifeful) {
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
        getView().changeView();
        getFavorite();
        getPlayHistory();
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    private void getFavorite(){
        AnimaFavoriteBean.getFavorite(new CommJsonObserver<AnimaFavoriteBean>() {
            @Override
            public void onSuccess(AnimaFavoriteBean animaFavoriteBean) {
                favoriteBean = animaFavoriteBean;
                if (animaFavoriteBean.getFavorites().size() > 3){
                    List<AnimaFavoriteBean.FavoritesBean> beans = new ArrayList<>();
                    for (int i=0; i<3; i++){
                        beans.add(animaFavoriteBean.getFavorites().get(i));
                    }
                    AnimaFavoriteBean animaFavoriteBeanTemp = new AnimaFavoriteBean();
                    animaFavoriteBean.setFavorites(beans);
                    getView().refreshFavorite(animaFavoriteBeanTemp);
                }else
                    getView().refreshFavorite(animaFavoriteBean);
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().refreshFavorite(null);
                TLog.e(message);
            }
        }, new NetworkConsumer());
    }

    private void getPlayHistory(){
        PlayHistoryBean.getPlayHistory(new CommJsonObserver<PlayHistoryBean>() {
            @Override
            public void onSuccess(PlayHistoryBean playHistoryBean) {
                historyBean = playHistoryBean;
                if (playHistoryBean.getPlayHistoryAnimes().size() > 3){
                    List<PlayHistoryBean.PlayHistoryAnimesBean> beans = new ArrayList<>();
                    for (int i = 0; i < 3; i++) {
                        beans.add(playHistoryBean.getPlayHistoryAnimes().get(i));
                    }
                    PlayHistoryBean playHistoryBeanTemp = new PlayHistoryBean();
                    playHistoryBean.setPlayHistoryAnimes(beans);
                    getView().refreshHistory(playHistoryBeanTemp);
                }else
                    getView().refreshHistory(playHistoryBean);
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().refreshHistory(null);
                TLog.e(message);
            }
        }, new NetworkConsumer());
    }

    @Override
    public AnimaFavoriteBean getFavoriteBean() {
        return favoriteBean;
    }

    @Override
    public PlayHistoryBean getPlayHistoryBean() {
        return historyBean;
    }
}
