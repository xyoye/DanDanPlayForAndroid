package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.MagnetBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by YE on 2018/10/13.
 */


public interface SearchMagnetView extends BaseMvpView, LoadDataView {

    void refreshAdapter(List<MagnetBean.ResourcesBean> beanList);

    int getEpisodeId();

    void downloadTorrentOver(String torrentPath, String magnet);

    void showLoading(String text);
}
