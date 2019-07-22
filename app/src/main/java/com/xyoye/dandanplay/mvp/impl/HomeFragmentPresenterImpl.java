package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.AnimeBean;
import com.xyoye.dandanplay.bean.BangumiBean;
import com.xyoye.dandanplay.bean.BannerBeans;
import com.xyoye.dandanplay.bean.HomeDataBean;
import com.xyoye.dandanplay.mvp.presenter.HomeFragmentPresenter;
import com.xyoye.dandanplay.mvp.view.HomeFragmentView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Lifeful;
import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by xyoye on 2018/6/29 0029.
 */

public class HomeFragmentPresenterImpl extends BaseMvpPresenterImpl<HomeFragmentView> implements HomeFragmentPresenter {

    public HomeFragmentPresenterImpl(HomeFragmentView view, Lifeful lifeful) {
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
    public void getHomeFragmentData(){
        HomeDataBean.getHomeData(new CommOtherDataObserver<HomeDataBean>(getLifeful()) {
            @Override
            public void onSuccess(HomeDataBean homeDataBean) {
                List<String> images = new ArrayList<>();
                List<String> titles = new ArrayList<>();
                List<String> urls = new ArrayList<>();
                for (BannerBeans.BannersBean banner : homeDataBean.getBanners() ){
                    images.add(banner.getImageUrl());
                    titles.add(banner.getTitle());
                    urls.add(banner.getUrl());
                }

                List<BangumiBean> bangumiBeanList = setBangumiList(homeDataBean.getBangumiList());
                getView().refreshUI(images, titles, urls, bangumiBeanList);

            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                ToastUtils.showShort("获取首页数据失败，请重试");
            }
        }, new NetworkConsumer());
    }

    private List<BangumiBean> setBangumiList(List<AnimeBean> bangumiList){
        List<BangumiBean> beansList = new ArrayList<>();
        BangumiBean bangumiBean00 = new BangumiBean();
        BangumiBean bangumiBean01 = new BangumiBean();
        BangumiBean bangumiBean02 = new BangumiBean();
        BangumiBean bangumiBean03 = new BangumiBean();
        BangumiBean bangumiBean04 = new BangumiBean();
        BangumiBean bangumiBean05 = new BangumiBean();
        BangumiBean bangumiBean06 = new BangumiBean();
        bangumiBean00.setBangumiList(new ArrayList<>());
        bangumiBean01.setBangumiList(new ArrayList<>());
        bangumiBean02.setBangumiList(new ArrayList<>());
        bangumiBean03.setBangumiList(new ArrayList<>());
        bangumiBean04.setBangumiList(new ArrayList<>());
        bangumiBean05.setBangumiList(new ArrayList<>());
        bangumiBean06.setBangumiList(new ArrayList<>());
        beansList.add(bangumiBean00);
        beansList.add(bangumiBean01);
        beansList.add(bangumiBean02);
        beansList.add(bangumiBean03);
        beansList.add(bangumiBean04);
        beansList.add(bangumiBean05);
        beansList.add(bangumiBean06);

        //按关注排序
        if (AppConfig.getInstance().isLogin()){
            Collections.sort(bangumiList, (o1, o2) -> {
                // 返回值为int类型，大于0表示正序，小于0表示逆序
                if (o1.isIsFavorited()) return -1;
                if (o2.isIsFavorited()) return 1;
                return 0;
            });
        }

        //按日期分类
        for (AnimeBean bean : bangumiList){
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
        return beansList;
    }
}
