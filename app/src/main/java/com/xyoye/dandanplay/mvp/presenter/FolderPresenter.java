package com.xyoye.dandanplay.mvp.presenter;

import com.xyoye.dandanplay.bean.VideoBean;
import com.xyoye.dandanplay.utils.interf.presenter.BaseMvpPresenter;

import java.util.List;

/**
 * Created by xyoye on 2018/6/30.
 */


public interface FolderPresenter extends BaseMvpPresenter {
    void getVideoList(String folderPath);

    void updateDanmu(String danmuPath, int episodeId, String[] whereArgs);

    void updateZimu(String zimuPath, String[] whereArgs);

    void getDanmu(String videoPath);

    void bindAllDanmu(List<VideoBean> videoList);

    void unbindAllDanmu(String folderPath);

    void bindAllZimu(List<VideoBean> videoList);

    void unbindAllZimu(String folderPath);
}
