package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.bean.AnimeBeans;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

import java.util.List;

/**
 * Created by xyy on 2019/1/9.
 */

public interface AnimaSeasonPresenter extends BaseMvpPresenter {

    void getSeasonAnima(int year, int month);

    void sortAnima(List<AnimeBeans.BangumiListBean> animaList, int sortType);
}
