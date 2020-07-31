package com.xyoye.dandanplay.mvp.impl;

import android.os.Bundle;

import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.LogUtils;
import com.xyoye.dandanplay.base.BaseMvpPresenterImpl;
import com.xyoye.dandanplay.bean.AnimeBean;
import com.xyoye.dandanplay.bean.BangumiBean;
import com.xyoye.dandanplay.bean.SeasonAnimeBean;
import com.xyoye.dandanplay.mvp.presenter.AnimeSeasonPresenter;
import com.xyoye.dandanplay.mvp.view.AnimeSeasonView;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.xyoye.dandanplay.ui.activities.anime.AnimeSeasonActivity.SORT_FOLLOW;
import static com.xyoye.dandanplay.ui.activities.anime.AnimeSeasonActivity.SORT_NAME;

/**
 * Created by xyoye on 2019/1/9.
 */

public class AnimeSeasonPresenterImpl extends BaseMvpPresenterImpl<AnimeSeasonView> implements AnimeSeasonPresenter {

    public AnimeSeasonPresenterImpl(AnimeSeasonView view, LifecycleOwner lifecycleOwner) {
        super(view, lifecycleOwner);
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
    public void getSeasonAnime(int year, int month) {
        getView().showLoading();
        SeasonAnimeBean.getSeasonAnimas(year+"", month+"", new CommJsonObserver<BangumiBean>(getLifecycle()) {
            @Override
            public void onSuccess(BangumiBean bangumiBean) {
                getView().hideLoading();
                if (bangumiBean != null){
                    int sortType = AppConfig.getInstance().getSeasonSortType();
                    if (!AppConfig.getInstance().isLogin() && sortType == 0){
                        sortType = SORT_NAME;
                    }
                    sortAnime(bangumiBean.getBangumiList(), sortType);
                    getView().refreshAnime(bangumiBean.getBangumiList());
                }
                else
                    getView().refreshAnime(new ArrayList<>());
            }

            @Override
            public void onError(int errorCode, String message) {
                getView().hideLoading();
                getView().showError(message);
                LogUtils.e(message);
            }
        }, new NetworkConsumer());
    }

    @Override
    public void sortAnime(List<AnimeBean> animaList, int sortType){
        if (sortType == SORT_FOLLOW){
            Collections.sort(animaList, (o1, o2) ->
                    Boolean.compare(o2.isIsFavorited(), o1.isIsFavorited()));
        }else if (sortType == SORT_NAME){
            Collections.sort(animaList, (o1, o2) ->
                    Collator.getInstance(Locale.CHINESE).compare( o1.getAnimeTitle(), o2.getAnimeTitle()));
        }else {
            Collections.sort(animaList, (o1, o2) ->
                    Double.compare(o2.getRating(), o1.getRating()));
        }
    }
}
