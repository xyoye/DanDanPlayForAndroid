package com.xyoye.dandanplay.mvp.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.AnimeBeans;
import com.xyoye.dandanplay.bean.BannerBeans;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.mvp.presenter.HomeFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.HomeFragmentView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.util.ArrayList;
import java.util.Collections;
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


public class HomeFragmentPresenterImpl extends BaseMvpPresenterImpl<HomeFragmentView> implements HomeFragmentPresenter {

    private CountDownLatch countDownLatch = null;
    private List<BannerBeans.BannersBean> bannerList;
    private List<AnimeBeans> animeBeansList;

    public HomeFragmentPresenterImpl(HomeFragmentView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {
        bannerList = new ArrayList<>();
        animeBeansList = new ArrayList<>();
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
    public void getBannerList(){
        BannerBeans.getBanner(new CommJsonObserver<BannerBeans>(getLifeful()) {
            @Override
            public void onSuccess(BannerBeans bannerBean) {
                List<BannerBeans.BannersBean> beans = bannerBean.getBanners();
                new Thread(() -> {
                    SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
                    sqLiteDatabase.delete(DataBaseInfo.getTableNames()[3],"", new String[]{});
                    for(BannerBeans.BannersBean bean : beans ){
                        ContentValues values=new ContentValues();
                        values.put(DataBaseInfo.getFieldNames()[3][1], bean.getTitle());
                        values.put(DataBaseInfo.getFieldNames()[3][2], bean.getDescription());
                        values.put(DataBaseInfo.getFieldNames()[3][3], bean.getUrl());
                        values.put(DataBaseInfo.getFieldNames()[3][4], bean.getImageUrl());
                        sqLiteDatabase.insert(DataBaseInfo.getTableNames()[3],null,values);
                    }
                }).start();
                if (countDownLatch != null){
                    countDownLatch.countDown();
                    HomeFragmentPresenterImpl.this.bannerList = beans;
                }else {
                    setBanners(beans);
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                LogUtils.e(message);
                ToastUtils.showShort(message);

                List<BannerBeans.BannersBean> bannerBeans = new ArrayList<>();
                SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
                String sql = "SELECT * FROM banner";
                Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{});
                while (cursor.moveToNext()){
                    String title = cursor.getString(1);
                    String description = cursor.getString(2);
                    String url = cursor.getString(3);
                    String imageUrl = cursor.getString(4);
                    bannerBeans.add(new BannerBeans.BannersBean(title,description,url,imageUrl));
                }
                cursor.close();

                if (countDownLatch != null){
                    countDownLatch.countDown();
                    HomeFragmentPresenterImpl.this.bannerList = bannerBeans;
                }else {
                    setBanners(bannerBeans);
                }
            }
        }, new NetworkConsumer());
    }

    @Override
    public void getAnimaList(){
        AnimeBeans.getAnimes(new CommJsonObserver<AnimeBeans>(getLifeful()) {
            @Override
            public void onSuccess(AnimeBeans animeBeans) {
                List<AnimeBeans> beansList = new ArrayList<>();
                initList(beansList);
                if (AppConfig.getInstance().isLogin()){
                    Collections.sort(animeBeans.getBangumiList(), (o1, o2) -> {
                        // 返回值为int类型，大于0表示正序，小于0表示逆序
                        if (o1.isIsFavorited()) return -1;
                        if (o2.isIsFavorited()) return 1;
                        return 0;
                    });
                }
                for (AnimeBeans.BangumiListBean bean : animeBeans.getBangumiList()){
                    switch (bean.getAirDay()){
                        case 0:
                            beansList.get(0).getBangumiList().add(bean);
                            break;
                        case 1:
                            beansList.get(1).getBangumiList().add(bean);
                            break;
                        case 2:
                            beansList.get(2).getBangumiList().add(bean);
                            break;
                        case 3:
                            beansList.get(3).getBangumiList().add(bean);
                            break;
                        case 4:
                            beansList.get(4).getBangumiList().add(bean);
                            break;
                        case 5:
                            beansList.get(5).getBangumiList().add(bean);
                            break;
                        case 6:
                            beansList.get(6).getBangumiList().add(bean);
                            break;
                    }
                }
                if (countDownLatch != null){
                    countDownLatch.countDown();
                    HomeFragmentPresenterImpl.this.animeBeansList = beansList;
                }else {
                    getView().initViewPager(beansList);
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                ToastUtils.showShort(message);
                LogUtils.e(message);

                if (countDownLatch != null){
                    countDownLatch.countDown();
                }else {
                    getView().initViewPager(new ArrayList<>());
                }
            }
        }, new NetworkConsumer());
    }

    @Override
    public void getHomeFragmentData(){
        countDownLatch = new CountDownLatch(2);
        getBannerList();
        getAnimaList();

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
                        List<String> images = new ArrayList<>();
                        List<String> titles = new ArrayList<>();
                        List<String> urls = new ArrayList<>();
                        for (BannerBeans.BannersBean banner : bannerList ){
                            images.add(banner.getImageUrl());
                            titles.add(banner.getTitle());
                            urls.add(banner.getUrl());
                        }

                        getView().refreshUI(images, titles, urls, animeBeansList);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void initList(List<AnimeBeans> beansList){
        AnimeBeans animeBeans00 = new AnimeBeans();
        AnimeBeans animeBeans01 = new AnimeBeans();
        AnimeBeans animeBeans02 = new AnimeBeans();
        AnimeBeans animeBeans03 = new AnimeBeans();
        AnimeBeans animeBeans04 = new AnimeBeans();
        AnimeBeans animeBeans05 = new AnimeBeans();
        AnimeBeans animeBeans06 = new AnimeBeans();
        animeBeans00.setBangumiList(new ArrayList<>());
        animeBeans01.setBangumiList(new ArrayList<>());
        animeBeans02.setBangumiList(new ArrayList<>());
        animeBeans03.setBangumiList(new ArrayList<>());
        animeBeans04.setBangumiList(new ArrayList<>());
        animeBeans05.setBangumiList(new ArrayList<>());
        animeBeans06.setBangumiList(new ArrayList<>());
        beansList.add(animeBeans00);
        beansList.add(animeBeans01);
        beansList.add(animeBeans02);
        beansList.add(animeBeans03);
        beansList.add(animeBeans04);
        beansList.add(animeBeans05);
        beansList.add(animeBeans06);
    }

    private void setBanners(List<BannerBeans.BannersBean> bannerBeans){
        List<String> images = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        for (BannerBeans.BannersBean banner : bannerBeans ){
            images.add(banner.getImageUrl());
            titles.add(banner.getTitle());
            urls.add(banner.getUrl());
        }

        getView().setBanners(images, titles, urls);
    }
}
