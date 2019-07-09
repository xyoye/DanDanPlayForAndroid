package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.AnimeFavoriteBean;
import com.xyoye.dandanplay.bean.AnimeTagBean;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

/**
 * Created by xyoye on 2018/7/24.
 */

public interface AnimeListView extends BaseMvpView, LoadDataView {
    void refreshHistory(PlayHistoryBean playHistoryBean);

    void refreshFavorite(AnimeFavoriteBean animeFavoriteBean);

    void refreshTagAnime(AnimeTagBean animeTagBean);
}
