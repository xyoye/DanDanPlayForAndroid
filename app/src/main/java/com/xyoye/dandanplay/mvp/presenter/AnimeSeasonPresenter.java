package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.bean.AnimeBean;
import com.xyoye.dandanplay.bean.BangumiBean;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

import java.util.List;

/**
 * Created by xyoye on 2019/1/9.
 */

public interface AnimeSeasonPresenter extends BaseMvpPresenter {

    void getSeasonAnime(int year, int month);

    void sortAnime(List<AnimeBean> animeList, int sortType);
}
