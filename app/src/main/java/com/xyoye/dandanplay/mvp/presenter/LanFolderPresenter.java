package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;

/**
 * Created by xyy on 2018/11/21.
 */

public interface LanFolderPresenter extends BasePresenter {
    void getFolders();

    void searchFolder();

    void deleteFolder(String folder);
}
