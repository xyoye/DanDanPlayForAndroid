package com.xyoye.dandanplay.mvp.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.db.DataBaseManager;
import com.xyoye.core.rx.Lifeful;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.bean.AnimaBeans;
import com.xyoye.dandanplay.bean.BannerBeans;
import com.xyoye.dandanplay.bean.DanmuMatchBean;
import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.bean.params.DanmuMatchParam;
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
    List<String> dateList;

    public HomeFragmentPresenterImpl(HomeFragmentView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {
        dateList = new ArrayList<>();
        dateList.add("日");
        dateList.add("一");
        dateList.add("二");
        dateList.add("三");
        dateList.add("四");
        dateList.add("五");
        dateList.add("六");
        getAnimaList();
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
        AnimaBeans.getAnimas(new CommJsonObserver<AnimaBeans>() {
            @Override
            public void onSuccess(AnimaBeans animaBeans) {
                List<AnimaBeans> beansList = new ArrayList<>();
                initList(beansList);
                for (AnimaBeans.BangumiListBean bean : animaBeans.getBangumiList()){
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
                getView().initViewPager(beansList, dateList);
            }

            @Override
            public void onError(int errorCode, String message) {
                ToastUtils.showShort(message);
                TLog.e(message);
            }
        }, new NetworkConsumer());
    }

    private void initList(List<AnimaBeans> beansList){
        AnimaBeans animaBeans00 = new AnimaBeans();
        AnimaBeans animaBeans01 = new AnimaBeans();
        AnimaBeans animaBeans02 = new AnimaBeans();
        AnimaBeans animaBeans03 = new AnimaBeans();
        AnimaBeans animaBeans04 = new AnimaBeans();
        AnimaBeans animaBeans05 = new AnimaBeans();
        AnimaBeans animaBeans06 = new AnimaBeans();
        animaBeans00.setBangumiList(new ArrayList<>());
        animaBeans01.setBangumiList(new ArrayList<>());
        animaBeans02.setBangumiList(new ArrayList<>());
        animaBeans03.setBangumiList(new ArrayList<>());
        animaBeans04.setBangumiList(new ArrayList<>());
        animaBeans05.setBangumiList(new ArrayList<>());
        animaBeans06.setBangumiList(new ArrayList<>());
        beansList.add(animaBeans00);
        beansList.add(animaBeans01);
        beansList.add(animaBeans02);
        beansList.add(animaBeans03);
        beansList.add(animaBeans04);
        beansList.add(animaBeans05);
        beansList.add(animaBeans06);
    }
}
