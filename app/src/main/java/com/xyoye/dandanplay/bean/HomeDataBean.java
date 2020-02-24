package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;
import com.xyoye.dandanplay.utils.net.service.RetrofitService;

import java.io.Serializable;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2019/7/9.
 */

public class HomeDataBean implements Serializable {
    private List<AnimeBean> bangumiList;
    private List<BannerBeans.BannersBean> banners;

    public HomeDataBean(List<AnimeBean> bangumiList, List<BannerBeans.BannersBean> banners) {
        this.bangumiList = bangumiList;
        this.banners = banners;
    }

    public List<AnimeBean> getBangumiList() {
        return bangumiList;
    }

    public List<BannerBeans.BannersBean> getBanners() {
        return banners;
    }

    public static void getHomeData(CommOtherDataObserver<HomeDataBean> observer, NetworkConsumer consumer){
        RetrofitService service = RetroFactory.getInstance();
        service.getAnimes()
                .zipWith(service.getBanner(), (bangumiBean, bannerBeans) ->
                                new HomeDataBean(bangumiBean.getBangumiList(), bannerBeans.getBanners()))
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
