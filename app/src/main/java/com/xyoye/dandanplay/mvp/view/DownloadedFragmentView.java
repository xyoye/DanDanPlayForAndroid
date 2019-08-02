package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.DownloadedTaskBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;
import com.xyoye.dandanplay.utils.interf.view.LoadDataView;

import java.util.List;

/**
 * Created by xyoye on 2019/8/1.
 */

public interface DownloadedFragmentView extends BaseMvpView, LoadDataView {

    void updateTask(List<DownloadedTaskBean> taskList);
}
