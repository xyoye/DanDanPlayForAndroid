package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;

/**
 * Created by YE on 2018/6/29 0029.
 */


public interface HomeFragmentPresenter extends BasePresenter {
    void getBannerList();
    void getAnimaList();

    void getHomeFragmentData();
}
