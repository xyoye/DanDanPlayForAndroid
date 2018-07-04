package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.core.interf.presenter.BasePresenter;

/**
 * Created by YE on 2018/6/29 0029.
 */


public interface PlayFragmentPresenter extends BasePresenter {
    void listFolder(String path);

    void getVideoList();
}
