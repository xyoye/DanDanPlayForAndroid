package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;

import java.io.Serializable;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2018/7/15.
 */


public class BangumiBean extends CommJsonEntity implements Serializable {

    private List<AnimeBean> bangumiList;

    public List<AnimeBean> getBangumiList() {
        return bangumiList;
    }

    public void setBangumiList(List<AnimeBean> bangumiList) {
        this.bangumiList = bangumiList;
    }

    public static void getAnimes(CommJsonObserver<BangumiBean> observer, NetworkConsumer consumer){
        RetroFactory.getInstance().getAnimes()
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
