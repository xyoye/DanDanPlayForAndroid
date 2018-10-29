package com.xyoye.dandanplay.mvp.view;

import com.xyoye.core.interf.view.BaseMvpView;
import com.xyoye.core.interf.view.LoadDataView;
import com.xyoye.dandanplay.bean.AnimeDetailBean;

/**
 * Created by YE on 2018/7/20.
 */


public interface AnimeDetailView extends BaseMvpView, LoadDataView{

    String getAnimaId();

    void showAnimeDetail(AnimeDetailBean bean);
}
