package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

/**
 * Created by xyoye on 2018/6/29.
 */

public interface PlayFragmentPresenter extends BaseMvpPresenter {

    //获取系统视频数据
    void getVideoFormSystem();

    //获取系统视频数据 + 遍历保存路径
    void getVideoFormSystemAndSave();

    //获取数据库数据
    void getVideoFormDatabase();

    //删除目录
    void deleteFolder(String folderPath);

    VideoBean getLastPlayVideo(String videoPath);
}
