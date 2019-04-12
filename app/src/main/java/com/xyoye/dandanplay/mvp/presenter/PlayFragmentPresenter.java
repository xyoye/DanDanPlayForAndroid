package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by YE on 2018/6/29 0029.
 */


public interface PlayFragmentPresenter extends BaseMvpPresenter {

    //获取系统视频数据
    void getVideoFormSystem();

    //获取系统视频数据 + 遍历保存路径
    void getVideoFormSystemAndSave();

    //获取数据库数据
    void getVideoFormDatabase();
}
