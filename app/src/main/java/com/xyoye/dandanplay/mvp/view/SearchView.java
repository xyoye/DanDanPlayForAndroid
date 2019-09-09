package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.MagnetBean;
import com.xyoye.dandanplay.bean.SearchHistoryBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by xyoye on 2019/1/8.
 */

public interface SearchView extends BaseMvpView{
    void refreshHistory(List<SearchHistoryBean> historyList, boolean doSearch);

    void refreshSearch(List<MagnetBean.ResourcesBean> searchResult);

    void downloadTorrentOver(String torrentPath, String magnet);

    void downloadExisted(String torrentPath, String magnet);

    String getDownloadFolder();

    void showDownloadTorrentLoading();

    void dismissDownloadTorrentLoading();
}
