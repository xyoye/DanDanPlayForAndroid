package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.bean.PlayHistoryBean;

/**
 * Created by YE on 2018/6/29 0029.
 */


public interface PersonalFragmentPresenter extends BasePresenter {
    AnimeFavoriteBean getFavoriteBean();

    PlayHistoryBean getPlayHistoryBean();
}
