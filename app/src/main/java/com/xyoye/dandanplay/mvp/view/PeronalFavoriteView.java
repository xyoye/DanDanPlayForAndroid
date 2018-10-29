package com.xyoye.dandanplay.mvp.view;

import com.xyoye.core.interf.view.BaseMvpView;
import com.xyoye.core.interf.view.LoadDataView;
import com.xyoye.dandanplay.bean.AnimeFavoriteBean;

/**
 * Created by YE on 2018/7/24.
 */


public interface PeronalFavoriteView extends BaseMvpView, LoadDataView {
    void refreshFavorite(AnimeFavoriteBean animeFavoriteBean);
}
