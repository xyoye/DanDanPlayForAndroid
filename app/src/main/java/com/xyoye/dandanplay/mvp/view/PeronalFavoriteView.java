package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

/**
 * Created by YE on 2018/7/24.
 */


public interface PeronalFavoriteView extends BaseMvpView, LoadDataView {
    void refreshFavorite(AnimeFavoriteBean animeFavoriteBean);
}
