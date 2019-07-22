package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.AnimeBean;
import com.xyoye.dandanplay.bean.BangumiBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by xyoye on 2019/1/9.
 */

public interface AnimeSeasonView extends BaseMvpView, LoadDataView{

    void refreshAnime(List<AnimeBean> anime);
}
