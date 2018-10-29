package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;

/**
 * Created by YE on 2018/10/27.
 */


public interface DownloadManagerPresenter extends BasePresenter{
    void getTorrentList();

    void observeService();
}
