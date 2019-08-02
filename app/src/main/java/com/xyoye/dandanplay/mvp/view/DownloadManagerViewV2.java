package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

/**
 * Created by xyoye on 2019/8/1.
 */

public interface DownloadManagerViewV2 extends BaseMvpView, LoadDataView {

    void startNewTask();
}
