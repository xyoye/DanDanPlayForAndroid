package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by YE on 2018/7/20.
 */


public interface AnimeDetailPresenter extends BaseMvpPresenter {

    void getAnimeDetail(String animeId);

    void followConfirm(String animeId);

    void followCancel(String animeId);
}
