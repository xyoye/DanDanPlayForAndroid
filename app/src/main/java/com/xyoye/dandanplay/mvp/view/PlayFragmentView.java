package com.xyoye.dandanplay.mvp.view;

import com.xyoye.core.interf.view.BaseMvpView;
import com.xyoye.core.interf.view.LoadDataView;
import com.xyoye.dandanplay.bean.FolderBean;

import java.util.List;

/**
 * Created by YE on 2018/6/29 0029.
 */


public interface PlayFragmentView extends BaseMvpView, LoadDataView {
    void refreshAdapter(List<FolderBean> beans);
}
