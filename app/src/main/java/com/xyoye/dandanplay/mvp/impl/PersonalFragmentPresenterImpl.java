package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.mvp.presenter.PersonalFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.PersonalFragmentView;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class PersonalFragmentPresenterImpl extends BaseMvpPresenterImpl<PersonalFragmentView> implements PersonalFragmentPresenter {
    private CountDownLatch countDownLatch = null;
    private AnimeFavoriteBean animeFavoriteBean;
    private PlayHistoryBean playHistoryBean;

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

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    private void getFavoriteData(){
        AnimeFavoriteBean.getFavorite(new CommJsonObserver<AnimeFavoriteBean>(getLifeful()) {
            @Override
            public void onSuccess(AnimeFavoriteBean animeFavoriteBean) {
                if (animeFavoriteBean.getFavorites().size() > 3){
                    List<AnimeFavoriteBean.FavoritesBean> beans = new ArrayList<>();
                    for (int i = 0; i < 3; i++) {
                        beans.add(animeFavoriteBean.getFavorites().get(i));
                    }
                    AnimeFavoriteBean animeFavoriteBeanTemp = new AnimeFavoriteBean();
                    animeFavoriteBeanTemp.setFavorites(beans);
                    PersonalFragmentPresenterImpl.this.animeFavoriteBean = animeFavoriteBeanTemp;
                }else
                    PersonalFragmentPresenterImpl.this.animeFavoriteBean = animeFavoriteBean;

                if (countDownLatch == null){
                    getView().refreshFavorite(PersonalFragmentPresenterImpl.this.animeFavoriteBean);
                }else {
                    countDownLatch.countDown();
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                PersonalFragmentPresenterImpl.this.animeFavoriteBean = null;
                LogUtils.e(message);

                if (countDownLatch == null){
                    getView().refreshFavorite(PersonalFragmentPresenterImpl.this.animeFavoriteBean);
                }else {
                    countDownLatch.countDown();
                }
            }
        }, new NetworkConsumer());
    }

    private void getHistoryData(){
        PlayHistoryBean.getPlayHistory(new CommJsonObserver<PlayHistoryBean>(getLifeful()) {
            @Override
            public void onSuccess(PlayHistoryBean playHistoryBean) {
                if (playHistoryBean.getPlayHistoryAnimes().size() > 3){
                    List<PlayHistoryBean.PlayHistoryAnimesBean> beans = new ArrayList<>();
                    for (int i = 0; i < 3; i++) {
                        beans.add(playHistoryBean.getPlayHistoryAnimes().get(i));
                    }
                    PlayHistoryBean playHistoryBeanTemp = new PlayHistoryBean();
                    playHistoryBeanTemp.setPlayHistoryAnimes(beans);
                    PersonalFragmentPresenterImpl.this.playHistoryBean = playHistoryBeanTemp;
                }else
                    PersonalFragmentPresenterImpl.this.playHistoryBean = playHistoryBean;

                if (countDownLatch == null){
                    getView().refreshHistory(PersonalFragmentPresenterImpl.this.playHistoryBean);
                }else {
                    countDownLatch.countDown();
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                PersonalFragmentPresenterImpl.this.playHistoryBean = null;
                LogUtils.e(message);

                if (countDownLatch == null){
                    getView().refreshHistory(PersonalFragmentPresenterImpl.this.playHistoryBean);
                }else {
                    countDownLatch.countDown();
                }
            }
        }, new NetworkConsumer());
    }

    @Override
    public void getFragmentData() {
        countDownLatch = new CountDownLatch(2);
        getFavoriteData();
        getHistoryData();

        io.reactivex.Observable.create((ObservableOnSubscribe<Boolean>) e -> {
            try {
                countDownLatch.await();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            e.onNext(true);
            e.onComplete();
        }).subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Observer<Boolean>() {
              @Override
              public void onSubscribe(Disposable d) {

              }

              @Override
              public void onNext(Boolean b) {
                  getView().refreshUI(animeFavoriteBean, playHistoryBean);
              }

              @Override
              public void onError(Throwable e) {

              }

              @Override
              public void onComplete() {

              }
          });
    }

}
