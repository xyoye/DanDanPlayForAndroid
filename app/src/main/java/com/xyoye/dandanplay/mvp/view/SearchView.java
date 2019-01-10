package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.MagnetBean;
import com.xyoye.dandanplay.bean.SearchHistoryBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by xyy on 2019/1/8.
 */

public interface SearchView extends BaseMvpView, LoadDataView {
    void refreshHistory(List<SearchHistoryBean> historyList, boolean doSearch);

    void refreshSearch(List<MagnetBean.ResourcesBean> searchResult);

    void downloadTorrentOver(String torrentPath, String magnet);
}
