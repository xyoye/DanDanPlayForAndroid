package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2018/7/24.
 */

public interface AnimeListPresenter extends BaseMvpPresenter {

    void getPlayHistory();

    void getFavorite();

    void getByTag(int tagId);
}
