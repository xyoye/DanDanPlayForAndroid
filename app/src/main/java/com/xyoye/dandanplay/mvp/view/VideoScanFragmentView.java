package com.xyoye.dandanplay.mvp.view;

import com.xyoye.dandanplay.bean.ScanFolderBean;
import com.xyoye.dandanplay.utils.interf.view.BaseMvpView;

import java.util.List;

/**
 * Created by xyoye on 2019/5/14.
 */

public interface VideoScanFragmentView extends BaseMvpView{
    void updateFolderList(List<ScanFolderBean> folderList);
}
