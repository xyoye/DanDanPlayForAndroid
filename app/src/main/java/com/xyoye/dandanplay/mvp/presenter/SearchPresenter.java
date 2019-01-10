package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.bean.AnimeTypeBean;
import com.xyoye.dandanplay.bean.SubGroupBean;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

import java.util.List;

/**
 * Created by xyy on 2019/1/8.
 */

public interface SearchPresenter extends BaseMvpPresenter {
    List<AnimeTypeBean.TypesBean> getTypeList();

    List<SubGroupBean.SubgroupsBean> getSubGroupList();

    void getSearchHistory(boolean doSearch);

    void addHistory(String text);

    void updateHistory(int _id);

    void deleteHistory(int _id);

    void deleteAllHistory();

    void search(String text, int type, int subgroup);

    void downloadTorrent(String animeTitle, String magnet);
}
