package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;

/**
 * Created by YE on 2018/10/13.
 */


public interface SearchMagnetPresenter extends BasePresenter{
    void searchManget(String anime, int typeId, int subGroundId);
}
