package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2018/10/27.
 */

public interface DownloadManagerPresenter extends BaseMvpPresenter {
    void getTorrentList();

    void observeService();
}
