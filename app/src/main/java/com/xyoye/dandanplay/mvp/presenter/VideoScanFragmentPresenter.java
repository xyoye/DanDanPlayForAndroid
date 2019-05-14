package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2019/5/14.
 */

public interface VideoScanFragmentPresenter extends BaseMvpPresenter {
    void addScanFolder(String path, boolean isScan);

    void queryScanFolderList(boolean isScan);

    void deleteScanFolder(String path, boolean isScan);
}
