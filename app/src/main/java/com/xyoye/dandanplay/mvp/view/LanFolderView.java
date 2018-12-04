package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.FolderBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;

import java.util.List;

/**
 * Created by xyy on 2018/11/21.
 */

public interface LanFolderView extends BaseMvpView {
    void refreshFolder(List<FolderBean> folderBeans);
}
