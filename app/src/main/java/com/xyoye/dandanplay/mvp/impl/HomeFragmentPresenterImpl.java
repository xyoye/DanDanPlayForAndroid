package com.xyoye.dandanplay.mvp.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.core.base.BaseMvpPresenter;
import com.xyoye.core.db.DataBaseManager;
import com.xyoye.core.rx.Lifeful;
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
    public HomeFragmentPresenterImpl(HomeFragmentView view, Lifeful lifeful) {
        super(view, lifeful);
    }

    @Override
    public void init() {

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
}
