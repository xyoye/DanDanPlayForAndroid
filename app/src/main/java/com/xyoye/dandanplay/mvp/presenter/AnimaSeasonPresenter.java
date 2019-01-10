package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyy on 2019/1/9.
 */

public interface AnimaSeasonPresenter extends BaseMvpPresenter {

    void getSeasonAnima(int year, int month);
}
