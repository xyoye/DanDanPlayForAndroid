package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.AnimeTypeBean;
import com.xyoye.dandanplay.bean.MagnetBean;
import com.xyoye.dandanplay.bean.SearchHistoryBean;
import com.xyoye.dandanplay.bean.SubGroupBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by xyoye on 2019/1/8.
 */

public interface SearchView extends BaseMvpView{
    void refreshHistory(List<SearchHistoryBean> historyList, boolean doSearch);

    void refreshSearch(List<MagnetBean.ResourcesBean> searchResult);

    void downloadTorrentOver(String torrentPath, int position, boolean onlyDownload, boolean playResource);

    String getDownloadFolder();

    void showDownloadTorrentLoading();

    void dismissDownloadTorrentLoading();

    void showAnimeTypeDialog(List<AnimeTypeBean.TypesBean> typeList);

    void showSubGroupDialog(List<SubGroupBean.SubgroupsBean> subGroupList);
}
