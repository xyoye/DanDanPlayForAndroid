package com.xyoye.dandanplay.mvp.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.db.DataBaseManager;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.bean.AnimeBeans;
import com.xyoye.dandanplay.bean.BannerBeans;
import com.xyoye.dandanplay.mvp.view.HomeFragmentView;
import com.xyoye.dandanplay.mvp.presenter.HomeFragmentPresenter;
import com.xyoye.dandanplay.net.CommJsonObserver;
import com.xyoye.dandanplay.net.NetworkConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YE on 2018/6/29 0029.
 */


public class HomeFragmentPresenterImpl extends BaseMvpPresenter<HomeFragmentView> implements HomeFragmentPresenter {
    private List<String> dateList;

    public HomeFragmentPresenterImpl(HomeFragmentView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {
        dateList = new ArrayList<>();
        dateList.add("周日");
        dateList.add("周一");
        dateList.add("周二");
        dateList.add("周三");
        dateList.add("周四");
        dateList.add("周五");
        dateList.add("周六");
    }

    @Override
    public void process(Bundle savedInstanceState) {
        List<BannerBeans.BannersBean> banners = getBannerList();
        List<String> images = new ArrayList<>();
        List<String> titles = new ArrayList<>();
        List<String> urls = new ArrayList<>();
        for (BannerBeans.BannersBean banner : banners ){
            images.add(banner.getImageUrl());
            titles.add(banner.getTitle());
            urls.add(banner.getUrl());
        }
        getView().setBanners(images, titles, urls);
        getView().initIndicator(dateList);
        getAnimaList();
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

    private List<BannerBeans.BannersBean> getBannerList(){
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
        return bannerBeans;
    }

    private void getAnimaList(){
        AnimeBeans.getAnimes(new CommJsonObserver<AnimeBeans>(getLifeful()) {
            @Override
            public void onSuccess(AnimeBeans animeBeans) {
                List<AnimeBeans> beansList = new ArrayList<>();
                initList(beansList);
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
                IApplication.getExecutor().execute(() ->
                        getView().initViewPager(beansList));
            }

            @Override
            public void onError(int errorCode, String message) {
                ToastUtils.showShort(message);
                TLog.e(message);
            }
        }, new NetworkConsumer());
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
}
