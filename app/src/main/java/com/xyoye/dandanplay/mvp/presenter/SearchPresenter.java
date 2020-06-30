package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2019/1/8.
 */

public interface SearchPresenter extends BaseMvpPresenter {
    void queryTypeList();

    void querySubGroupList();

    void getSearchHistory(boolean doSearch);

    void addHistory(String text);

    void updateHistory(int _id);

    void deleteHistory(int _id);

    void deleteAllHistory();

    void search(String animeTitle, String text, int type, int subgroup);

    void downloadTorrent(String magnet, int position, boolean onlyDownload, boolean playResource);
}
