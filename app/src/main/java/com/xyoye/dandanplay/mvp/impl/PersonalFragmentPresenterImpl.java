package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.mvp.view.PersonalFragmentView;
import com.xyoye.dandanplay.mvp.presenter.PersonalFragmentPresenter;
import com.xyoye.dandanplay.net.CommJsonObserver;
import com.xyoye.dandanplay.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.UserInfoShare;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class PersonalFragmentPresenterImpl extends BaseMvpPresenter<PersonalFragmentView> implements PersonalFragmentPresenter {

    private AnimeFavoriteBean favoriteBean;
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
        if (UserInfoShare.getInstance().isLogin()){
            getView().changeView();
            getFavorite();
            getPlayHistory();
        }else {
            getView().refreshFavorite(null);
            getView().refreshHistory(null);
            getView().changeView();
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    private void getFavorite(){
        AnimeFavoriteBean.getFavorite(new CommJsonObserver<AnimeFavoriteBean>() {
            @Override
            public void onSuccess(AnimeFavoriteBean animeFavoriteBean) {
                favoriteBean = animeFavoriteBean;
                if (animeFavoriteBean.getFavorites().size() > 3){
                    List<AnimeFavoriteBean.FavoritesBean> beans = new ArrayList<>();
                    for (int i=0; i<3; i++){
                        beans.add(animeFavoriteBean.getFavorites().get(i));
                    }
                    AnimeFavoriteBean animeFavoriteBeanTemp = new AnimeFavoriteBean();
                    animeFavoriteBean.setFavorites(beans);
                    getView().refreshFavorite(animeFavoriteBeanTemp);
                }else
                    getView().refreshFavorite(animeFavoriteBean);
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
    public AnimeFavoriteBean getFavoriteBean() {
        return favoriteBean;
    }

    @Override
    public PlayHistoryBean getPlayHistoryBean() {
        return historyBean;
    }
}
