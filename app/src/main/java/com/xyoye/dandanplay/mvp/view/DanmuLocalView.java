package com.xyoye.dandanplay.mvp.view;

import com.xyoye.core.interf.view.BaseMvpView;
import com.xyoye.dandanplay.bean.DanmuFolderBean;

import java.util.List;

/**
 * Created by YE on 2018/7/2.
 */


public interface DanmuLocalView extends BaseMvpView {
    void refreshAdapter(List<DanmuFolderBean> beans);

    void showLoading();

    void hideLoading();

    void updatePathTitle(String path);

    boolean isFolder();
}
