package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;
import com.xyoye.dandanplay.utils.jlibtorrent.BtTask;

/**
 * Created by xyoye on 2019/8/1.
 */

public interface DownloadingFragmentPresenter extends BaseMvpPresenter {
    void setTaskFinish(BtTask task);
}
